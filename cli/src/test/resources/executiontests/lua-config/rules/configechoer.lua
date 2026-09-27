return {
  identifier = "configechoer",
  name = "Lua configuration echo rule",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The echoer",
      command = function(ctx)
        return isWindows(
          getCommand("cmd", "/c", "echo", ctx.options),
          getCommand("echo", ctx.options)
        )
      end
    }
  },
  arguments = {
    {
      identifier = "options",
      default = {},
      flag = function(ctx, value)
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of options.")
        end
      end
    }
  }
}
