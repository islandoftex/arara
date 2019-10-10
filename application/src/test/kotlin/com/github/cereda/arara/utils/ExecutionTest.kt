package com.github.cereda.arara.utils

import com.github.cereda.arara.configuration.Configuration
import com.github.cereda.arara.model.Extractor
import com.github.cereda.arara.model.Interpreter
import com.github.cereda.arara.model.Parser
import com.github.cereda.arara.ruleset.DirectiveUtils
import io.kotlintest.DoNotParallelize
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.specs.ShouldSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.time.ExperimentalTime

@ExperimentalTime
@DoNotParallelize
class ExecutionTest : ShouldSpec({
    fun getPathForTest(name: String): String = "src/test/resources/$name"
    fun outputForTest(testName: String): String {
        val sysout = System.out
        val previousUserDir = System.getProperty("user.dir")!!
        val output = ByteArrayOutputStream()
        try {
            System.setOut(PrintStream(output))
            System.setProperty("user.dir", getPathForTest(testName))
            Configuration.load()
            Parser(arrayOf("-v", "$testName.tex")).parse()
            val directives = DirectiveUtils.validate(Extractor.extract(
                    File("${getPathForTest(testName)}/$testName.tex")))
            Interpreter(directives).execute()
            return output.toByteArray().toString(Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            System.setProperty("user.dir", previousUserDir)
            System.setOut(sysout)
            output.close()
        }
        return ""
    }

    should("be able to store variables sessions") {
        outputForTest("sessions") shouldContain "MeowQuack"
    }

    should("honor falsy existence test") {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        if(file.exists()) file.delete()
        val output = outputForTest("conditionals")
        output shouldContain "QuackOne"
        output shouldContain "QuackFour"
        output shouldNotContain "QuackTwo"
        output shouldNotContain "QuackThree"
    }
    should("honor truthy existence (falsy contains) test") {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        file.writeText("Meow")
        val output = outputForTest("conditionals")
        file.delete()
        output shouldContain "QuackOne"
        output shouldContain "QuackFour"
        output shouldContain "QuackTwo"
        output shouldNotContain "QuackThree"
    }
    should("honor truthy contains test") {
        val file = File(getPathForTest("conditionals") + "/conditionals.quack")
        file.writeText("Duck")
        val output = outputForTest("conditionals")
        file.delete()
        output shouldContain "QuackOne"
        output shouldContain "QuackFour"
        output shouldContain "QuackTwo"
        output shouldContain "QuackThree"
    }

    should("track changes") {
        val file = File(getPathForTest("changes") + "/arara.yaml")
        if(file.exists()) file.delete()
        outputForTest("changes") shouldContain "QuackOne"
        outputForTest("changes") shouldNotContain "QuackOne"
    }
})
