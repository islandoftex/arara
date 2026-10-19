-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "context",
  name = "ConTeXt",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "ConTeXt engine",
      command = function(ctx)
        return get_command("context", ctx.make, ctx.ctx, ctx.interface, ctx.autopdf, ctx.purge, ctx.modules, ctx.environments, ctx.mode, ctx.path, ctx.arguments, ctx.trackers, ctx.directives, ctx.silent, ctx.errors, ctx.interaction, ctx.synctex, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "make",
      flag = function(ctx, value)
        return is_true(value, "--make")
      end
    },
    {
      identifier = "ctx",
      flag = function(ctx, value)
        return "--ctx=" .. value
      end
    },
    {
      identifier = "interface",
      flag = function(ctx, value)
        return { "--interface", value }
      end
    },
    {
      identifier = "autopdf",
      flag = function(ctx, value)
        return is_true(value, "--autopdf")
      end
    },
    {
      identifier = "purge",
      flag = function(ctx, value)
        local p = { ["partial"] = "--purge", ["all"] = "--purgeall", ["result"] = "--purgeresult" }
        if p[value] ~= nil then
          return p[value]
        else
          throw_error("Invalid value, expected partial, result or all.")
        end
      end
    },
    {
      identifier = "modules",
      flag = function(ctx, value)
        if is_list(value) then
          return "--usemodule=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "environments",
      flag = function(ctx, value)
        if is_list(value) then
          return "--environment=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "mode",
      flag = function(ctx, value)
        if is_list(value) then
          return "--mode=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "path",
      flag = function(ctx, value)
        if is_list(value) then
          return "--path=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "arguments",
      flag = function(ctx, value)
        if is_list(value) then
          return "--arguments=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "trackers",
      flag = function(ctx, value)
        if is_list(value) then
          return "--trackers=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "directives",
      flag = function(ctx, value)
        if is_list(value) then
          return "--directives=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "silent",
      flag = function(ctx, value)
        if is_list(ctx.parameters.modules) then
          return "--silent=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "errors",
      flag = function(ctx, value)
        if is_list(value) then
          return "--errors=" .. join(value, ",")
        else
          throw_error("I was expecting a list.")
        end
      end
    },
    {
      identifier = "interaction",
      flag = function(ctx, value)
        if contains({ "batchmode", "nonstopmode" }, value) then
          return "--" .. value
        else
          throw_error("The provided interaction value is not valid.")
        end
      end
    },
    {
      identifier = "synctex",
      flag = function(ctx, value)
        return is_true(value, "--synctex", "--nosynctex")
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
