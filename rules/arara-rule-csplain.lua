-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "csplain",
  name = "CSplain",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "CSplain engine",
      command = function(ctx)
        return getCommand("csplain", ctx.interaction, ctx.draft, ctx.shell, ctx.synctex, ctx.options, ctx.reference.fileName)
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
