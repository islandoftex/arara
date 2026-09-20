return {
  identifier = "hellolua",
  name = "Lua engine smoke rule",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The echoer",
      command = function(ctx)
        return isWindows(
          getCommand("cmd", "/c", "echo", "LuaEngineWorks"),
          getCommand("echo", "LuaEngineWorks")
        )
      end
    }
  },
  arguments = { }
}
