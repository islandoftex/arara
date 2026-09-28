return {
  name = "project configuration test",
  files = {
    ["project-config.tex"] = {
      file_type = { extension = "tex" },
      directives = { "% arara: projectconfig" }
    }
  }
}
