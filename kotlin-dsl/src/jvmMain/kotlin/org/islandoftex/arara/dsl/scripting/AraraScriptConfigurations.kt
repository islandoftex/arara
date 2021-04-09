// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.scripting

import java.io.File
import java.net.JarURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.script.experimental.api.ScriptAcceptedLocation
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.acceptedLocations
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.api.ide
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache
import org.islandoftex.arara.dsl.language.DSLInstance

/**
 * The compiler configurationfor arara's configuration scripts.
 *
 * Major influences from
 * https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/simple-main-kts/simple-main-kts/src/main/kotlin/org/jetbrains/kotlin/script/examples/simpleMainKts/scriptDef.kt
 */
class AraraScriptCompilationConfiguration : ScriptCompilationConfiguration({
    defaultImports("java.io.*")
    implicitReceivers(DSLInstance::class)
    jvm {
        val keyResource = AraraScriptCompilationConfiguration::class.java
                .name.replace('.', '/') + ".class"
        val thisJarFile = AraraScriptCompilationConfiguration::class.java
                .classLoader.getResource(keyResource)?.toContainingJarOrNull()
        if (thisJarFile != null) {
            dependenciesFromClassContext(
                    AraraScriptCompilationConfiguration::class,
                    thisJarFile.name, "kotlin-stdlib", "kotlin-reflect",
                    "kotlin-scripting-dependencies"
            )
        } else {
            dependenciesFromClassContext(
                    AraraScriptCompilationConfiguration::class,
                    wholeClasspath = true
            )
        }
    }
    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
    hostConfiguration(ScriptingHostConfiguration {
        jvm {
            System.getProperty("java.io.tmpdir")?.let(::File)
                    ?.takeIf { it.exists() && it.isDirectory }
                    ?.let {
                        File(it, "org.islandoftex.arara.kts.cache")
                                .apply { mkdir() }
                    }
                    ?.let {
                        compilationCache(
                                CompiledScriptJarsCache { script, compilationConfiguration ->
                                    it.resolve(compiledScriptUniqueName(script, compilationConfiguration) + ".jar")
                                }
                        )
                    }
        }
    })
})

class AraraScriptEvaluationConfiguration : ScriptEvaluationConfiguration({
    implicitReceivers(DSLInstance)
})

private fun compiledScriptUniqueName(
    script: SourceCode,
    scriptCompilationConfiguration: ScriptCompilationConfiguration
): String {
    val digestWrapper = MessageDigest.getInstance("MD5")
    digestWrapper.update(script.text.toByteArray())
    scriptCompilationConfiguration.notTransientData.entries
            .sortedBy { it.key.name }
            .forEach {
                digestWrapper.update(it.key.name.toByteArray())
                digestWrapper.update(it.value.toString().toByteArray())
            }
    return digestWrapper.digest().toHexString()
}

private fun ByteArray.toHexString(): String = joinToString("", transform = { "%02x".format(it) })

internal fun URL.toContainingJarOrNull(): File? =
        if (protocol == "jar") {
            (openConnection() as? JarURLConnection)?.jarFileURL?.toFileOrNull()
        } else null

internal fun URL.toFileOrNull() =
        try {
            File(toURI())
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: java.net.URISyntaxException) {
            null
        } ?: run {
            if (protocol != "file") null
            else File(file)
        }
