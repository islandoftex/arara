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
        if not is_empty(ctx.clean) then
          local prefix
          if is_unix() then
            prefix = { "rm", "-f", "-v" }
          else
            prefix = { "cmd", "/c", "del" }
          end
          local base = get_basename(ctx.reference.path)
          local removals = {}
          local lines = read_from_file(base .. "." .. "aux")
          if not is_empty(ctx.trans) then
            if ctx.trans[1] == get_original_file() then
              throw_error("I cannot remove the main file reference.")
            end
            table.insert(removals, get_command(prefix, ctx.trans))
          else
            table.insert(removals, get_command(prefix, base .. ".glg"))
          end
          for _, line in ipairs(lines) do
            local resource = line:match(
              "\\glsxtr@resource%{.-%}%{([^%}]+)%}")
            if resource then
              table.insert(removals,
                get_command(prefix, resource .. ".glstex"))
            end
          end
          return removals
        else
          return get_command("bib2gls", ctx.dir, ctx.trans, ctx.group,
            ctx.interpret, ctx.breakspace, ctx.trimfields, ctx.recordcount,
            ctx.recordcountunit, ctx.cite, ctx.verbose, ctx.merge,
            ctx.locale, ctx.uniscript, ctx.packages, ctx.ignore, ctx.custom,
            ctx.mapformats, ctx.options, get_basename(ctx.reference.file_name))
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
          throw_error("Forbidden transcript extension")
        end
        return { "--log-file", (get_basename(ctx.reference.file_name) .. ".") .. value }
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
        return is_true(value, "--group", "--no-group")
      end
    },
    {
      identifier = "interpret",
      flag = function(ctx, value)
        return is_true(value, "--interpret", "--no-interpret")
      end
    },
    {
      identifier = "breakspace",
      flag = function(ctx, value)
        return is_true(value, "--break-space", "--no-break-space")
      end
    },
    {
      identifier = "trimfields",
      flag = function(ctx, value)
        return is_true(value, "--trim-fields", "--no-trim-fields")
      end
    },
    {
      identifier = "recordcount",
      flag = function(ctx, value)
        return is_true(value, "--record-count", "--no-record-count")
      end
    },
    {
      identifier = "recordcountunit",
      flag = function(ctx, value)
        return is_true(value, "--record-count-unit", "--no-record-count-unit")
      end
    },
    {
      identifier = "cite",
      flag = function(ctx, value)
        return is_true(value, "--cite-as-record", "--no-cite-as-record")
      end
    },
    {
      identifier = "verbose",
      flag = function(ctx, value)
        return is_true(value, "--verbose", "--no-verbose")
      end
    },
    {
      identifier = "merge",
      flag = function(ctx, value)
        return is_true(value, "--merge-wrglossary-records", "--no-merge-wrglossary-records")
      end
    },
    {
      identifier = "uniscript",
      flag = function(ctx, value)
        return is_true(value, "--support-unicode-script", "--no-support-unicode-script")
      end
    },
    {
      identifier = "packages",
      flag = function(ctx, value)
        if is_list(value) then
          local elements = {}
          for _, element in ipairs(value) do
            table.insert(elements, "--packages")
            table.insert(elements, element)
          end
          return elements
        else
          throw_error("I was expecting a list of package names.")
        end
      end
    },
    {
      identifier = "ignore",
      flag = function(ctx, value)
        if is_list(value) then
          local elements = {}
          for _, element in ipairs(value) do
            table.insert(elements, "--ignore-packages")
            table.insert(elements, element)
          end
          return elements
        else
          throw_error("I was expecting a list of package names.")
        end
      end
    },
    {
      identifier = "custom",
      flag = function(ctx, value)
        if is_list(value) then
          local elements = {}
          for _, element in ipairs(value) do
            table.insert(elements, "--custom-packages")
            table.insert(elements, element)
          end
          return elements
        else
          throw_error("I was expecting a list of package names.")
        end
      end
    },
    {
      identifier = "mapformats",
      flag = function(ctx, value)
        if is_list(value) then
          local elements = {}
          for _, element in ipairs(value) do
            if is_list(element) and (#element == 2) then
              table.insert(elements, "--map-format")
              table.insert(elements, (element[0] .. ":") .. element[1])
            else
              throw_error("I was expecting a map " .. "format list [<key>, <value>] ")
            end
          end
          return elements
        else
          throw_error("I was expecting mapformats: [ [<key>, <value>], " .. "..., [<key>, <value>] ].")
        end
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
    {
      identifier = "clean",
      flag = function(ctx, value)
        return is_true(value, "true", "")
      end
    },
  }
}
