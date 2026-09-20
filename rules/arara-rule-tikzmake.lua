-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "tikzmake",
  name = "TikZmake",
  authors = { "Robbie Smith", "Island of TeX" },
  commands = {
    {
      name = "TikZ list-and-make engine",
      command = function(ctx)
        local makefile = getBasename(ctx.reference.fileName) .. ".makefile"
        return getCommand("make", ctx.force, ctx.jobs, ctx.options, "-f", makefile)
      end
    },
  },
  arguments = {
    {
      identifier = "force",
      flag = function(ctx, value)
        return isTrue(value, "--always-make")
      end
    },
    {
      identifier = "jobs",
      flag = function(ctx, value)
        return "-j" .. value
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
