-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "tikzmake",
  name = "TikZmake",
  authors = { "Robbie Smith", "Island of TeX" },
  commands = {
    {
      name = "TikZ list-and-make engine",
      command = function(ctx)
        local makefile = get_basename(ctx.reference.file_name) .. ".makefile"
        return get_command("make", ctx.force, ctx.jobs, ctx.options, "-f", makefile)
      end
    },
  },
  arguments = {
    {
      identifier = "force",
      flag = function(ctx, value)
        return is_true(value, "--always-make")
      end
    },
    {
      identifier = "jobs",
      flag = function(ctx, value)
        return "-j" .. value
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
