-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "sketch",
  name = "Sketch",
  authors = { "Sergey Ulyanov", "Island of TeX" },
  commands = {
    {
      name = "The Sketch software",
      command = function(ctx)
        local output = get_basename(ctx.reference.file_name) .. ".tex"
        return get_command("sketch", ctx.options, ctx.reference.file_name, "-o", output)
      end
    },
  },
  arguments = {
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
