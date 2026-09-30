-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "latexmk",
  name = "LaTeXmk",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "Tool LaTeXmk",
      command = function(ctx)
        if not is_empty(ctx.clean) then
          return get_command("latexmk", ctx.clean, ctx.options)
        else
          return get_command("latexmk", is_true(is_empty(ctx.program), ctx.engine, ctx.program), ctx.options, ctx.reference.file_name)
        end
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
    {
      identifier = "clean",
      flag = function(ctx, value)
        if contains({ "all", "partial" }, value) then
          return is_true(value == "all", "-C", "-c")
        else
          throw_error("The provided clean value is not valid.")
        end
      end
    },
    {
      identifier = "engine",
      flag = function(ctx, value)
        if contains({ "latex", "pdflatex", "xelatex", "lualatex" }, value) then
          local flags = { ["latex"] = "-dvi", ["pdflatex"] = "-pdf", ["xelatex"] = "-xelatex", ["lualatex"] = "-lualatex", ["latex-dev"] = { "-dvi", "-e", "$latex='latex-dev'" }, ["pdflatex-dev"] = { "-pdf", "-e", "$pdflatex='pdflatex-dev'" }, ["xelatex-dev"] = { "-xelatex", "-e", "$xelatex='xelatex-dev'" }, ["lualatex-dev"] = { "-lualatex", "-e", "$lualatex='lualatex-dev'" } }
          return flags[value]
        else
          throw_error("The provided engine value is not valid.")
        end
      end
    },
    {
      identifier = "program",
      flag = function(ctx, value)
        return "-latex=" .. value
      end
    },
  }
}
