-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
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
        if not is_empty(ctx.targets) then
          local tasks = {}
          for _, target in ipairs(ctx.targets) do
            table.insert(tasks, get_command("make", target, ctx.options))
          end
          return tasks
        else
          return get_command("make", ctx.options)
        end
      end
    },
  },
  arguments = {
    {
      identifier = "targets",
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of targets.")
        end
      end
    },
    {
      identifier = "options",
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
  }
}
