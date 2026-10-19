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
        if is_unix() then
          prefix = { "mv", "-f" }
        else
          prefix = { "cmd", "/c", "move", "/Y" }
        end
        if get_original_reference() == ctx.reference.path then
          throw_error("I cannot move the main file reference.")
        end
        local t = is_list(ctx.target) and ctx.target[1] or ctx.target
        local f = normalize_path(t)
        if get_original_reference() == f then
          throw_error("I cannot overwrite the main file reference.")
        end
        return get_command(prefix, ctx.reference.path, f)
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
