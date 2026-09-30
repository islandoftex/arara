return {
  identifier = "snaketest",
  name = "Snake case helper showcase",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The echoer",
      command = function(ctx)
        return is_windows(
          get_command("cmd", "/c", "echo",
            trim_spaces("  snake works  "),
            get_basename(ctx.reference.file_name)),
          get_command("echo",
            trim_spaces("  snake works  "),
            get_basename(ctx.reference.file_name))
        )
      end
    }
  },
  arguments = {}
}
