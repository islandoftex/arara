-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "makeindex",
  name = "MakeIndex",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The MakeIndex software",
      command = function(ctx)
        local base = getBasename(ctx.reference.file_name)
        local infile = (base .. ".") .. ctx.input[1]
        local outfile = { "-o", (base .. ".") .. ctx.output[1] }
        local logfile = { "-t", (base .. ".") .. ctx.log[1] }
        return getCommand("makeindex", ctx.german, ctx.style, ctx.order, ctx.options, logfile, infile, outfile)
      end
    },
  },
  arguments = {
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
      identifier = "german",
      flag = function(ctx, value)
        return isTrue(value, "-g")
      end
    },
    {
      identifier = "order",
      flag = function(ctx, value)
        if contains({ "letter", "word" }, value) then
          return isTrue(value == "letter", "-l", "")
        else
          throwError("The provided order is invalid.")
        end
      end
    },
    {
      identifier = "style",
      flag = function(ctx, value)
        return { "-s", value }
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
