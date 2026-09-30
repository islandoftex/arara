-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "nomencl",
  name = "Nomencl",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The Nomenclature software",
      command = function(ctx)
        local nlo = get_basename(ctx.reference.file_name) .. ".nlo"
        local nls = get_basename(ctx.reference.file_name) .. ".nls"
        return get_command("makeindex", ctx.options, nlo, ctx.style, "-o", nls)
      end
    },
  },
  arguments = {
    {
      identifier = "style",
      default = "",
      flag = function(ctx, value)
        return { "-s", is_empty(value, "nomencl.ist", value) }
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
