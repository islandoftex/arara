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
        if not isEmpty(ctx.clean) then
          return getCommand("latexmk", ctx.clean, ctx.options)
        else
          return getCommand("latexmk", isTrue(isEmpty(ctx.program), ctx.engine, ctx.program), ctx.options, ctx.reference.fileName)
        end
      end
    },
  },
  arguments = {
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
    {
      identifier = "clean",
      flag = function(ctx, value)
        if contains({ "all", "partial" }, value) then
          return isTrue(value == "all", "-C", "-c")
        else
          throwError("The provided clean value is not valid.")
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
          throwError("The provided engine value is not valid.")
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
