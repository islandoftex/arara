package com.github.cereda.arara.utils

import com.github.cereda.arara.configuration.Configuration
import com.github.cereda.arara.model.Extractor
import com.github.cereda.arara.model.Interpreter
import com.github.cereda.arara.model.Parser
import com.github.cereda.arara.ruleset.DirectiveUtils
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.specs.ShouldSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ExecutionTest : ShouldSpec({
    should("run sessions") {
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))
        val previousUserDir = System.getProperty("user.dir")!!
        System.setProperty("user.dir", "src/test/resources/sessions")
        Configuration.load()
        Parser(arrayOf("-v", "session.tex")).parse()
        val directives = DirectiveUtils.validate(Extractor.extract(
                File("src/test/resources/sessions/session.tex")))
        Interpreter(directives).execute()
        System.setProperty("user.dir", previousUserDir)
        output.toString(Charsets.UTF_8.name()) shouldContain "MeowQuack"
    }
})