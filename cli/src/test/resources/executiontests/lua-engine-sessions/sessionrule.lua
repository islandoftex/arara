return {
  identifier = "sessionrule",
  name = "Test rule for sessions",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The echoer",
      command = function(ctx)
        local session = getSession()
        session.put('quack', 'Quack')
        return isWindows(
          getCommand('cmd', '/c', 'echo', 'Quack'),
          getCommand('echo', 'Quack')
        )
      end
    },
    {
      name = "The echoer mark II",
      command = function(ctx)
        local session = getSession()
        return isWindows(
          getCommand(
            'cmd', '/c', 'echo', 'Meow' .. session.get('quack')
          ),
          getCommand('echo', 'Meow' .. session.get('quack'))
        )
      end
    }
  },
  arguments = {}
}
