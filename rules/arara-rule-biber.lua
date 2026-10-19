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
        if is_true(ctx.tool[1]) then
          return get_command("biber", "--tool", ctx.options)
        else
          return get_command("biber", ctx.options, get_basename(ctx.reference.file_name))
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
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
  }
}
