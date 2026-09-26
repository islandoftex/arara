-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "pdftk",
  name = "PDFtk",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "PDFtk",
      command = function(ctx)
        local input = getBasename(ctx.reference.fileName) .. ".pdf"
        return getCommand("pdftk", input, ctx.options)
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
