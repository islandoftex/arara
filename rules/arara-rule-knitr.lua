-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "knitr",
  name = "knitr",
  authors = { "Adam Liter", "Island of TeX" },
  commands = {
    {
      name = "knitr",
      command = function(ctx)
        local elements = {}
        if not is_empty(ctx.tangle) then
          table.insert(elements, "tangle = " .. ctx.tangle[1])
        end
        if not is_empty(ctx.quiet) then
          table.insert(elements, "quiet = " .. ctx.quiet[1])
        end
        local out = is_empty(ctx.output) and "NULL" or (("\"" .. ctx.output[1]) .. "\"")
        table.insert(elements, "envir = " .. ctx.envir[1])
        table.insert(elements, "encoding = " .. ctx.encoding[1])
        return get_command("Rscript", "-e", (((((("library(knitr); knit(\"" .. ctx.reference.file_name) .. "\", ") .. "output = ") .. out) .. ", ") .. join(elements, ", ")) .. ")")
      end
    },
  },
  arguments = {
    {
      identifier = "output",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "tangle",
      flag = function(ctx, value)
        if is_true(value) and (not (ctx.parameters["output"] ~= nil)) then
          throw_error("If you want to tangle the document, specify an output file.")
        else
          return is_true(value, "TRUE", "FALSE")
        end
      end
    },
    {
      identifier = "quiet",
      flag = function(ctx, value)
        return is_true(value, "TRUE", "FALSE")
      end
    },
    {
      identifier = "envir",
      default = "parent.frame()",
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "encoding",
      default = "getOption(\"encoding\")",
      flag = function(ctx, value)
        return value
      end
    },
  }
}
