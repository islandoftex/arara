-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "texfmt",
  name = "tex-fmt",
  authors = { "William Underwood", "Island of TeX" },
  commands = {
    {
      name = "The tex-fmt application",
      command = function(ctx)
        return getCommand("tex-fmt", ctx.check, ctx.print, ctx.messages, ctx.tabs, ctx.stdin, ctx.config, ctx.location, ctx.wrap, ctx.length, ctx.tabsize, ctx.options, ctx.reference.path)
      end
    },
  },
  arguments = {
    {
      identifier = "check",
      flag = function(ctx, value)
        return isTrue(value, "--check")
      end
    },
    {
      identifier = "print",
      flag = function(ctx, value)
        return isTrue(value, "--print")
      end
    },
    {
      identifier = "messages",
      flag = function(ctx, value)
        if contains({ "verbose", "quiet", "trace" }, value) then
          return "--" .. value
        else
          throwError("You provided an invalid value for messages.")
        end
      end
    },
    {
      identifier = "tabs",
      flag = function(ctx, value)
        return isTrue(value, "--usetabs")
      end
    },
    {
      identifier = "stdin",
      flag = function(ctx, value)
        return isTrue(value, "--stdin")
      end
    },
    {
      identifier = "location",
      flag = function(ctx, value)
        local check = ctx.parameters["config"] ~= nil
        if check and isTrue(ctx.parameters.config) then
          return { "--config", value }
        else
          throwError("This key requires config to be enabled.")
        end
      end
    },
    {
      identifier = "config",
      flag = function(ctx, value)
        return isFalse(value, "--noconfig")
      end
    },
    {
      identifier = "wrap",
      flag = function(ctx, value)
        return isFalse(value, "--nowrap")
      end
    },
    {
      identifier = "length",
      flag = function(ctx, value)
        local check = ctx.parameters["wrap"] ~= nil
        local wrap = check and ctx.parameters.wrap or true
        if isTrue(wrap) then
          return { "--wraplen", value }
        else
          throwError("This key cannot be used when wrap is disabled.")
        end
      end
    },
    {
      identifier = "tabsize",
      flag = function(ctx, value)
        return { "--tabsize", value }
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
