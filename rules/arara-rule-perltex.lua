-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "perltex",
  name = "PerlTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "PerlTeX wrapper",
      command = function(ctx)
        return get_command("perltex", ctx.engine, ctx.safe, ctx.permit, ctx.standalone, ctx.interaction, ctx.shell, ctx.synctex, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "engine",
      flag = function(ctx, value)
        if contains({ "latex", "pdflatex", "xelatex", "lualatex", "tex", "pdftex", "luatex", "context" }, value) then
          return "--latex=" .. value
        else
          throw_error("The provided TeX engine is invalid.")
        end
      end
    },
    {
      identifier = "safe",
      flag = function(ctx, value)
        return is_true(value, "--safe", "--nosafe")
      end
    },
    {
      identifier = "permit",
      flag = function(ctx, value)
        if is_list(value) then
          local p = {}
          for _, entry in ipairs(value) do
            table.insert(p, "--permit=" .. entry)
          end
          return p
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "standalone",
      flag = function(ctx, value)
        return is_true(value, "--makesty")
      end
    },
    {
      identifier = "interaction",
      flag = function(ctx, value)
        if contains({ "batchmode", "nonstopmode", "scrollmode", "errorstopmode" }, value) then
          return "--interaction=" .. value
        else
          throw_error("The provided interaction value is not valid.")
        end
      end
    },
    {
      identifier = "shell",
      flag = function(ctx, value)
        return is_true(value, "--shell-escape", "--no-shell-escape")
      end
    },
    {
      identifier = "synctex",
      flag = function(ctx, value)
        return is_true(value, "--synctex=1", "--synctex=0")
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
