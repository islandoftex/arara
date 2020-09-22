import java.io.File
import org.islandoftex.arara.dsl.scripting.RuleMethods.error

fun generate(name: String, output: String = "pdf") {
    rule(name.toLowerCase(),
            label = name.toUpperCase(),
            description = "Rule for the ${name.toUpperCase()} program",
            authors = listOf("Island of TeX")) {
        argument<String>("output") {
            defaultValue = ""
            processor { value ->
                value.takeIf { it.isNotEmpty() } ?: reference.name
            }
        }
        argument<List<String>>("options") {
            defaultValue = listOf()
            processor { it }
        }

        val infile = "${reference.nameWithoutExtension}.dvi"
        val outfile = "${File(output[0]).nameWithoutExtension}.$output"
        command(name.toLowerCase(), infile, "-o", outfile, options)
    }
}

generate("dvips", output = "ps")
generate("dvipdfmx")
generate("dvipdfm")
generate("xdvipdfmx")
