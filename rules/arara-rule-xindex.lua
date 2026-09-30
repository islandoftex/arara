-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
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
        return get_command("xindex", ctx.config, ctx.language, ctx.options, ctx.input)
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
        return { "-c", is_empty(value, "cfg", value) }
      end
    },
    {
      identifier = "language",
      default = "",
      flag = function(ctx, value)
        return { "-l", is_empty(value, "en", value) }
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
