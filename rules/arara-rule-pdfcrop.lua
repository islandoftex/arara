-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "pdfcrop",
  name = "PDFCrop",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The cropping tool",
      command = function(ctx)
        local base = getBasename(ctx.reference.fileName)
        local input = base .. ".pdf"
        return getCommand("pdfcrop", ctx.verbose, ctx.debug, ctx.engine, ctx.margins, ctx.clip, ctx.hires, ctx.ini, ctx.restricted, ctx.papersize, ctx.resolution, ctx.bbox, ctx.uncompress, ctx.options, input, ctx.output)
      end
    },
  },
  arguments = {
    {
      identifier = "output",
      default = "",
      flag = function(ctx, value)
        return isEmpty(value, getBasename(ctx.reference.fileName) .. "-crop.pdf", value)
      end
    },
    {
      identifier = "verbose",
      flag = function(ctx, value)
        return isTrue(value, "--verbose", "--noverbose")
      end
    },
    {
      identifier = "debug",
      flag = function(ctx, value)
        return isTrue(value, "--debug", "--nodebug")
      end
    },
    {
      identifier = "engine",
      flag = function(ctx, value)
        local engines = { "pdftex", "xetex", "luatex" }
        if contains(engines, value) then
          return "--" .. value
        else
          throwError("The provided engine is invalid.")
        end
      end
    },
    {
      identifier = "margins",
      flag = function(ctx, value)
        if isList(value) then
          if #value ~= 4 then
            throwError("Margins require 4 elements.")
          else
            local ms = {}
            for _, m in ipairs(value) do
              table.insert(ms, tostring(m))
            end
            return { "--margins", join(ms, " ") }
          end
        else
          throwError("I was expecting a list.")
        end
      end
    },
    {
      identifier = "clip",
      flag = function(ctx, value)
        return isTrue(value, "--clip", "--noclip")
      end
    },
    {
      identifier = "hires",
      flag = function(ctx, value)
        return isTrue(value, "--hires", "--nohires")
      end
    },
    {
      identifier = "ini",
      flag = function(ctx, value)
        return isTrue(value, "--ini", "--noini")
      end
    },
    {
      identifier = "restricted",
      flag = function(ctx, value)
        return isTrue(value, "--restricted")
      end
    },
    {
      identifier = "papersize",
      flag = function(ctx, value)
        return { "--papersize", value }
      end
    },
    {
      identifier = "resolution",
      flag = function(ctx, value)
        return { "--resolution", value }
      end
    },
    {
      identifier = "bbox",
      flag = function(ctx, value)
        if isList(value) then
          if #value ~= 4 then
            throwError("Bounding box require 4 elements.")
          else
            local ms = {}
            for _, m in ipairs(value) do
              table.insert(ms, tostring(m))
            end
            return { "--margins", join(ms, " ") }
          end
        else
          throwError("I was expecting a list.")
        end
      end
    },
    {
      identifier = "uncompress",
      flag = function(ctx, value)
        return isTrue(value, "--uncompress")
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
