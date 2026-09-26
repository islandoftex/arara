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
        return getCommand("authorindex", ctx.draft, ctx.index, ctx.keep, ctx.print, ctx.recurse, ctx.options, getBasename(ctx.reference.fileName))
      end
    },
  },
  arguments = {
    {
      identifier = "draft",
      flag = function(ctx, value)
        return isTrue(value, "-d")
      end
    },
    {
      identifier = "index",
      flag = function(ctx, value)
        return isTrue(value, "-i")
      end
    },
    {
      identifier = "keep",
      flag = function(ctx, value)
        return isTrue(value, "-k")
      end
    },
    {
      identifier = "print",
      flag = function(ctx, value)
        return isTrue(value, "-p")
      end
    },
    {
      identifier = "recurse",
      flag = function(ctx, value)
        return isFalse(value, "-r")
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
