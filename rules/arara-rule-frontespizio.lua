-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
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
        return getCommand(ctx.engine, ctx.interaction, ctx.shell, ctx.options, ctx.reference.fileName)
      end
    },
    {
      name = "The frontispiece",
      command = function(ctx)
        local base = getBasename(ctx.reference.fileName) .. "-frn"
        return getCommand(ctx.engine, ctx.interaction, ctx.shell, base)
      end
    },
    {
      name = "The DVIPS program",
      command = function(ctx)
        local base = getBasename(ctx.reference.fileName) .. "-frn"
        local eps = base .. ".eps"
        return isTrue(ctx.engine == "latex", getCommand("dvips", "-o", eps, base), "")
      end
    },
    {
      name = "The engine",
      command = function(ctx)
        return getCommand(ctx.engine, ctx.interaction, ctx.shell, ctx.options, ctx.reference.fileName)
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
          throwError("The provided engine is not valid")
        end
      end
    },
    {
      identifier = "shell",
      flag = function(ctx, value)
        return isTrue(value, "--shell-escape", "--no-shell-escape")
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
    {
      identifier = "interaction",
      flag = function(ctx, value)
        if contains({ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" }, value) then
          return "--interaction=" .. value
        else
          throwError("The provided interaction value is not valid.")
        end
      end
    },
  }
}
