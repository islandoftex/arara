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
        return get_command("mpost", ctx.interaction, ctx.numbersystem, ctx.options, ctx.reference.file_name)
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
          throw_error("The provided interaction value is not valid.")
        end
      end
    },
    {
      identifier = "numbersystem",
      flag = function(ctx, value)
        if contains({ "scaled", "double", "binary", "decimal" }, value) then
          return "-numbersystem=" .. value
        else
          throw_error("The provided numbersystem value is not valid.")
        end
      end
    },
    {
      identifier = "options",
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
  }
}
