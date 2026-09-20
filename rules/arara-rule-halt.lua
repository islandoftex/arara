-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "halt",
  name = "Halt",
  authors = { "Heiko Oberdiek", "Island of TeX" },
  commands = {
    {
      name = "The halt trigger",
      command = function(ctx)
        halt(0)
        return true
      end
    },
  },
  arguments = {}
}
