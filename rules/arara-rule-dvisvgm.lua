-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "dvisvgm",
  name = "DVISVGM",
  authors = { "samcarter", "Pablo González" },
  commands = {
    {
      name = "The dvisvgm program",
      command = function(ctx)
        local base = (getBasename(ctx.reference.file_name) .. ".") .. ctx.entry[1]
        return getCommand("dvisvgm", ctx.entry[2], ctx.pages, ctx.options, base)
      end
    },
  },
  arguments = {
    {
      identifier = "pages",
      flag = function(ctx, value)
        if isList(value) then
          local q = {}
          for _, p in ipairs(value) do
            table.insert(q, tostring(p))
          end
          return { "--page=" .. join(q, ",") }
        else
          throwError("I was expecting a list of pages.")
        end
      end
    },
    {
      identifier = "entry",
      default = "dvi",
      flag = function(ctx, value)
        local formats = { ["dvi"] = { "dvi" }, ["xdv"] = { "xdv" }, ["pdf"] = { "pdf", "--pdf" }, ["eps"] = { "eps", "--eps" } }
        if formats[value] ~= nil then
          return formats[value]
        else
          throwError("The valid formats are: " .. join(keys(formats), ", "))
        end
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
