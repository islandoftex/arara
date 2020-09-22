import org.islandoftex.arara.dsl.scripting.RuleMethods.error

fun generate(enginePrefix: String) {
    rule("${enginePrefix}latex",
            label = "${enginePrefix}LaTeX",
            description = "Rule for the ${
                if (enginePrefix.isEmpty())
                    "pdf"
                else
                    ""
            }tex engine and the latex format",
            authors = listOf("Island of TeX")) {
        argument<String>("branch") {
            defaultValue = "stable"
            processor {
                val engines = mapOf("stable" to "pdflatex", "developer" to "pdflatex-dev")
                if (it in engines.keys) {
                    engines.getValue(it)
                } else {
                    throw error("The valid branch values are: ${engines.keys}")
                }
            }
        }
        argument("interaction") {
            processor {
                val modes = listOf("batchmode", "nonstopmode", "scrollmode", "errorstopmode")
                if (it in modes) {
                    "--interaction=$it"
                } else {
                    throw error("The provided interaction value is not valid.")
                }
            }
        }
        argument<Boolean>("shell") {
            defaultValue = false
            processor {
                if (it)
                    "--shell-escape"
                else
                    "--no-shell-escape"
            }
        }
        argument<Boolean>("synctex") {
            defaultValue = false
            processor {
                "--synctex=${if (it) 1 else 0}"
            }
        }
        argument<Boolean>("draft") {
            defaultValue = false
            processor {
                if (it)
                    "--draftmode"
                else
                    ""
            }
        }
        argument<List<String>>("options") {
            defaultValue = listOf()
            processor { it }
        }

        command("${enginePrefix}latex" + arguments["branch"]!!,
                arguments["interaction"], arguments["draft"],
                arguments["shell"], arguments["synctex"],
                arguments["options"], reference.name
        )
    }
}

generate("")
generate("pdf")
generate("xe")
generate("lua")
generate("up")
generate("p")
