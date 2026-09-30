-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "datatooltk",
  name = "DatatoolTk",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The DatatoolTk software",
      command = function(ctx)
        return get_command("datatooltk", ctx.options, ctx.output, ctx.csv, ctx.sql, ctx.input, ctx.sqldb, ctx.sqluser, ctx.name, ctx.probsoln, ctx.sort, ctx.sortlocale, ctx.sortcase, ctx.seed, ctx.shuffle, ctx.sep, ctx.delim, ctx.csvheader, ctx.noconsole, ctx.debug, ctx.owneronly, ctx.maptex, ctx.xls, ctx.ods, ctx.sheet, ctx.filterop, ctx.filters, ctx.truncate)
      end
    },
  },
  arguments = {
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
      identifier = "output",
      required = true,
      flag = function(ctx, value)
        return { "--output", value }
      end
    },
    {
      identifier = "csv",
      flag = function(ctx, value)
        return { "--csv", value }
      end
    },
    {
      identifier = "sep",
      flag = function(ctx, value)
        return { "--sep", value }
      end
    },
    {
      identifier = "delim",
      flag = function(ctx, value)
        return { "--delim", value }
      end
    },
    {
      identifier = "name",
      flag = function(ctx, value)
        return { "--name", value }
      end
    },
    {
      identifier = "sql",
      flag = function(ctx, value)
        return { "--sql", value }
      end
    },
    {
      identifier = "sqldb",
      flag = function(ctx, value)
        return { "--sqldb", value }
      end
    },
    {
      identifier = "sqluser",
      flag = function(ctx, value)
        return { "--sqluser", value }
      end
    },
    {
      identifier = "noconsole",
      default = "",
      flag = function(ctx, value)
        return { "--noconsole-action", is_empty(value, "gui", value) }
      end
    },
    {
      identifier = "probsoln",
      flag = function(ctx, value)
        return { "--probsoln", value }
      end
    },
    {
      identifier = "input",
      flag = function(ctx, value)
        return { "--in", value }
      end
    },
    {
      identifier = "sort",
      flag = function(ctx, value)
        return { "--sort", value }
      end
    },
    {
      identifier = "sortlocale",
      flag = function(ctx, value)
        return { "--sort-locale", value }
      end
    },
    {
      identifier = "sortcase",
      flag = function(ctx, value)
        return is_true(value, "--sort-case-sensitive", "--sort-case-insensitive")
      end
    },
    {
      identifier = "seed",
      flag = function(ctx, value)
        return { "--seed", value }
      end
    },
    {
      identifier = "shuffle",
      flag = function(ctx, value)
        return is_true(value, "--shuffle", "--noshuffle")
      end
    },
    {
      identifier = "csvheader",
      flag = function(ctx, value)
        return is_true(value, "--csvheader", "--nocsvheader")
      end
    },
    {
      identifier = "debug",
      flag = function(ctx, value)
        return is_true(value, "--debug", "--nodebug")
      end
    },
    {
      identifier = "owneronly",
      flag = function(ctx, value)
        return is_true(value, "--owner-only", "--noowner-only")
      end
    },
    {
      identifier = "maptex",
      flag = function(ctx, value)
        return is_true(value, "--map-tex-specials", "--nomap-tex-specials")
      end
    },
    {
      identifier = "xls",
      flag = function(ctx, value)
        return { "--xls", value }
      end
    },
    {
      identifier = "ods",
      flag = function(ctx, value)
        return { "--ods", value }
      end
    },
    {
      identifier = "sheet",
      flag = function(ctx, value)
        return { "--sheet", value }
      end
    },
    {
      identifier = "filterop",
      flag = function(ctx, value)
        if contains({ "and", "or" }, value) then
          return "--filter-" .. value
        else
          throw_error("The provided filterop value is not valid.")
        end
      end
    },
    {
      identifier = "filters",
      flag = function(ctx, value)
        if is_list(value) then
          local elements = {}
          for _, element in ipairs(value) do
            if is_list(element) and (#element == 3) then
              table.insert(elements, "--filter")
              table.insert(elements, element)
            else
              throw_error("I was expecting a filter " .. "list [<label>, <op>, <value>]")
            end
          end
          return elements
        else
          throw_error("I was expecting filters: [ [<label>, <op>, <value>], " .. "..., [<label>, <op>, <value>] ].")
        end
      end
    },
    {
      identifier = "truncate",
      flag = function(ctx, value)
        return { "--truncate", value }
      end
    },
  }
}
