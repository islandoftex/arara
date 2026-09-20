-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "pdftocairo",
  name = "pdftocairo",
  authors = { "samcarter" },
  commands = {
    {
      name = "The pdftocairo program",
      command = function(ctx)
        return getCommand("pdftocairo", "-" .. ctx.format[1], getBasename(ctx.reference.fileName) .. ".pdf", ctx.options, ctx.output)
      end
    },
  },
  arguments = {
    {
      identifier = "format",
      default = "png",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "output",
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
