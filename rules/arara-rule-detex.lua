-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "detex",
  name = "DeTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The DeTeX program",
      command = function(ctx)
        local c = getCommand("detex", ctx.references, ctx.mode, ctx.follow, ctx.math, ctx.spaces, ctx.words, ctx.environments, ctx.reference.fileName)
        if isEmpty(ctx.output) then
          return c
        else
          local p = unsafelyExecuteSystemCommand(c)
          if p.first == 0 then
            return writeToFile(ctx.output[1], p.second, false)
          else
            return false
          end
        end
      end
    },
  },
  arguments = {
    {
      identifier = "references",
      flag = function(ctx, value)
        return isTrue(value, "-c")
      end
    },
    {
      identifier = "mode",
      flag = function(ctx, value)
        local modes = { ["latex"] = "-l", ["tex"] = "-t" }
        if modes[value] ~= nil then
          return modes[value]
        else
          throwError("I was expecting either the TeX or LaTeX mode.")
        end
      end
    },
    {
      identifier = "follow",
      flag = function(ctx, value)
        return isFalse(value, "-n")
      end
    },
    {
      identifier = "math",
      flag = function(ctx, value)
        return isFalse(value, "-r")
      end
    },
    {
      identifier = "spaces",
      flag = function(ctx, value)
        return isTrue(value, "-s")
      end
    },
    {
      identifier = "words",
      flag = function(ctx, value)
        return isTrue(value, "-w")
      end
    },
    {
      identifier = "environments",
      flag = function(ctx, value)
        if not isList(value) then
          throwError("I was expecting a list of environments.")
        else
          return { "-e", join(value, ",") }
        end
      end
    },
    {
      identifier = "output",
      flag = function(ctx, value)
        return value
      end
    },
  }
}
