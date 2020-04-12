// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.URLClassLoader

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

    /**
     * Loads a class from the provided file, potentially a Java archive.
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class.
     */
    fun loadClass(file: File, name: String):
            Pair<ClassLoadingStatus, Class<*>> {
        // status and class to be returned,
        // it defaults to an object class
        var value: Class<*> = Any::class.java

        // if file does not exist, nothing
        // can be done, status is changed
        val status = if (!file.exists()) {
            ClassLoadingStatus.FILE_NOT_FOUND
        } else {
            // classloading involves defining
            // a classloader and fetching the
            // desired class from it, based on
            // the provided file archive
            try {
                // creates a new classloader with
                // the provided file (potentially
                // a JAR file)
                val classloader = URLClassLoader(arrayOf(file.toURI().toURL()),
                        ClassLoading::class.java.classLoader)

                // fetches the class from the
                // instantiated classloader
                value = Class.forName(name, true, classloader)
                ClassLoadingStatus.SUCCESS
            } catch (_: MalformedURLException) {
                ClassLoadingStatus.MALFORMED_URL
            } catch (_: ClassNotFoundException) {
                ClassLoadingStatus.CLASS_NOT_FOUND
            }
        }

        // return a new pair based on the
        // current status and class holder
        return status to value
    }

    /**
     * Loads a class from the provided file, instantiating it.
     * @param file File containing the Java bytecode (namely, a JAR).
     * @param name The canonical name of the class.
     * @return A pair representing the status and the class object.
     */
    fun loadObject(file: File, name: String): Pair<ClassLoadingStatus, Any> {
        // load the corresponding class
        // based on the qualified name
        val pair = loadClass(file, name)

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
