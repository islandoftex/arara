-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
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
        return get_command("tex-fmt", ctx.check, ctx.print, ctx.messages, ctx.tabs, ctx.stdin, ctx.config, ctx.location, ctx.wrap, ctx.length, ctx.tabsize, ctx.options, ctx.reference.path)
      end
    },
  },
  arguments = {
    {
      identifier = "check",
      flag = function(ctx, value)
        return is_true(value, "--check")
      end
    },
    {
      identifier = "print",
      flag = function(ctx, value)
        return is_true(value, "--print")
      end
    },
    {
      identifier = "messages",
      flag = function(ctx, value)
        if contains({ "verbose", "quiet", "trace" }, value) then
          return "--" .. value
        else
          throw_error("You provided an invalid value for messages.")
        end
      end
    },
    {
      identifier = "tabs",
      flag = function(ctx, value)
        return is_true(value, "--usetabs")
      end
    },
    {
      identifier = "stdin",
      flag = function(ctx, value)
        return is_true(value, "--stdin")
      end
    },
    {
      identifier = "location",
      flag = function(ctx, value)
        local check = ctx.parameters["config"] ~= nil
        if check and is_true(ctx.parameters.config) then
          return { "--config", value }
        else
          throw_error("This key requires config to be enabled.")
        end
      end
    },
    {
      identifier = "config",
      flag = function(ctx, value)
        return is_false(value, "--noconfig")
      end
    },
    {
      identifier = "wrap",
      flag = function(ctx, value)
        return is_false(value, "--nowrap")
      end
    },
    {
      identifier = "length",
      flag = function(ctx, value)
        local check = ctx.parameters["wrap"] ~= nil
        local wrap = check and ctx.parameters.wrap or true
        if is_true(wrap) then
          return { "--wraplen", value }
        else
          throw_error("This key cannot be used when wrap is disabled.")
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
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of options.")
        end
      end
    },
  }
}
