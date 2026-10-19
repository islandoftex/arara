-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "pdftocairo",
  name = "pdftocairo",
  authors = { "samcarter" },
  commands = {
    {
      name = "The pdftocairo program",
      command = function(ctx)
        return get_command("pdftocairo", "-" .. ctx.format[1], get_basename(ctx.reference.file_name) .. ".pdf", ctx.options, ctx.output)
      end
    },
  },
  arguments = {
    {
      identifier = "format",
      default = "png",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "output",
      flag = function(ctx, value)
        return value
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
