-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "gnuplot",
  name = "The Gnuplot program",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "Gnuplot",
      command = function(ctx)
        local c = {}
        for _, entry in ipairs(ctx.input) do
          table.insert(c, get_command("gnuplot", ctx.persist, ctx.default, ctx.commands, entry))
        end
        return c
      end
    },
  },
  arguments = {
    {
      identifier = "persist",
      flag = function(ctx, value)
        return is_true(value, "--persist")
      end
    },
    {
      identifier = "default",
      flag = function(ctx, value)
        return is_true(value, "--default")
      end
    },
    {
      identifier = "commands",
      flag = function(ctx, value)
        return { "-e", value }
      end
    },
    {
      identifier = "input",
      required = true,
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of input files.")
        end
      end
    },
  }
}
