-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "xdvipdfmx",
  name = "XDVIPDFMX",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The XDVIPDFMX program",
      command = function(ctx)
        local infile = (getBasename(ctx.reference.file_name) .. ".") .. ctx.entry[1]
        local outfile = getBasename(ctx.output[1]) .. ".pdf"
        return getCommand("xdvipdfmx", infile, "-o", outfile, ctx.options)
      end
    },
  },
  arguments = {
    {
      identifier = "entry",
      default = "dvi",
      flag = function(ctx, value)
        if contains({ "dvi", "xdv" }, value) then
          return value
        else
          throwError("I was expecting either DVI or XDV.")
        end
      end
    },
    {
      identifier = "output",
      default = "",
      flag = function(ctx, value)
        return isEmpty(value, ctx.reference.file_name, value)
      end
    },
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
  }
}
