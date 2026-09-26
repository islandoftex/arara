-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "bibtex8",
  name = "BibTeX8",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "An 8-bit implementation of BibTeX 0.99 with a very large capacity",
      command = function(ctx)
        return getCommand("bibtex8", ctx.options, getBasename(ctx.reference.fileName))
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
