-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "ps2pdf",
  name = "PS2PDF",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The PS2PDF program",
      command = function(ctx)
        local infile = get_basename(ctx.reference.file_name) .. ".ps"
        local outfile = get_basename(ctx.output[1]) .. ".pdf"
        return get_command("ps2pdf", ctx.options, infile, outfile)
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
