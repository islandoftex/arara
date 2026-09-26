-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "bib2gls",
  name = "Bib2Gls",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The Bib2Gls software",
      command = function(ctx)
        if not isEmpty(ctx.clean) then
          local prefix
          if isUnix() then
            prefix = { "rm", "-f", "-v" }
          else
            prefix = { "cmd", "/c", "del" }
          end
          local base = getBasename(ctx.reference.path)
          local removals = {}
          local lines = readFromFile(base .. "." .. "aux")
          if not isEmpty(ctx.trans) then
            if ctx.trans[1] == getOriginalFile() then
              throwError("I cannot remove the main file reference.")
            end
            table.insert(removals, getCommand(prefix, ctx.trans))
          else
            table.insert(removals, getCommand(prefix, base .. ".glg"))
          end
          for _, line in ipairs(lines) do
            local resource = line:match(
              "\\glsxtr@resource%{.-%}%{([^%}]+)%}")
            if resource then
              table.insert(removals,
                getCommand(prefix, resource .. ".glstex"))
            end
          end
          return removals
        else
          return getCommand("bib2gls", ctx.dir, ctx.trans, ctx.group,
            ctx.interpret, ctx.breakspace, ctx.trimfields, ctx.recordcount,
            ctx.recordcountunit, ctx.cite, ctx.verbose, ctx.merge,
            ctx.locale, ctx.uniscript, ctx.packages, ctx.ignore, ctx.custom,
            ctx.mapformats, ctx.options, getBasename(ctx.reference.fileName))
        end
      end
    },
  },
  arguments = {
    {
      identifier = "dir",
      flag = function(ctx, value)
        return { "--dir", value }
      end
    },
    {
      identifier = "trans",
      flag = function(ctx, value)
        if contains({ "tex", "ltx", "cls", "sty", "bib", "dtx", "ins", "def", "ldf" }, value) then
          throwError("Forbidden transcript extension")
        end
        return { "--log-file", (getBasename(ctx.reference.fileName) .. ".") .. value }
      end
    },
    {
      identifier = "locale",
      flag = function(ctx, value)
        return { "--locale", value }
      end
    },
    {
      identifier = "group",
      flag = function(ctx, value)
        return isTrue(value, "--group", "--no-group")
      end
    },
    {
      identifier = "interpret",
      flag = function(ctx, value)
        return isTrue(value, "--interpret", "--no-interpret")
      end
    },
    {
      identifier = "breakspace",
      flag = function(ctx, value)
        return isTrue(value, "--break-space", "--no-break-space")
      end
    },
    {
      identifier = "trimfields",
      flag = function(ctx, value)
        return isTrue(value, "--trim-fields", "--no-trim-fields")
      end
    },
    {
      identifier = "recordcount",
      flag = function(ctx, value)
        return isTrue(value, "--record-count", "--no-record-count")
      end
    },
    {
      identifier = "recordcountunit",
      flag = function(ctx, value)
        return isTrue(value, "--record-count-unit", "--no-record-count-unit")
      end
    },
    {
      identifier = "cite",
      flag = function(ctx, value)
        return isTrue(value, "--cite-as-record", "--no-cite-as-record")
      end
    },
    {
      identifier = "verbose",
      flag = function(ctx, value)
        return isTrue(value, "--verbose", "--no-verbose")
      end
    },
    {
      identifier = "merge",
      flag = function(ctx, value)
        return isTrue(value, "--merge-wrglossary-records", "--no-merge-wrglossary-records")
      end
    },
    {
      identifier = "uniscript",
      flag = function(ctx, value)
        return isTrue(value, "--support-unicode-script", "--no-support-unicode-script")
      end
    },
    {
      identifier = "packages",
      flag = function(ctx, value)
        if isList(value) then
          local elements = {}
          for _, element in ipairs(value) do
            table.insert(elements, "--packages")
            table.insert(elements, element)
          end
          return elements
        else
          throwError("I was expecting a list of package names.")
        end
      end
    },
    {
      identifier = "ignore",
      flag = function(ctx, value)
        if isList(value) then
          local elements = {}
          for _, element in ipairs(value) do
            table.insert(elements, "--ignore-packages")
            table.insert(elements, element)
          end
          return elements
        else
          throwError("I was expecting a list of package names.")
        end
      end
    },
    {
      identifier = "custom",
      flag = function(ctx, value)
        if isList(value) then
          local elements = {}
          for _, element in ipairs(value) do
            table.insert(elements, "--custom-packages")
            table.insert(elements, element)
          end
          return elements
        else
          throwError("I was expecting a list of package names.")
        end
      end
    },
    {
      identifier = "mapformats",
      flag = function(ctx, value)
        if isList(value) then
          local elements = {}
          for _, element in ipairs(value) do
            if isList(element) and (#element == 2) then
              table.insert(elements, "--map-format")
              table.insert(elements, (element[0] .. ":") .. element[1])
            else
              throwError("I was expecting a map " .. "format list [<key>, <value>] ")
            end
          end
          return elements
        else
          throwError("I was expecting mapformats: [ [<key>, <value>], " .. "..., [<key>, <value>] ].")
        end
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
    {
      identifier = "clean",
      flag = function(ctx, value)
        return isTrue(value, "true", "")
      end
    },
  }
}
