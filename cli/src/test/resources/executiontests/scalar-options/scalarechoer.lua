return {
  identifier = "scalarechoer",
  name = "Scalar options echo rule",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The echoer",
      command = function(ctx)
        return getCommand("echo", ctx.options)
      end
    }
  },
  arguments = {
    {
      identifier = "options",
      default = "none",
      flag = function(ctx, value)
        return value
      end
    }
  }
}
