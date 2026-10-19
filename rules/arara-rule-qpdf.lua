-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "qpdf",
  name = "QPDF",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The QPDF program",
      command = function(ctx)
        return get_command("qpdf", ctx.options)
      end
    },
  },
  arguments = {
    {
      identifier = "options",
      required = true,
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
