-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "biber",
  name = "Biber",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The Biber reference management software",
      command = function(ctx)
        if isTrue(ctx.tool[1]) then
          return getCommand("biber", "--tool", ctx.options)
        else
          return getCommand("biber", ctx.options, getBasename(ctx.reference.fileName))
        end
      end
    },
  },
  arguments = {
    {
      identifier = "tool",
      default = "False",
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
