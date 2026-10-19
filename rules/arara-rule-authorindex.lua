-- arara, the cool TeX automation tool
-- Copyright (c) 2012-2026, Island of TeX
-- All rights reserved.
--
-- This rule is part of arara.
return {
  identifier = "authorindex",
  name = "AuthorIndex",
  authors = { "Island of TeX" },
  commands = {
    {
      name = "The AuthorIndex script",
      command = function(ctx)
        return get_command("authorindex", ctx.draft, ctx.index, ctx.keep, ctx.print, ctx.recurse, ctx.options, get_basename(ctx.reference.file_name))
      end
    },
  },
  arguments = {
    {
      identifier = "draft",
      flag = function(ctx, value)
        return is_true(value, "-d")
      end
    },
    {
      identifier = "index",
      flag = function(ctx, value)
        return is_true(value, "-i")
      end
    },
    {
      identifier = "keep",
      flag = function(ctx, value)
        return is_true(value, "-k")
      end
    },
    {
      identifier = "print",
      flag = function(ctx, value)
        return is_true(value, "-p")
      end
    },
    {
      identifier = "recurse",
      flag = function(ctx, value)
        return is_false(value, "-r")
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
