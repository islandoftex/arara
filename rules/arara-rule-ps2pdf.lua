-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "ps2pdf",
  name = "PS2PDF",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The PS2PDF program",
      command = function(ctx)
        local infile = getBasename(ctx.reference.fileName) .. ".ps"
        local outfile = getBasename(ctx.output[1]) .. ".pdf"
        return getCommand("ps2pdf", ctx.options, infile, outfile)
      end
    },
  },
  arguments = {
    {
      identifier = "output",
      default = "",
      flag = function(ctx, value)
        return isEmpty(value, ctx.reference.fileName, value)
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
