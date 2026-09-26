-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "upbibtex",
  name = "upBibTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The upBibTeX reference management software",
      command = function(ctx)
        return getCommand("upbibtex", ctx.options, getBasename(ctx.reference.fileName))
      end
    },
  },
  arguments = {
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
