-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "sketch",
  name = "Sketch",
  authors = { "Sergey Ulyanov", "Island of TeX" },
  commands = {
    {
      name = "The Sketch software",
      command = function(ctx)
        local output = getBasename(ctx.reference.fileName) .. ".tex"
        return getCommand("sketch", ctx.options, ctx.reference.fileName, "-o", output)
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
