-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "qpdf",
  name = "QPDF",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The QPDF program",
      command = function(ctx)
        return getCommand("qpdf", ctx.options)
      end
    },
  },
  arguments = {
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
  }
}
