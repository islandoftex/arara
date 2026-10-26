-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "clean",
  name = "Clean",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "Cleaning feature",
      command = function(ctx)
        local prefix = {}
        if is_unix() then
          prefix = { "rm", "-f" }
        else
          prefix = { "cmd", "/c", "del" }
        end
        if is_empty(ctx.extensions) then
          if get_original_reference() == ctx.reference.path then
            throw_error("I cannot remove the main file reference.")
          end
          return get_command(prefix, ctx.reference.path)
        else
          local base = get_basename(ctx.reference.path)
          local removals = {}
          for _, extension in ipairs(ctx.extensions) do
            if ((base .. ".") .. extension) == get_original_file() then
              throw_error("I cannot remove the main file reference.")
            end
            table.insert(removals, named_command("Cleaning " .. extension,
              get_command(prefix, (base .. ".") .. extension)))
          end
          return removals
        end
      end
    },
  },
  arguments = {
    {
      identifier = "extensions",
      flag = function(ctx, value)
        if is_list(value) then
          return value
        else
          throw_error("I was expecting a list of extensions.")
        end
      end
    },
  }
}
