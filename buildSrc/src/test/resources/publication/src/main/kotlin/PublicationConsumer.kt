// SPDX-License-Identifier: BSD-3-Clause
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.configuration.ExecutionOptions

fun main() {
    val path = MPPPath(".").resolve("publication-check")
    val options = ExecutionOptions()
    check(path.toString().isNotEmpty() && !options.verbose) {
        "Published library defaults are not usable"
    }
    println("Published core and transitive API are usable from Kotlin on Java 11.")
}
