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
        local infile = (get_basename(ctx.reference.file_name) .. ".") .. ctx.entry[1]
        local outfile = get_basename(ctx.output[1]) .. ".pdf"
        return get_command("xdvipdfmx", infile, "-o", outfile, ctx.options)
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
          throw_error("I was expecting either DVI or XDV.")
        end
      end
    },
    {
      identifier = "output",
      default = "",
      flag = function(ctx, value)
        return is_empty(value, ctx.reference.file_name, value)
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
