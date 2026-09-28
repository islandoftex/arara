-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "sage",
  name = "Sage",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The command line tool",
      command = function(ctx)
        local base = getBasename(ctx.reference.file_name)
        local e = isList(ctx.extension) and ctx.extension[1] or ctx.extension
        local input = (base .. ".") .. e
        return getCommand(ctx.program, ctx.command, ctx.dotsage, ctx.options, input)
      end
    },
  },
  arguments = {
    {
      identifier = "program",
      default = "sage",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "extension",
      default = "sage",
      flag = function(ctx, value)
        local exts = { "sage", "py", "spyx" }
        if contains(exts, value) then
          return value
        else
          throwError("The provided extension is invalid.")
        end
      end
    },
    {
      identifier = "command",
      flag = function(ctx, value)
        return { "-c", value }
      end
    },
    {
      identifier = "dotsage",
      flag = function(ctx, value)
        return isFalse(value, "--nodotsage")
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
