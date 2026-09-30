-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "detex",
  name = "DeTeX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The DeTeX program",
      command = function(ctx)
        local c = get_command("detex", ctx.references, ctx.mode, ctx.follow, ctx.math, ctx.spaces, ctx.words, ctx.environments, ctx.reference.file_name)
        if is_empty(ctx.output) then
          return c
        else
          local p = unsafely_execute_system_command(c)
          if p.first == 0 then
            return write_to_file(ctx.output[1], p.second, false)
          else
            return false
          end
        end
      end
    },
  },
  arguments = {
    {
      identifier = "references",
      flag = function(ctx, value)
        return is_true(value, "-c")
      end
    },
    {
      identifier = "mode",
      flag = function(ctx, value)
        local modes = { ["latex"] = "-l", ["tex"] = "-t" }
        if modes[value] ~= nil then
          return modes[value]
        else
          throw_error("I was expecting either the TeX or LaTeX mode.")
        end
      end
    },
    {
      identifier = "follow",
      flag = function(ctx, value)
        return is_false(value, "-n")
      end
    },
    {
      identifier = "math",
      flag = function(ctx, value)
        return is_false(value, "-r")
      end
    },
    {
      identifier = "spaces",
      flag = function(ctx, value)
        return is_true(value, "-s")
      end
    },
    {
      identifier = "words",
      flag = function(ctx, value)
        return is_true(value, "-w")
      end
    },
    {
      identifier = "environments",
      flag = function(ctx, value)
        if not is_list(value) then
          throw_error("I was expecting a list of environments.")
        else
          return { "-e", join(value, ",") }
        end
      end
    },
    {
      identifier = "output",
      flag = function(ctx, value)
        return value
      end
    },
  }
}
