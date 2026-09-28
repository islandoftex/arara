-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "animate",
  name = "Animate",
  authors = { "Chris Hughes", "Island of TeX" },
  commands = {
    {
      name = "The Convert program",
      command = function(ctx)
        local input = getBasename(ctx.reference.file_name) .. ".pdf"
        local output = getBasename(ctx.reference.file_name) .. ".gif"
        return getCommand(ctx.program, "-delay", ctx.delay, "-loop", ctx.loop, "-density", ctx.density, ctx.options, input, output)
      end
    },
  },
  arguments = {
    {
      identifier = "delay",
      default = "10",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "loop",
      default = "0",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "density",
      default = "300",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "program",
      default = "magick",
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
  }
}
