-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "mkdir",
  name = "Create subdirectory",
  authors = { "hackbaellchen", "Island of TeX" },
  commands = {
    {
      name = "Create subdirectory",
      command = function(ctx)
        createDirectories(ctx.target[1])
        return true
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
