-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "fig2dev",
  name = "fig2dev",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The fig2dev program",
      command = function(ctx)
        local input = getBasename(ctx.reference.file_name) .. ".fig"
        return getCommand("fig2dev", ctx.language, ctx.magnification, ctx.font, ctx.size, ctx.options, input, ctx.output)
      end
    },
  },
  arguments = {
    {
      identifier = "language",
      required = true,
      flag = function(ctx, value)
        if contains({ "box", "cgm", "epic", "eepic", "eepicemu", "emf", "eps", "gif", "ibmgl", "jpeg", "latex", "map", "mf", "mp", "mmp", "pcx", "pdf", "pdftex", "pdftex_t", "pic", "pictex", "png", "ppm", "ps", "pstex", "pstex_t", "ptk", "shape", "sld", "svg", "textyl", "tiff", "tk", "tpic", "xbm", "xpm" }, value) then
          return { "-L", value }
        else
          throwError("Invalid language, please refer to the manual.")
        end
      end
    },
    {
      identifier = "output",
      required = true,
      flag = function(ctx, value)
        return value
      end
    },
    {
      identifier = "magnification",
      flag = function(ctx, value)
        return { "-m", value }
      end
    },
    {
      identifier = "font",
      flag = function(ctx, value)
        return { "-f", value }
      end
    },
    {
      identifier = "size",
      flag = function(ctx, value)
        return { "-s", value }
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
