-- arara, the cool TeX automation tool
-- Copyright (c) 2026, Island of TeX
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
        if isUnix() then
          prefix = { "rm", "-f" }
        else
          prefix = { "cmd", "/c", "del" }
        end
        if isEmpty(ctx.extensions) then
          if getOriginalReference() == ctx.reference.path then
            throwError("I cannot remove the main file reference.")
          end
          return getCommand(prefix, ctx.reference.path)
        else
          local base = getBasename(ctx.reference.path)
          local removals = {}
          for _, extension in ipairs(ctx.extensions) do
            if ((base .. ".") .. extension) == getOriginalFile() then
              throwError("I cannot remove the main file reference.")
            end
            table.insert(removals, getCommand(prefix, (base .. ".") .. extension))
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
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of extensions.")
        end
      end
    },
  }
}
