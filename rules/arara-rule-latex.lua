-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "latex",
  name = "LaTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "LaTeX engine",
      command = function(ctx)
        return getCommand(ctx.branch, ctx.interaction, ctx.draft, ctx.shell, ctx.synctex, ctx.options, ctx.reference.fileName)
      end
    },
  },
  arguments = {
    {
      identifier = "branch",
      default = "stable",
      flag = function(ctx, value)
        local engines = { ["stable"] = "latex", ["developer"] = "latex-dev" }
        if engines[value] ~= nil then
          return engines[value]
        else
          throwError("The valid branch values are: " .. join(keys(engines), ", "))
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
    {
      identifier = "shell",
      flag = function(ctx, value)
        return isTrue(value, "--shell-escape", "--no-shell-escape")
      end
    },
    {
      identifier = "synctex",
      flag = function(ctx, value)
        return isTrue(value, "--synctex=1", "--synctex=0")
      end
    },
    {
      identifier = "draft",
      flag = function(ctx, value)
        return isTrue(value, "--draftmode")
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
