-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "ptex",
  name = "pTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "pTeX engine",
      command = function(ctx)
        return get_command("ptex", ctx.interaction, ctx.shell, ctx.synctex, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "interaction",
      flag = function(ctx, value)
        if contains({ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" }, value) then
          return "--interaction=" .. value
        else
          throw_error("The provided interaction value is not valid.")
        end
      end
    },
    {
      identifier = "shell",
      flag = function(ctx, value)
        return is_true(value, "--shell-escape", "--no-shell-escape")
      end
    },
    {
      identifier = "synctex",
      flag = function(ctx, value)
        return is_true(value, "--synctex=1", "--synctex=0")
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
