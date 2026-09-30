-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "pdftk",
  name = "PDFtk",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "PDFtk",
      command = function(ctx)
        local input = get_basename(ctx.reference.file_name) .. ".pdf"
        return get_command("pdftk", input, ctx.options)
      end
    },
  },
  arguments = {
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
