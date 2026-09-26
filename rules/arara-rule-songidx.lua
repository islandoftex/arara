-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "songidx",
  name = "SongIDX",
  authors = { "Francesco Endrici", "Island of TeX" },
  commands = {
    {
      name = "The SongIDX Lua script",
      command = function(ctx)
        local infile = getBasename(ctx.input[1]) .. ".sxd"
        return getCommand("texlua", ctx.script, ctx.options, infile)
      end
    },
  },
  arguments = {
    {
      identifier = "input",
      required = true,
      flag = function(ctx, value)
        return value
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
    {
      identifier = "script",
      default = "songidx.lua",
      flag = function(ctx, value)
        return value
      end
    },
  }
}
