-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "metapost",
  name = "Metapost",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "Metapost",
      command = function(ctx)
        return getCommand("mpost", ctx.interaction, ctx.numbersystem, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "interaction",
      flag = function(ctx, value)
        if contains({ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" }, value) then
          return "-interaction=" .. value
        else
          throwError("The provided interaction value is not valid.")
        end
      end
    },
    {
      identifier = "numbersystem",
      flag = function(ctx, value)
        if contains({ "scaled", "double", "binary", "decimal" }, value) then
          return "-numbersystem=" .. value
        else
          throwError("The provided numbersystem value is not valid.")
        end
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
