return {
  name = "project configuration test",
  files = {
    ["project-config.tex"] = {
      fileType = { extension = "tex" },
      directives = { "% arara: projectconfig" }
    }
  }
}
