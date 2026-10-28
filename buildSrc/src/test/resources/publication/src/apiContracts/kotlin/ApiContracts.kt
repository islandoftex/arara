// SPDX-License-Identifier: BSD-3-Clause
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.localization.CatalogMetadata
import org.islandoftex.arara.api.localization.MPPLocale
import org.islandoftex.arara.api.reporting.RunSummary
import org.islandoftex.arara.api.rules.CommandEvaluator
import org.islandoftex.arara.api.rules.DirectiveFetchingHooks
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.session.ExecutorHooks
import org.islandoftex.arara.api.ui.ConsoleOutput
import org.islandoftex.arara.api.ui.DialogRenderer

/** Compiles against the API artifact alone, without core, interpreters or terminal libraries. */
fun verifyApiContracts() {
    val evaluator = object : CommandEvaluator {
        override fun evaluate(parameters: Map<String, Any>): Any? = parameters["result"]
        override fun isSuccessful(exitCode: Int): Boolean = exitCode == 0
    }
    check(evaluator.evaluate(mapOf("result" to true)) == true && evaluator.isSuccessful(0))
    val catalog = object : CatalogMetadata {
        override val providedLocale = MPPLocale("en")
        override val translators = emptyList<String>()
    }
    check(catalog.providedLocale == MPPLocale("en"))
    val dialog = object : DialogRenderer {
        override fun showMessage(type: Int, title: String, text: String) = Unit
        override fun showInput(type: Int, title: String, text: String): String = text
        override fun showOptions(type: Int, title: String, text: String, buttons: Array<out Any>): Int = 0
        override fun showList(type: Int, title: String, text: String, elements: Array<out Any>): Int = 0
    }
    check(dialog.showInput(0, "title", "answer") == "answer")
    val output = StringBuilder()
    val console = object : ConsoleOutput {
        override fun printWrapped(text: String) { output.append(text) }
    }
    console.printWrapped(RuleEngine.LUA.name)
    check(output.toString() == "LUA")
    check(RunSummary(true, false, emptyList()).success)
    ExecutorHooks().executeBeforeExecution()
    check(DirectiveFetchingHooks().processPotentialDirective(1, "directive") == "directive")
    check(MPPPath(".").normalize().isAbsolute)
}
