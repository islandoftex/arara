-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "make",
  name = "Make",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The Make program",
      command = function(ctx)
        if not isEmpty(ctx.targets) then
          local tasks = {}
          for _, target in ipairs(ctx.targets) do
            table.insert(tasks, getCommand("make", target, ctx.options))
          end
          return tasks
        else
          return getCommand("make", ctx.options)
        end
      end
    },
  },
  arguments = {
    {
      identifier = "targets",
      flag = function(ctx, value)
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of targets.")
        end
      end
    },
    {
      identifier = "options",
      flag = function(ctx, value)
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of options.")
        end
      end
    },
  }
}
