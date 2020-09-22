import org.islandoftex.arara.dsl.scripting.RuleMethods.error

fun generate(prefix: String) {
    rule("${prefix}xindy",
            label = "${prefix.capitalize()}Xindy",
            description = "Rule for the ${prefix.capitalize()}Xindy software",
            authors = listOf("Island of TeX")) {
        argument<Boolean>("quiet") {
            defaultValue = false
            processor {
                if (it) "-q" else ""
            }
        }
        argument<List<String>>("modules") {
            defaultValue = listOf()
            processor {
                it.flatMap { module ->
                    listOf("-M", module)
                }.joinToString(" ")
            }
        }
        argument("codepage") {
            processor { cp ->
                cp?.let { "-C $it" } ?: ""
            }
        }
        argument("language") {
            processor { lang ->
                lang?.let { "-L $it" } ?: ""
            }
        }
        argument("markup") {
            processor {
                val values = listOf("latex", "xelatex", "omega").let {
                    if (prefix.isNotBlank()) it else it.plus("xindy")
                }
                if (it in values) {
                    return "-I $it"
                } else {
                    throw error("The provided markup is invalid.")
                }
            }
        }
        argument<String>("input") {
            defaultValue = "idx"
        }
        argument<String>("output") {
            defaultValue = "ind"
        }
        argument<String>("log") {
            defaultValue = "ilg"
        }
        argument<List<String>>("options") {
            defaultValue = listOf()
            processor {
                it.joinToString(" ")
            }
        }

        val base = reference.nameWithoutExtension
        val infile = "$base.${arguments["input"]!!}"
        val outfile = "-o $base.${arguments["output"]!!}"
        val logfile = "-t $base.${arguments["log"]!!}"
        command("${prefix}xindy", arguments["quiet"],
                arguments["markup"], arguments["modules"],
                arguments["codepage"], arguments["language"],
                logfile, outfile, argument["options"], infile,
                reference.name
        )
    }
}

generate("")
generate("te")
