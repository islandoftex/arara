return {
  identifier = "unsafe",
  name = "The unsafe helper probe",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The unsafe command",
      command = function(ctx)
        unsafelyExecuteSystemCommand(
          getCommandWithWorkingDirectory(".", "echo", "should not run")
        )
        return true
      end
    }
  }
}
