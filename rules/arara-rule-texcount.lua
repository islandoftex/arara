-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "texcount",
  name = "TeXcount",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The TeXcount program",
      command = function(ctx)
        return getCommand("texcount", ctx.rules, ctx.verbosity, ctx.strict, ctx.html, ctx.total, ctx.unicode, ctx.options, ctx.output, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "rules",
      flag = function(ctx, value)
        if contains({ "relaxed", "restricted" }, value) then
          return "-" .. value
        else
          throwError("Invalid value, use either relaxed or restricted.")
        end
      end
    },
    {
      identifier = "verbosity",
      flag = function(ctx, value)
        if contains({ "0", "1", "2", "3", "4" }, value) then
          return "-v" .. value
        else
          throwError("Invalid value, use an integer range from 0 to 4.")
        end
      end
    },
    {
      identifier = "strict",
      flag = function(ctx, value)
        return isTrue(value, "-strict")
      end
    },
    {
      identifier = "html",
      flag = function(ctx, value)
        return isTrue(value, "-html")
      end
    },
    {
      identifier = "total",
      flag = function(ctx, value)
        return isTrue(value, "-total")
      end
    },
    {
      identifier = "unicode",
      flag = function(ctx, value)
        return isTrue(value, "-unicode")
      end
    },
    {
      identifier = "output",
      flag = function(ctx, value)
        return "-out=" .. value
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
