-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "frontespizio",
  name = "Frontespizio",
  authors = { "Francesco Endrici", "Enrico Gregorio", "Island of TeX" },
  commands = {
    {
      name = "The engine",
      command = function(ctx)
        return get_command(ctx.engine, ctx.interaction, ctx.shell, ctx.options, ctx.reference.file_name)
      end
    },
    {
      name = "The frontispiece",
      command = function(ctx)
        local base = get_basename(ctx.reference.file_name) .. "-frn"
        return get_command(ctx.engine, ctx.interaction, ctx.shell, base)
      end
    },
    {
      name = "The DVIPS program",
      command = function(ctx)
        local base = get_basename(ctx.reference.file_name) .. "-frn"
        local eps = base .. ".eps"
        return is_true(ctx.engine == "latex", get_command("dvips", "-o", eps, base), "")
      end
    },
    {
      name = "The engine",
      command = function(ctx)
        return get_command(ctx.engine, ctx.interaction, ctx.shell, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "engine",
      default = "pdflatex",
      flag = function(ctx, value)
        if contains({ "pdflatex", "latex", "xelatex", "lualatex" }, value) then
          return value
        else
          throw_error("The provided engine is not valid")
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
      identifier = "options",
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
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
  }
}
