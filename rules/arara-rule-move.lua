-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "move",
  name = "Move",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The OS move call",
      command = function(ctx)
        local prefix = {}
        if isUnix() then
          prefix = { "mv", "-f" }
        else
          prefix = { "cmd", "/c", "move", "/Y" }
        end
        if getOriginalReference() == ctx.reference.path then
          throwError("I cannot move the main file reference.")
        end
        local t = isList(ctx.target) and ctx.target[1] or ctx.target
        local f = normalizePath(t)
        if getOriginalReference() == f then
          throwError("I cannot overwrite the main file reference.")
        end
        return getCommand(prefix, ctx.reference.path, f)
      end
    },
  },
  arguments = {
    {
      identifier = "target",
      required = true,
      flag = function(ctx, value)
        return value
      end
    },
  }
}
