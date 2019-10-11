package com.github.cereda.arara.configuration

import com.github.cereda.arara.localization.Language
import com.github.cereda.arara.utils.CommonUtils
import com.uchuhimo.konf.ConfigSpec
import java.io.File
import java.time.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

object AraraSpec : ConfigSpec() {
    object Application : ConfigSpec() {
        val defaultLanguageCode by optional("en")
        val version by optional("5.0.0")
        val copyrightYear by optional("2012–${LocalDate.now().year}")
        val namePattern by optional("arara:\\s")
        val width by optional(65)
    }

    object Execution : ConfigSpec() {
        val loops by optional(10)
        val timeout by optional(false)
        @ExperimentalTime
        val timeoutValue by optional(0.milliseconds)
        val haltOnErrors by optional(true)

        val databaseName by optional("arara")
        val logName by optional("arara")

        val verbose by optional(false)
        val language by optional(Language(Application
                .defaultLanguageCode.default))
        val logging by optional(false)
        val dryrun by optional(false)
        val status by optional(0)
        val fileTypes by optional(ConfigurationUtils.defaultFileTypes)
        val rulePaths by optional(listOf(CommonUtils.buildPath(
                ConfigurationUtils.applicationPath,
                "rules"
        )))
        val preambles by optional(mapOf<String, String>())
        val preamblesActive by optional(false)
        val preamblesContent by optional("")

        val configurationName by optional("[none]")
        val header by optional(false)

        // TODO: these are runtime values, they should be properly
        // initialized and tested
        val reference by optional(File("/tmp/"))
        val file by optional(File("/tmp/"))

        object InfoSpec : ConfigSpec() {
            val ruleId by optional<String?>(null)
            val rulePath by optional<String?>(null)
        }

        object DirectiveSpec : ConfigSpec() {
            val lines by optional(listOf<Int>())
        }

        val ruleArguments by optional(listOf<String>())
        val filePattern by optional("")
    }

    object Directive : ConfigSpec() {
        val linebreakPattern by optional("^\\s*-->\\s(.*)$")

        private val directive = "^\\s*(\\w+)\\s*(:\\s*(\\{.*\\})\\s*)?"
        private val pattern = "(\\s+(if|while|until|unless)\\s+(\\S.*))?$"
        val directivePattern by optional(directive + pattern)
    }

    object Trigger : ConfigSpec() {
        val halt by optional(false)
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