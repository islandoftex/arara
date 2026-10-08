return {
  identifier = "multitask",
  name = "Multitask",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The task pair",
      command = function(ctx)
        return {
          isWindows(
            getCommand('cmd', '/c', 'echo', 'one'),
            getCommand('echo', 'one')
          ),
          isWindows(
            getCommand('cmd', '/c', 'echo', 'two'),
            getCommand('echo', 'two')
          )
        }
      end
    }
  },
  arguments = {}
}
