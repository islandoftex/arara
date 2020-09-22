import org.islandoftex.arara.dsl.scripting.RuleMethods.error

fun generate(enginePrefix: String) {
    rule("${enginePrefix}tex",
            label = "${enginePrefix}TeX",
            description = "Rule for the ${
                if (enginePrefix.isEmpty())
                    "pdf"
                else
                    ""
            }tex engine and plain tex format",
            authors = listOf("Island of TeX")) {
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

        command("${enginePrefix}tex", arguments["interaction"],
                arguments["draft"], arguments["shell"],
                arguments["synctex"], arguments["options"],
                reference.name
        )
    }
}

generate("")
generate("e")
generate("pdf")
generate("xe")
generate("lua")
generate("luahb")
generate("up")
generate("p")
