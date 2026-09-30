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
        return get_command("texcount", ctx.rules, ctx.verbosity, ctx.strict, ctx.html, ctx.total, ctx.unicode, ctx.options, ctx.output, ctx.reference.file_name)
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
          throw_error("Invalid value, use either relaxed or restricted.")
        end
      end
    },
    {
      identifier = "verbosity",
      flag = function(ctx, value)
        if contains({ "0", "1", "2", "3", "4" }, value) then
          return "-v" .. value
        else
          throw_error("Invalid value, use an integer range from 0 to 4.")
        end
      end
    },
    {
      identifier = "strict",
      flag = function(ctx, value)
        return is_true(value, "-strict")
      end
    },
    {
      identifier = "html",
      flag = function(ctx, value)
        return is_true(value, "-html")
      end
    },
    {
      identifier = "total",
      flag = function(ctx, value)
        return is_true(value, "-total")
      end
    },
    {
      identifier = "unicode",
      flag = function(ctx, value)
        return is_true(value, "-unicode")
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
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
  }
}
