-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "bibtexu",
  name = "BibTeXu",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "An 8-bit implementation of BibTeX 0.99 with a very large capacity",
      command = function(ctx)
        return get_command("bibtexu", ctx.options, get_basename(ctx.reference.file_name))
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
