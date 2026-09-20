-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "indent",
  name = "Indent",
  authors = { "Chris Hughes", "Island of TeX" },
  commands = {
    {
      name = "The latexindent.pl script",
      command = function(ctx)
        return getCommand("latexindent", ctx.silent, ctx.trace, ctx.screenlog, ctx.lines, ctx.settings, ctx.cruft, ctx.overwrite, ctx.output, ctx.reference.path, ctx.modifylinebreaks, ctx.check, ctx.replacement, ctx.options, ctx.logfile)
      end
    },
  },
  arguments = {
    {
      identifier = "silent",
      flag = function(ctx, value)
        return isTrue(value, "-s")
      end
    },
    {
      identifier = "overwrite",
      flag = function(ctx, value)
        return isTrue(value, "-w")
      end
    },
    {
      identifier = "trace",
      flag = function(ctx, value)
        if contains({ "default", "complete" }, value) then
          return isTrue(value == "default", "-t", "-tt")
        else
          throwError("You provided an invalid value for trace.")
        end
      end
    },
    {
      identifier = "screenlog",
      flag = function(ctx, value)
        return isTrue(value, "-sl")
      end
    },
    {
      identifier = "modifylinebreaks",
      flag = function(ctx, value)
        return isTrue(value, "-m")
      end
    },
    {
      identifier = "settings",
      flag = function(ctx, value)
        local check = ctx.parameters["where"] ~= nil
        local location = check and ctx.parameters.where or ""
        if contains({ "local", "onlydefault" }, value) then
          return isTrue(value == "local", isTrue(check, "-l=" .. location, "-l"), "-d")
        else
          throwError("You provided an invalid value for settings.")
        end
      end
    },
    {
      identifier = "cruft",
      flag = function(ctx, value)
        return "-c=" .. value
      end
    },
    {
      identifier = "logfile",
      flag = function(ctx, value)
        return { "-g", value }
      end
    },
    {
      identifier = "output",
      flag = function(ctx, value)
        return { "-o", value }
      end
    },
    {
      identifier = "lines",
      flag = function(ctx, value)
        return { "--lines", value }
      end
    },
    {
      identifier = "where",
      flag = function(ctx, value)
        local check = ctx.parameters["settings"] ~= nil
        local setting = check and ctx.parameters.settings or ""
        if setting ~= "local" then
          throwError("This key requires a local setting.")
        end
      end
    },
    {
      identifier = "replacement",
      flag = function(ctx, value)
        local opts = { ["full"] = "-r", ["noverb"] = "-rv", ["noindent"] = "-rr" }
        if opts[value] ~= nil then
          return opts[value]
        else
          throwError("You provided an invalid value for replacement.")
        end
      end
    },
    {
      identifier = "check",
      flag = function(ctx, value)
        local opts = { ["standard"] = "--check", ["verbose"] = "--checkv" }
        if opts[value] ~= nil then
          return opts[value]
        else
          throwError("You provided an invalid value for check.")
        end
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
