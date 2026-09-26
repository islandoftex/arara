-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "convert",
  name = "Convert",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The Convert program",
      command = function(ctx)
        return getCommand(ctx.program, ctx.options)
      end
    },
  },
  arguments = {
    {
      identifier = "program",
      default = "magick",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "options",
      required = true,
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
