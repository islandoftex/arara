/*
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara.utils

import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.MalformedURLException
import java.net.URLClassLoader

/**
 * Implements utilitary methods for classloading and object instantiation.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
object ClassLoadingUtils {
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
                        ClassLoadingUtils::class.java.classLoader)

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
