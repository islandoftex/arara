return {
  identifier = "preambler",
  name = "The preamble marker",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The marker",
      command = function(ctx)
        return isWindows(
          getCommand("cmd", "/c", "echo", "PreambleWorks"),
          getCommand("echo", "PreambleWorks")
        )
      end
    }
  }
}
