// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.configuration

import com.uchuhimo.konf.ConfigSpec
import java.nio.file.Paths
import kotlin.time.milliseconds
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.session.ExecutionMode
import org.islandoftex.arara.api.session.ExecutionOptions
import org.islandoftex.arara.api.session.LoggingOptions
import org.islandoftex.arara.api.session.UserInterfaceOptions
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
        val version by optional(AraraSpec::class.java.`package`.implementationVersion
                ?: "DEVELOPMENT BUILD")
    }

    object Execution : ConfigSpec() {
        val executionOptions by optional<ExecutionOptions>(
                org.islandoftex.arara.core.session.ExecutionOptions()
        )
        val loggingOptions by optional<LoggingOptions>(
                org.islandoftex.arara.core.session.LoggingOptions()
        )
        val userInterfaceOptions by optional<UserInterfaceOptions>(
                org.islandoftex.arara.core.session.UserInterfaceOptions()
        )
        val maxLoops by lazy { it[executionOptions].maxLoops }
        val timeout by lazy { it[executionOptions].timeoutValue != 0.milliseconds }
        val timeoutValue by lazy { it[executionOptions].timeoutValue }
        val haltOnErrors by lazy { it[executionOptions].haltOnErrors }
        val databaseName by lazy { it[executionOptions].databaseName }

        val verbose by lazy { it[executionOptions].verbose }
        val language by lazy { Language(it[userInterfaceOptions].languageCode) }
        val dryrun by lazy { it[executionOptions].executionMode == ExecutionMode.DRY_RUN }
        val exitCode by optional(0)
        val fileTypes by lazy { it[executionOptions].fileTypes }
        val rulePaths by lazy {
            it[executionOptions].rulePaths.plus(
                    ConfigurationUtils
                            .applicationPath.resolve("rules")
            )
        }
        val preambles by optional(mapOf<String, String>())
        val preamblesActive by optional(false)
        val preamblesContent by optional("")

        val workingDirectory by optional(Paths.get(""))
        val configurationName by optional("[none]")
        val onlyHeader by lazy { it[executionOptions].parseOnlyHeader }

        // TODO: this is a runtime value which should be properly
        // initialized and tested (maybe move it into its own
        // Spec or session)
        val reference by optional<org.islandoftex.arara.api.files.ProjectFile>(
                ProjectFile(Paths.get("/tmp/"), FileType.UNKNOWN_TYPE)
        )

        object InfoSpec : ConfigSpec() {
            val ruleId by optional<String?>(null)
            val rulePath by optional<String?>(null)
        }
    }

    object UserInteraction : ConfigSpec() {
        val displayLine by optional(true)
        val displayResult by optional(false)
        val displayRolling by optional(false)
        val displayException by optional(false)
    }
}
