-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "xindex",
  name = "xindex",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The xindex software",
      command = function(ctx)
        return getCommand("xindex", ctx.config, ctx.language, ctx.options, ctx.input)
      end
    },
  },
  arguments = {
    {
      identifier = "input",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "config",
      default = "",
      flag = function(ctx, value)
        return { "-c", isEmpty(value, "cfg", value) }
      end
    },
    {
      identifier = "language",
      default = "",
      flag = function(ctx, value)
        return { "-l", isEmpty(value, "en", value) }
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
