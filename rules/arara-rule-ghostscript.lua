-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "ghostscript",
  name = "GhostScript",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The command line tool",
      command = function(ctx)
        return getCommand(ctx.program, ctx.device, ctx.output, ctx.options)
      end
    },
  },
  arguments = {
    {
      identifier = "program",
      default = "gs",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "options",
      required = true,
      flag = function(ctx, value)
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of options.")
        end
      end
    },
    {
      identifier = "device",
      flag = function(ctx, value)
        return "-sDEVICE=" .. value
      end
    },
    {
      identifier = "output",
      flag = function(ctx, value)
        return "-sOutputFile=" .. value
      end
    },
  }
}
