-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "spix",
  name = "SpiX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The automation tool",
      command = function(ctx)
        return getCommand("spix", ctx.dry, ctx.options, ctx.reference.fileName)
      end
    },
  },
  arguments = {
    {
      identifier = "dry",
      flag = function(ctx, value)
        return isTrue(value, "--dry-run")
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
