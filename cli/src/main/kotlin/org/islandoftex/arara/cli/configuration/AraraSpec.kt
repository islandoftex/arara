// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.uchuhimo.konf.ConfigSpec
import java.io.File
import java.nio.file.Paths
import kotlin.time.milliseconds
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.session.ExecutionOptions
import org.islandoftex.arara.cli.localization.Language
import org.islandoftex.arara.cli.model.ProjectFile
import org.islandoftex.arara.cli.model.UNKNOWN_TYPE

/**
 * Configuration hierarchy for arara
 *
 * @author Island of TeX
 * @version 5.0
 * @since 5.0
 */
@Suppress("MagicNumber")
object AraraSpec : ConfigSpec() {
    object Application : ConfigSpec() {
        val defaultLanguageCode by optional("en")
        val version by optional(AraraSpec::class.java.`package`.implementationVersion
                ?: "DEVELOPMENT BUILD")
        val namePattern by optional("arara:\\s")
        val width by optional(65)
    }

    object Execution : ConfigSpec() {
        val executionOptions by optional<ExecutionOptions>(
                org.islandoftex.arara.core.session.ExecutionOptions()
        )
        val maxLoops by lazy { it[executionOptions].maxLoops }
        val timeout by lazy { it[executionOptions].timeoutValue != 0.milliseconds }
        val timeoutValue by lazy { it[executionOptions].timeoutValue }
        val haltOnErrors by lazy { it[executionOptions].haltOnErrors }
        val databaseName by lazy { it[executionOptions].databaseName }
        val logName by optional("arara")

        val verbose by lazy { it[executionOptions].verbose }
        val language by optional(Language(Application.defaultLanguageCode.default))
        val logging by optional(false)
        val dryrun by optional(false)
        val status by optional(0)
        val fileTypes by optional(ConfigurationUtils.defaultFileTypes)
        val rulePaths by optional(setOf(
                ConfigurationUtils.applicationPath.resolve("rules")
                        .toString()
        ))
        val preambles by optional(mapOf<String, String>())
        val preamblesActive by optional(false)
        val preamblesContent by optional("")

        val workingDirectory by optional(Paths.get(""))
        val configurationName by optional("[none]")
        val onlyHeader by lazy { it[executionOptions].parseOnlyHeader }

        // TODO: these are runtime values, they should be properly
        // initialized and tested (maybe move them into their own
        // Spec or session)
        val reference by optional<org.islandoftex.arara.api.files.ProjectFile>(
                ProjectFile(Paths.get("/tmp/"), FileType.UNKNOWN_TYPE)
        )
        val file by optional(File("/tmp/"))

        object InfoSpec : ConfigSpec() {
            val ruleId by optional<String?>(null)
            val rulePath by optional<String?>(null)
        }
    }

    object UserInteraction : ConfigSpec() {
        val lookAndFeel by optional("none")
        val displayTime by optional(false)
        val displayLine by optional(true)
        val displayResult by optional(false)
        val displayRolling by optional(false)
        val displayException by optional(false)
    }
}
