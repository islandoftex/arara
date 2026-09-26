-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "llmk",
  name = "LLMK",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The automation tool",
      command = function(ctx)
        return getCommand("llmk", ctx.clean, ctx.debug, ctx.dry, ctx.mode, ctx.reference.fileName)
      end
    },
  },
  arguments = {
    {
      identifier = "clean",
      flag = function(ctx, value)
        local goals = { ["partial"] = "--clean", ["all"] = "--clobber" }
        if goals[value] ~= nil then
          return goals[value]
        else
          throwError("The provided value is invalid.")
        end
      end
    },
    {
      identifier = "debug",
      flag = function(ctx, value)
        if value == "all" then
          return "-D"
        else
          return { "-d", value }
        end
      end
    },
    {
      identifier = "dry",
      flag = function(ctx, value)
        return isTrue(value, "--dry-run")
      end
    },
    {
      identifier = "mode",
      flag = function(ctx, value)
        local modes = { ["quiet"] = "-q", ["silent"] = "-s", ["verbose"] = "-v" }
        if modes[value] ~= nil then
          return modes[value]
        else
          throwError("Invalid mode name.")
        end
      end
    },
  }
}
