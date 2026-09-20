-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "dvipdfm",
  name = "DVIPDFM",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The DVIPDFM program",
      command = function(ctx)
        local base = getBasename(ctx.reference.fileName) .. ".dvi"
        local out = getBasename(ctx.output[1]) .. ".pdf"
        return getCommand("dvipdfm", base, "-o", out, ctx.options)
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
