-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "upbibtex",
  name = "upBibTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The upBibTeX reference management software",
      command = function(ctx)
        return get_command("upbibtex", ctx.options, get_basename(ctx.reference.file_name))
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
