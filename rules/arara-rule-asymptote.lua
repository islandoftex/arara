-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "asymptote",
  name = "Asymptote",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "Asymptote",
      command = function(ctx)
        return get_command("asy", ctx.color, ctx.engine, ctx.twice, ctx.format, ctx.output, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "color",
      flag = function(ctx, value)
        if contains({ "bw", "cmyk", "rgb", "gray" }, value) then
          return "-" .. value
        else
          throw_error("The provided color enforcement is not valid.")
        end
      end
    },
    {
      identifier = "engine",
      default = "latex",
      flag = function(ctx, value)
        if contains({ "latex", "pdflatex", "xelatex", "lualatex", "tex", "pdftex", "luatex", "context", "none" }, value) then
          return { "-tex", value }
        else
          throw_error("The provided TeX engine is invalid.")
        end
      end
    },
    {
      identifier = "twice",
      flag = function(ctx, value)
        return is_true(value, "-twice")
      end
    },
    {
      identifier = "format",
      flag = function(ctx, value)
        return { "-f", value }
      end
    },
    {
      identifier = "output",
      flag = function(ctx, value)
        return { "-o", value }
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
