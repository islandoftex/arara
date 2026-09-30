-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "pythontex",
  name = "PythonTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "PythonTeX wrapper",
      command = function(ctx)
        return get_command("pythontex", ctx.encoding, ctx.errorcode, ctx.runall, ctx.rerun, ctx.hashdependencies, ctx.jobs, ctx.verbose, ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "encoding",
      flag = function(ctx, value)
        return "--encoding=" .. value
      end
    },
    {
      identifier = "errorcode",
      flag = function(ctx, value)
        return "--error-exit-code=" .. is_true(value, "true", "false")
      end
    },
    {
      identifier = "runall",
      flag = function(ctx, value)
        return "--runall=" .. is_true(value, "true", "false")
      end
    },
    {
      identifier = "rerun",
      flag = function(ctx, value)
        if contains({ "never", "modified", "errors", "warnings", "always" }, value) then
          return "--rerun=" .. value
        else
          throw_error("The provided value is not valid.")
        end
      end
    },
    {
      identifier = "hashdependencies",
      flag = function(ctx, value)
        return "--hashdependencies=" .. is_true(value, "true", "false")
      end
    },
    {
      identifier = "jobs",
      flag = function(ctx, value)
        return { "--jobs", value }
      end
    },
    {
      identifier = "verbose",
      flag = function(ctx, value)
        return is_true(value, "--verbose")
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
