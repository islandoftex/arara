-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "dvipspdf",
  name = "DVIPSPDF",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The DVIPS program",
      command = function(ctx)
        local base = getBasename(ctx.reference.fileName) .. ".dvi"
        local out = getBasename(ctx.reference.fileName) .. ".ps"
        return getCommand("dvips", base, "-o", out, ctx.options1)
      end
    },
    {
      name = "The PS2PDF program",
      command = function(ctx)
        local base = getBasename(ctx.reference.fileName) .. ".ps"
        local out = getBasename(ctx.output[1]) .. ".pdf"
        return getCommand("ps2pdf", ctx.options2, base, "-o", out)
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
      identifier = "options1",
      flag = function(ctx, value)
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of options.")
        end
      end
    },
    {
      identifier = "options2",
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
