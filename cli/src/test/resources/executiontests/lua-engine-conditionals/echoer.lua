return {
  identifier = "echoer",
  name = "Lua conditional echo rule",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The echoer",
      command = function(ctx)
        return getCommand("echo", ctx.marker)
      end
    }
  },
  arguments = {
    {
      identifier = "marker",
      default = "Quack",
      flag = function(ctx, value)
        return value
      end
    }
  }
}
