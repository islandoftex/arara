// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.build.website

import java.io.File

/** Entry point for the offline Nix build; Gradle tasks use these same functions. */
fun main(args: Array<String>) {
    val parameters = args.drop(1)
    when (args.firstOrNull()) {
        "prepare" -> {
            val expected = listOf("REPOSITORY", "OUTPUT", "THEME")
            require(parameters.size == expected.size) { "prepare ${expected.joinToString(" ")}" }
            val (repo, output, theme) = parameters.map(::File)
            WebsiteSources.prepare(repo, output, theme)
        }
        "check" -> {
            require(parameters.size in 1..2) { "check SITE [SITE_URL]" }
            WebsiteContent.checkSite(File(parameters[0]), parameters.getOrElse(1) { WebsiteContent.SITE_URL })
        }
        else -> error("Expected prepare or check")
    }
}
