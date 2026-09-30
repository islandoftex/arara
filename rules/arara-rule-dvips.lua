-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "dvips",
  name = "DVIPS",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The DVIPS program",
      command = function(ctx)
        local base = get_basename(ctx.reference.file_name) .. ".dvi"
        local out = get_basename(ctx.output[1]) .. ".ps"
        return get_command("dvips", base, "-o", out, ctx.options)
      end
    },
  },
  arguments = {
    {
      identifier = "output",
      default = "",
      flag = function(ctx, value)
        return is_empty(value, ctx.reference.file_name, value)
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
