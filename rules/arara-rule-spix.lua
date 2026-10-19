-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
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
        return get_command("spix", ctx.dry, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "dry",
      flag = function(ctx, value)
        return is_true(value, "--dry-run")
      end
    },
    {
      identifier = "options",
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
  }
}
