// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.URLClassLoader
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile

/**
 * Implements the class loading and object instantiation helpers the
 * registry exposes to rules, backed by archive inspection.
 *
 * @author Island of TeX
 */
object ClassLoading {
    /**
     * Indicator of success or failure of class loading.
     */
    enum class ClassLoadingStatus {
        SUCCESS,
        FILE_NOT_FOUND,
        MALFORMED_URL,
        CLASS_NOT_FOUND,
        ILLEGAL_ACCESS,
        INSTANTIATION_EXCEPTION
    }

    // loaders stay open for the lifetime of the JVM: the loaded classes
    // keep referencing their defining loader, and the JVM resolves
    // lazily referenced companion classes from the same archive through
    // it. Each loader operates on a private copy of the archive, so
    // the user's file is never held open — a long-lived embedding (a
    // daemon, a server) must not lock user archives. The shutdown hook
    // closes the loaders and deletes the copies at exit. Repeated
    // loads of the same archive deliberately create independent
    // loaders, so a rebuilt archive is served fresh on the next call.
    /**
     * The monitor guarding the loader list, so class loading
     * stays thread-safe.
     */
    private val lock = Any()
    /**
     * The class loaders the session has opened, so they can
     * be closed when the session releases them.
     */
    private val loaders = mutableListOf<URLClassLoader>()
    /**
     * The temporary copies of the loaded jar files, tracked
     * so they can be cleaned up with their loaders.
     */
    private val copies = mutableListOf<File>()

    init {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                // close first: an open file cannot be deleted on some
                // operating systems
                synchronized(lock) {
                    // the exit cleanup is best effort: one loader
                    // refusing to close must not strand the loaders
                    // after it nor the copies they own
                    loaders.forEach { loader ->
                        runCatching { loader.close() }
                    }
                    copies.forEach { copy ->
                        runCatching { copy.delete() }
                    }
                }
            }
        )
    }

    /**
     * Loads a class from the provided file, potentially a Java archive.
     *
     * The archive is copied to a private temporary file so the user's
     * file is never held open, and the class loader backing the copy
     * stays open for the lifetime of the JVM: the loaded class keeps
     * referencing its defining loader, and lazily referenced companion
     * classes from the same archive resolve through it.
     * @param path File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    fun loadClass(path: MPPPath, name: String):
        Pair<ClassLoadingStatus, Class<*>> {
        val file = path.toJVMFile()

        // status and class to be returned,
        // it defaults to an object class
        var value: Class<*> = Any::class.java

        // if file does not exist, nothing
        // can be done, status is changed
        val status = if (!file.exists()) {
            ClassLoadingStatus.FILE_NOT_FOUND
        } else {
            // classloading involves copying the archive, defining a
            // classloader over the private copy and fetching the
            // desired class from it
            try {
                // the copy keeps the user's file out of the loader's
                // hands; the copy itself stays open as long as the
                // loaded classes may lazily resolve through it
                val copy = File.createTempFile("arara-classpath-", ".jar")
                try {
                    file.copyTo(copy, overwrite = true)
                } catch (ioexception: IOException) {
                    copy.delete()
                    throw ioexception
                }
                val classloader = try {
                    URLClassLoader(
                        arrayOf(copy.toURI().toURL()),
                        ClassLoading::class.java.classLoader
                    )
                } catch (exception: MalformedURLException) {
                    // the copy has no consumer when its archive
                    // cannot become a loader source, so it must
                    // not linger behind the failed attempt
                    copy.delete()
                    throw exception
                }
                synchronized(lock) {
                    loaders.add(classloader)
                    copies.add(copy)
                }
                // fetches the class from the instantiated classloader
                value = Class.forName(name, true, classloader)
                ClassLoadingStatus.SUCCESS
            } catch (_: MalformedURLException) {
                ClassLoadingStatus.MALFORMED_URL
            } catch (_: ClassNotFoundException) {
                ClassLoadingStatus.CLASS_NOT_FOUND
            } catch (_: IOException) {
                // the archive could not be read for the private copy,
                // so it cannot serve its classes either
                ClassLoadingStatus.CLASS_NOT_FOUND
            }
        }

        // return a new pair based on the
        // current status and class holder
        return status to value
    }

    /**
     * Loads a class from the provided file, instantiating it.
     * @param path File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    fun loadObject(path: MPPPath, name: String): Pair<ClassLoadingStatus, Any> {
        // load the corresponding class
        // based on the qualified name
        val pair = loadClass(path, name)

        // status and object to be returned,
        // it defaults to an object
        var status = pair.first
        var value = Any()

        // checks if the class actually
        // exists, otherwise simply
        // ignore instantiation
        if (status == ClassLoadingStatus.SUCCESS) {
            // object instantiation relies
            // on the default constructor
            // (without arguments), class
            // must implement it

            // OBS: constructors with arguments
            // must be invoked through reflection
            try {
                // get the class reference from
                // the pair and instantiate it
                // by invoking the default
                // constructor (without arguments)
                value = pair.second.getDeclaredConstructor().newInstance()
            } catch (_: NoSuchMethodException) {
                // the class does not provide a default constructor
                status = ClassLoadingStatus.INSTANTIATION_EXCEPTION
            } catch (_: IllegalAccessException) {
                status = ClassLoadingStatus.ILLEGAL_ACCESS
            } catch (_: InstantiationException) {
                // the user wanted to instantiate an abstract class
                status = ClassLoadingStatus.INSTANTIATION_EXCEPTION
            } catch (_: InvocationTargetException) {
                // the underlying constructor caused an exception
                status = ClassLoadingStatus.INSTANTIATION_EXCEPTION
            }
        }

        // return a new pair based on the
        // current status and object holder
        return status to value
    }
}
