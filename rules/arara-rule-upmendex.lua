-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "upmendex",
  name = "UpMendex",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The UpMendex index software",
      command = function(ctx)
        local base = get_basename(ctx.reference.file_name)
        local infile = (base .. ".") .. ctx.input[1]
        local outfile = { "-o", (base .. ".") .. ctx.output[1] }
        local logfile = { "-t", (base .. ".") .. ctx.log[1] }
        return get_command("upmendex", ctx.compress, ctx.style, ctx.dictionary, ctx.order, ctx.options, logfile, infile, outfile)
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
      identifier = "compress",
      flag = function(ctx, value)
        return is_true(value, "-c")
      end
    },
    {
      identifier = "order",
      flag = function(ctx, value)
        if contains({ "letter", "word" }, value) then
          return is_true(value == "letter", "-l", "")
        else
          throw_error("The provided order is invalid.")
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
      identifier = "dictionary",
      flag = function(ctx, value)
        return { "-d", value }
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
