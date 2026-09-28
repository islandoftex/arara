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
        local nlo = getBasename(ctx.reference.file_name) .. ".nlo"
        local nls = getBasename(ctx.reference.file_name) .. ".nls"
        return getCommand("makeindex", ctx.options, nlo, ctx.style, "-o", nls)
      end
    },
  },
  arguments = {
    {
      identifier = "style",
      default = "",
      flag = function(ctx, value)
        return { "-s", isEmpty(value, "nomencl.ist", value) }
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
