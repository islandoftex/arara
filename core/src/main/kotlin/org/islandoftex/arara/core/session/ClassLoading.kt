// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.URLClassLoader
import java.nio.file.Files
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath

/**
 * Implements utilitary methods for classloading and object instantiation.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
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

    private data class LoadedArchive(val loader: URLClassLoader, val copy: MPPPath?) {
        fun close() {
            try {
                loader.close()
            } finally {
                // Snapshot cleanup must not replace the class-loading result.
                copy?.let { runCatching { Files.deleteIfExists(it.toJVMPath()) } }
            }
        }
    }

    private val archives = mutableListOf<LoadedArchive>()

    init {
        Runtime.getRuntime().addShutdownHook(Thread { closeLoaders() })
    }

    /**
     * Release loaders once the host no longer needs any returned classes or objects.
     * Successful loaders stay open until this call or JVM shutdown so lazy companion
     * classes and resources remain available. The user's archive itself is never held open.
     */
    fun closeLoaders() = synchronized(archives) {
        archives.forEach { runCatching { it.close() } }
        archives.clear()
    }

    private fun openArchive(path: MPPPath): LoadedArchive {
        // Directories keep their existing classpath semantics; jar files get a private snapshot.
        val copy = if (path.isRegularFile) Files.createTempFile("arara-classpath-", ".jar").toMPPPath() else null
        var opened = false
        try {
            if (copy != null) {
                path.read { source ->
                    // MPPPath has no binary sink API; only the local snapshot output uses NIO.
                    Files.newOutputStream(copy.toJVMPath()).use { output -> source.inputStream().copyTo(output) }
                }
            }
            val resource = LoadedArchive(URLClassLoader(
                arrayOf((copy ?: path).toJVMPath().toUri().toURL()), ClassLoading::class.java.classLoader
            ), copy)
            opened = true
            return resource
        } finally {
            if (!opened) copy?.let { runCatching { Files.deleteIfExists(it.toJVMPath()) } }
        }
    }

    /** Load a class from an independent snapshot of the provided archive. */
    @JvmStatic
    fun loadClass(path: MPPPath, name: String): Pair<ClassLoadingStatus, Class<*>> {
        if (!path.exists) return ClassLoadingStatus.FILE_NOT_FOUND to Any::class.java
        val archive = try {
            openArchive(path)
        } catch (_: MalformedURLException) {
            return ClassLoadingStatus.MALFORMED_URL to Any::class.java
        } catch (_: IOException) {
            return ClassLoadingStatus.CLASS_NOT_FOUND to Any::class.java
        } catch (_: AraraIOException) {
            return ClassLoadingStatus.CLASS_NOT_FOUND to Any::class.java
        }
        var retained = false
        try {
            val loaded = Class.forName(name, true, archive.loader)
            synchronized(archives) { archives.add(archive) }
            retained = true
            return ClassLoadingStatus.SUCCESS to loaded
        } catch (_: ClassNotFoundException) {
            return ClassLoadingStatus.CLASS_NOT_FOUND to Any::class.java
        } finally {
            if (!retained) archive.close()
        }
    }

    /**
     * Loads a class from the provided file, instantiating it.
     * @param path File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    @JvmStatic
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
            } catch (_: IllegalAccessException) {
                status = ClassLoadingStatus.ILLEGAL_ACCESS
            } catch (_: NoSuchMethodException) {
                status = ClassLoadingStatus.INSTANTIATION_EXCEPTION
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
