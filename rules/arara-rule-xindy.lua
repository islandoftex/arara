-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "xindy",
  name = "Xindy",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The Xindy software",
      command = function(ctx)
        local base = getBasename(ctx.reference.file_name)
        local infile = (base .. ".") .. ctx.input[1]
        local outfile = { "-o", (base .. ".") .. ctx.output[1] }
        local logfile = { "-t", (base .. ".") .. ctx.log[1] }
        return getCommand("xindy", ctx.quiet, ctx.markup, ctx.modules, ctx.codepage, ctx.language, logfile, outfile, ctx.options, infile)
      end
    },
  },
  arguments = {
    {
      identifier = "quiet",
      flag = function(ctx, value)
        return isTrue(value, "-q")
      end
    },
    {
      identifier = "modules",
      flag = function(ctx, value)
        local elements = {}
        if isList(value) then
          for _, module in ipairs(value) do
            table.insert(elements, "-M")
            table.insert(elements, module)
          end
          return elements
        else
          throwError("I was expecting a list of options.")
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
        if contains({ "latex", "xelatex", "omega", "xindy" }, value) then
          return { "-I", value }
        else
          throwError("The provided markup is invalid.")
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
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of options.")
        end
      end
    },
  }
}
