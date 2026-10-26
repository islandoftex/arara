-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "mptopdf",
  name = "MPTOPDF",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "MetaPost to PDF",
      command = function(ctx)
        return get_command("mptopdf", ctx.metafun, ctx.texexec, ctx.latex,
          ctx.options, ctx.reference.file_name)
      end
    },
  },
  arguments = {
    {
      identifier = "metafun",
      flag = function(ctx, value)
        return is_true(value, "--metafun")
      end
    },
    {
      identifier = "texexec",
      flag = function(ctx, value)
        return is_true(value, "--texexec")
      end
    },
    {
      identifier = "latex",
      flag = function(ctx, value)
        return is_true(value, "--latex")
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
