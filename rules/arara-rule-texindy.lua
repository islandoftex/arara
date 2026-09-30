-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "texindy",
  name = "TeXindy",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The TeXindy software",
      command = function(ctx)
        local base = get_basename(ctx.reference.file_name)
        local infile = (base .. ".") .. ctx.input[1]
        local outfile = { "-o", (base .. ".") .. ctx.output[1] }
        local logfile = { "-t", (base .. ".") .. ctx.log[1] }
        return get_command("texindy", ctx.quiet, ctx.markup, ctx.modules, ctx.codepage, ctx.language, logfile, outfile, ctx.options, infile)
      end
    },
  },
  arguments = {
    {
      identifier = "quiet",
      flag = function(ctx, value)
        return is_true(value, "-q")
      end
    },
    {
      identifier = "modules",
      flag = function(ctx, value)
        local elements = {}
        if is_list(value) then
          for _, module in ipairs(value) do
            table.insert(elements, "-M")
            table.insert(elements, module)
          end
          return elements
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
    {
      identifier = "codepage",
      flag = function(ctx, value)
        return { "-C", value }
      end
    },
    {
      identifier = "language",
      flag = function(ctx, value)
        return { "-L", value }
      end
    },
    {
      identifier = "markup",
      flag = function(ctx, value)
        if contains({ "latex", "xelatex", "omega" }, value) then
          return { "-I", value }
        else
          throw_error("The provided markup is invalid.")
        end
      end
    },
    {
      identifier = "input",
      default = "idx",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "output",
      default = "ind",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "log",
      default = "ilg",
      flag = function(ctx, value)
        return value
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
