-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "ltx2any",
  name = "LTX2any",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The automation tool",
      command = function(ctx)
        return getCommand("ltx2any", ctx.clean, ctx.engine, ctx.parameters, ctx.tikzimages, ctx.jobname, ctx.logname, ctx.logformat, ctx.loglevel, ctx.frequency, ctx.directory, ctx.synctex, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "clean",
      flag = function(ctx, value)
        return isTrue(value, "-c")
      end
    },
    {
      identifier = "engine",
      flag = function(ctx, value)
        return { "-e", value }
      end
    },
    {
      identifier = "parameters",
      flag = function(ctx, value)
        if isList(value) then
          return { "-ep", join(value, " ") }
        else
          throwError("I was execting a list of parameters.")
        end
      end
    },
    {
      identifier = "tikzimages",
      flag = function(ctx, value)
        if isList(value) then
          return { "-ir", join(value, ":") }
        else
          throwError("I was expecting a list of images.")
        end
      end
    },
    {
      identifier = "jobname",
      flag = function(ctx, value)
        return { "-j", value }
      end
    },
    {
      identifier = "logname",
      flag = function(ctx, value)
        return { "-l", value }
      end
    },
    {
      identifier = "logformat",
      flag = function(ctx, value)
        local fts = { ["raw"] = "raw", ["markdown"] = "md", ["pdf"] = "pdf" }
        if fts[value] ~= nil then
          return { "-lf", fts[value] }
        else
          throwError("Invalid log format.")
        end
      end
    },
    {
      identifier = "loglevel",
      flag = function(ctx, value)
        local ll = { "error", "warning", "info" }
        if contains(ll, value) then
          return { "-ll", value }
        else
          throwError("Invalid log level.")
        end
      end
    },
    {
      identifier = "frequency",
      flag = function(ctx, value)
        return { "-n", value }
      end
    },
    {
      identifier = "directory",
      flag = function(ctx, value)
        return { "-t", value }
      end
    },
    {
      identifier = "synctex",
      flag = function(ctx, value)
        return isTrue(value, "-synctex")
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
