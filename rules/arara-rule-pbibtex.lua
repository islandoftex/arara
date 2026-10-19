-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "pbibtex",
  name = "pBibTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The pBibTeX reference management software",
      command = function(ctx)
        return get_command("pbibtex", ctx.options, get_basename(ctx.reference.file_name))
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
