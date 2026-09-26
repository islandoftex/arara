-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "makeglossaries",
  name = "MakeGlossaries",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The MakeGlossaries software",
      command = function(ctx)
        if isEmpty(ctx.clean) then
          return getCommand("makeglossaries", ctx.options,
                            getBasename(ctx.reference.fileName))
        else
          local prefix
          if isUnix() then
            prefix = { "rm", "-f" }
          else
            prefix = { "cmd", "/c", "del" }
          end
          local base = getBasename(ctx.reference.path)
          local removals = {}
          table.insert(removals, getCommand(prefix, base .. ".glsdefs"))
          local lines = readFromFile(base .. "." .. "aux")
          local partial = ctx.clean[1] == "partial"
          local istDone = partial
          for _, line in ipairs(lines) do
            local g1, g2, g3 = line:match(
              "\\@newglossary%{.-%}%{(.-)%}%{(.-)%}%{(.-)%}")
            if g1 then
              for _, extension in ipairs({ g1, g2, g3 }) do
                table.insert(removals,
                  getCommand(prefix, base .. "." .. extension))
              end
            elseif not istDone then
              local ist = line:match("\\@istfilename%{(.-)%}")
              if ist then
                table.insert(removals, getCommand(prefix, ist))
                istDone = true
              end
            end
          end
          return removals
        end
      end
    },
  },
  arguments = {
    {
      identifier = "options",
      flag = function(ctx, value)
        if isList(value) then
          return value
        else
          throwError("I was expecting a list of options.")
        end
      end
    },
    {
      identifier = "clean",
      flag = function(ctx, value)
        if contains({ "all", "partial" }, value) then
          return value
        else
          throwError("The provided clean value is not valid.")
        end
      end
    },
  }
}
