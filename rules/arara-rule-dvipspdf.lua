-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "dvipspdf",
  name = "DVIPSPDF",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The DVIPS program",
      command = function(ctx)
        local base = get_basename(ctx.reference.file_name) .. ".dvi"
        local out = get_basename(ctx.reference.file_name) .. ".ps"
        return get_command("dvips", base, "-o", out, ctx.options1)
      end
    },
    {
      name = "The PS2PDF program",
      command = function(ctx)
        local base = get_basename(ctx.reference.file_name) .. ".ps"
        local out = get_basename(ctx.output[1]) .. ".pdf"
        return get_command("ps2pdf", ctx.options2, base, "-o", out)
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
      identifier = "options1",
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
    {
      identifier = "options2",
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
