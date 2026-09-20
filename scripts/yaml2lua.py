#!/usr/bin/env python3
# SPDX-License-Identifier: BSD-3-Clause
"""Convert arara's YAML/MVEL rules to the Lua rule format.

The MVEL snippets in the official rules only use a small, well-behaved
subset of the language, which this script parses and re-emits as Lua.
Rules using constructs outside that subset (Java regex interop) are
converted by hand and registered in HAND_CONVERTED.
"""

import glob
import os
import re
import sys

import yaml

# ----------------------------------------------------------------------
# YAML loading
# ----------------------------------------------------------------------


def config_ctor(loader, node):
    return loader.construct_mapping(node)


yaml.SafeLoader.add_constructor("!config", config_ctor)

# ----------------------------------------------------------------------
# MVEL tokenizer
# ----------------------------------------------------------------------

TOKEN_RE = re.compile(
    r"""
    (?P<ws>\s+)
  | (?P<str>'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*")
  | (?P<num>\d+(?:\.\d+)?)
  | (?P<id>[A-Za-z_][A-Za-z0-9_]*)
  | (?P<op>==|!=|<=|>=|&&|\|\||[-+*/!<>=?:;,.()\[\]{}])
""",
    re.X,
)


class Unsupported(Exception):
    pass


def tokenize(src):
    tokens = []
    pos = 0
    while pos < len(src):
        m = TOKEN_RE.match(src, pos)
        if not m:
            raise Unsupported(f"cannot tokenize at: {src[pos:pos+30]!r}")
        pos = m.end()
        if m.lastgroup == "ws":
            continue
        tokens.append((m.lastgroup, m.group()))
    tokens.append(("eof", ""))
    return tokens


# ----------------------------------------------------------------------
# MVEL parser (subset), producing tuple-based AST
# ----------------------------------------------------------------------


class Parser:
    def __init__(self, tokens):
        self.tokens = tokens
        self.i = 0

    def peek(self, offset=0):
        return self.tokens[self.i + offset]

    def next(self):
        tok = self.tokens[self.i]
        self.i += 1
        return tok

    def accept(self, value):
        if self.peek()[1] == value:
            return self.next()
        return None

    def expect(self, value):
        tok = self.next()
        if tok[1] != value:
            raise Unsupported(f"expected {value!r}, got {tok[1]!r}")
        return tok

    # ---------------- statements ----------------

    def parse_block_or_stmt(self):
        if self.accept("{"):
            stmts = []
            while not self.accept("}"):
                stmts.append(self.parse_statement())
            return stmts
        return [self.parse_statement()]

    def parse_statements(self):
        stmts = []
        while self.peek()[0] != "eof":
            stmts.append(self.parse_statement())
        return stmts

    def parse_statement(self):
        kind, value = self.peek()
        if value == "if":
            return self.parse_if()
        if value in ("foreach", "for"):
            return self.parse_foreach()
        if value == "return":
            self.next()
            if self.peek()[1] == ";":
                self.next()
                return ("return", None)
            expr = self.parse_expr()
            self.accept(";")
            return ("return", expr)
        # assignment: ID = expr ;
        if kind == "id" and self.peek(1)[1] == "=":
            name = self.next()[1]
            self.next()
            expr = self.parse_expr()
            self.accept(";")
            return ("assign", name, expr)
        expr = self.parse_expr()
        self.accept(";")
        return ("expr", expr)

    def parse_if(self):
        self.expect("if")
        self.expect("(")
        cond = self.parse_expr()
        self.expect(")")
        then = self.parse_block_or_stmt()
        elifs = []
        orelse = []
        while self.accept("else"):
            if self.peek()[1] == "if":
                self.next()
                self.expect("(")
                elif_cond = self.parse_expr()
                self.expect(")")
                elifs.append((elif_cond, self.parse_block_or_stmt()))
            else:
                orelse = self.parse_block_or_stmt()
                break
        return ("if", cond, then, elifs, orelse)

    def parse_foreach(self):
        self.next()  # foreach / for
        self.expect("(")
        var = self.next()[1]
        self.expect(":")
        iterable = self.parse_expr()
        self.expect(")")
        body = self.parse_block_or_stmt()
        return ("foreach", var, iterable, body)

    # ---------------- expressions ----------------

    def parse_expr(self):
        return self.parse_ternary()

    def parse_ternary(self):
        cond = self.parse_or()
        if self.accept("?"):
            then = self.parse_expr()
            self.expect(":")
            orelse = self.parse_expr()
            return ("ternary", cond, then, orelse)
        return cond

    def binary(self, sub, ops):
        left = sub()
        while self.peek()[1] in ops:
            op = self.next()[1]
            right = sub()
            left = ("binop", op, left, right)
        return left

    def parse_or(self):
        return self.binary(self.parse_and, ("||",))

    def parse_and(self):
        return self.binary(self.parse_eq, ("&&",))

    def parse_eq(self):
        return self.binary(self.parse_rel, ("==", "!="))

    def parse_rel(self):
        return self.binary(self.parse_add, ("<", ">", "<=", ">="))

    def parse_add(self):
        return self.binary(self.parse_unary, ("+", "-"))

    def parse_unary(self):
        if self.accept("!"):
            return ("not", self.parse_unary())
        return self.parse_postfix()

    def parse_postfix(self):
        expr = self.parse_primary()
        while True:
            if self.accept("."):
                name = self.next()[1]
                if self.accept("("):
                    args = self.parse_args()
                    expr = ("method", expr, name, args)
                else:
                    expr = ("member", expr, name)
            elif self.accept("["):
                index = self.parse_expr()
                self.expect("]")
                expr = ("index", expr, index)
            else:
                return expr

    def parse_args(self):
        args = []
        if self.accept(")"):
            return args
        args.append(self.parse_expr())
        while self.accept(","):
            args.append(self.parse_expr())
        self.expect(")")
        return args

    def parse_primary(self):
        kind, value = self.next()
        if kind == "str":
            return ("str", value[1:-1])
        if kind == "num":
            return ("num", value)
        if value == "(":
            expr = self.parse_expr()
            self.expect(")")
            return ("paren", expr)
        if value == "[":
            return self.parse_list_or_map()
        if kind == "id":
            if value in ("true", "false"):
                return ("bool", value)
            if value == "null":
                return ("nil",)
            if self.accept("("):
                return ("call", value, self.parse_args())
            return ("id", value)
        raise Unsupported(f"unexpected token {value!r}")

    def parse_list_or_map(self):
        if self.accept("]"):
            return ("list", [])
        first = self.parse_expr()
        if self.accept(":"):
            entries = [(first, self.parse_expr())]
            while self.accept(","):
                key = self.parse_expr()
                self.expect(":")
                entries.append((key, self.parse_expr()))
            self.expect("]")
            return ("map", entries)
        items = [first]
        while self.accept(","):
            items.append(self.parse_expr())
        self.expect("]")
        return ("list", items)


# ----------------------------------------------------------------------
# Lua emitter
# ----------------------------------------------------------------------

VOID_CALLS = {"throwError", "throw", "halt", "showMessage"}


class Emitter:
    def __init__(self, kind, self_param, arg_ids):
        self.kind = kind  # "flag" or "command"
        self.self_param = self_param
        self.arg_ids = set(arg_ids)
        self.declared = set()

    # ---------------- expressions ----------------

    def lua_string(self, value):
        return '"' + value.replace("\\", "\\\\").replace('"', '\\"') + '"'

    def emit_expr(self, node, parenthesize=False):
        result = self._emit_expr(node)
        if parenthesize and not re.fullmatch(
            r'[\w."\[\]#]+|\w+\(.*\)|".*"', result
        ):
            return f"({result})"
        return result

    def _concat_operand(self, node):
        # keySet() used inside a string concatenation is joined for display
        if node[0] == "method" and node[2] == "keySet":
            return f'join(keys({self._emit_expr(node[1])}), ", ")'
        return self.emit_expr(node, parenthesize=True)

    def _emit_expr(self, node):
        tag = node[0]
        if tag == "str":
            return self.lua_string(node[1])
        if tag == "num":
            return node[1]
        if tag == "bool":
            return node[1]
        if tag == "nil":
            return "nil"
        if tag == "paren":
            return f"({self._emit_expr(node[1])})"
        if tag == "list":
            return "{ " + ", ".join(self._emit_expr(i) for i in node[1]) + " }" \
                if node[1] else "{}"
        if tag == "map":
            entries = ", ".join(
                f"[{self._emit_expr(k)}] = {self._emit_expr(v)}"
                for k, v in node[1]
            )
            return "{ " + entries + " }"
        if tag == "id":
            return self.emit_identifier(node[1])
        if tag == "not":
            return f"not {self.emit_expr(node[1], parenthesize=True)}"
        if tag == "ternary":
            cond = self.emit_expr(node[1], parenthesize=True)
            then = self.emit_expr(node[2], parenthesize=True)
            orelse = self.emit_expr(node[3], parenthesize=True)
            return f"{cond} and {then} or {orelse}"
        if tag == "binop":
            return self.emit_binop(node)
        if tag == "index":
            return self.emit_index(node)
        if tag == "member":
            return self.emit_member(node)
        if tag == "method":
            return self.emit_method(node)
        if tag == "call":
            return self.emit_call(node)
        raise Unsupported(f"cannot emit {tag}")

    def emit_identifier(self, name):
        if name == "parameters":
            if self.kind == "command" and name in self.arg_ids:
                return "ctx.parameters"
            if self.kind != "flag":
                raise Unsupported("bare 'parameters' outside flag")
            return "ctx.parameters"
        if name == "reference":
            return "ctx.reference.path"
        if self.kind == "command" and name in self.arg_ids:
            return f"ctx.{name}"
        return name

    def emit_binop(self, node):
        _, op, left, right = node
        if op == "+":
            return f"{self._concat_operand(left)} .. {self._concat_operand(right)}"
        lua_op = {"&&": "and", "||": "or", "!=": "~="}.get(op, op)
        return (
            f"{self.emit_expr(left, parenthesize=True)} {lua_op} "
            f"{self.emit_expr(right, parenthesize=True)}"
        )

    def emit_index(self, node):
        _, obj, index = node
        if index[0] == "num":
            # MVEL list indices are zero-based, Lua sequences are one-based
            return (
                f"{self.emit_expr(obj, parenthesize=True)}"
                f"[{int(index[1]) + 1}]"
            )
        # non-numeric indices are map lookups and translate verbatim
        return (
            f"{self.emit_expr(obj, parenthesize=True)}"
            f"[{self._emit_expr(index)}]"
        )

    def emit_member(self, node):
        _, obj, name = node
        if obj == ("id", "parameters"):
            if self.kind == "flag" and name == self.self_param:
                return "value"
            if self.kind == "flag":
                return f"ctx.parameters.{name}"
            raise Unsupported("'parameters' access in command")
        if obj == ("id", "reference"):
            if name in ("fileName", "path", "parent"):
                return f"ctx.reference.{name}"
            raise Unsupported(f"reference member {name}")
        return f"{self.emit_expr(obj, parenthesize=True)}.{name}"

    def emit_method(self, node):
        _, obj, name, args = node
        if name == "concat":
            return (
                f"{self._concat_operand(obj)} .. "
                f"{self._concat_operand(args[0])}"
            )
        if name == "contains":
            if obj[0] == "method" and obj[2] == "keySet":
                map_expr = self.emit_expr(obj[1], parenthesize=True)
                return f"{map_expr}[{self._emit_expr(args[0])}] ~= nil"
            return (
                f"contains({self._emit_expr(obj)}, "
                f"{self._emit_expr(args[0])})"
            )
        if name == "keySet":
            return f"keys({self._emit_expr(obj)})"
        if name == "containsKey":
            return (
                f"{self.emit_expr(obj, parenthesize=True)}"
                f"[{self._emit_expr(args[0])}] ~= nil"
            )
        if name == "get":
            return (
                f"{self.emit_expr(obj, parenthesize=True)}"
                f"[{self._emit_expr(args[0])}]"
            )
        if name == "size":
            return f"#{self.emit_expr(obj, parenthesize=True)}"
        if name == "isEmpty":
            return f"isEmpty({self._emit_expr(obj)})"
        if name == "equals":
            return (
                f"{self.emit_expr(obj, parenthesize=True)} == "
                f"{self.emit_expr(args[0], parenthesize=True)}"
            )
        if name == "toString":
            return f"tostring({self._emit_expr(obj)})"
        if name == "join" and obj == ("id", "String"):
            return (
                f"join({self._emit_expr(args[1])}, "
                f"{self._emit_expr(args[0])})"
            )
        if name == "add":
            raise Unsupported("'add' outside statement position")
        # toFile(x).<path operation>()
        if obj[0] == "call" and obj[1] == "toFile":
            target = self._emit_expr(obj[2][0])
            if name == "normalize":
                return f"normalizePath({target})"
            if name == "createDirectories":
                return f"createDirectories({target})"
            if name == "removeDirectory":
                return f"removeDirectory({target})"
        raise Unsupported(f"method {name}")

    def emit_call(self, node):
        _, name, args = node
        if name == "throw":
            # upstream ltx2any uses a bare 'throw' which behaves like
            # throwError for our purposes
            name = "throwError"
        if name == "getOrNull":
            obj = self.emit_expr(args[0], parenthesize=True)
            index = int(args[1][1]) + 1 if len(args) > 1 else 1
            return f"{obj}[{index}]"
        if name == "toFile":
            # the Lua bridges operate on path strings directly
            return self._emit_expr(args[0])
        emitted = ", ".join(self._emit_expr(a) for a in args)
        return f"{name}({emitted})"

    # ---------------- statements ----------------

    def emit_statements(self, stmts, indent, top_level=False):
        lines = []
        for pos, stmt in enumerate(stmts):
            is_last = top_level and pos == len(stmts) - 1
            lines.extend(self.emit_statement(stmt, indent, is_last))
        return lines

    def emit_statement(self, stmt, indent, make_return=False):
        pad = "  " * indent
        tag = stmt[0]
        if tag == "return":
            expr = self._emit_expr(stmt[1]) if stmt[1] else ""
            return [f"{pad}return {expr}".rstrip()]
        if tag == "assign":
            _, name, expr = stmt
            prefix = ""
            if name not in self.declared:
                self.declared.add(name)
                prefix = "local "
            return [f"{pad}{prefix}{name} = {self._emit_expr(expr)}"]
        if tag == "expr":
            expr = stmt[1]
            if expr[0] == "method" and expr[2] == "add":
                obj = self._emit_expr(expr[1])
                arg = self._emit_expr(expr[3][0])
                return [f"{pad}table.insert({obj}, {arg})"]
            emitted = self._emit_expr(expr)
            is_void = expr[0] == "call" and expr[1] in VOID_CALLS
            if make_return and not is_void:
                return [f"{pad}return {emitted}"]
            return [f"{pad}{emitted}"]
        if tag == "foreach":
            _, var, iterable, body = stmt
            self.declared.add(var)
            lines = [
                f"{pad}for _, {var} in ipairs("
                f"{self._emit_expr(iterable)}) do"
            ]
            lines.extend(self.emit_statements(body, indent + 1))
            lines.append(f"{pad}end")
            return lines
        if tag == "if":
            _, cond, then, elifs, orelse = stmt

            def branch(body):
                lines = self.emit_statements(body[:-1], indent + 1)
                if body:
                    lines.extend(
                        self.emit_statement(body[-1], indent + 1, make_return)
                    )
                return lines

            lines = [f"{pad}if {self._emit_expr(cond)} then"]
            lines.extend(branch(then))
            for elif_cond, elif_body in elifs:
                lines.append(f"{pad}elseif {self._emit_expr(elif_cond)} then")
                lines.extend(branch(elif_body))
            if orelse:
                lines.append(f"{pad}else")
                lines.extend(branch(orelse))
            lines.append(f"{pad}end")
            return lines
        raise Unsupported(f"statement {tag}")


# ----------------------------------------------------------------------
# rule conversion
# ----------------------------------------------------------------------


def strip_mvel(snippet):
    snippet = snippet.strip()
    if not (snippet.startswith("@{") and snippet.endswith("}")):
        raise Unsupported("not a single MVEL block")
    return snippet[2:-1]


def convert_snippet(snippet, kind, self_param, arg_ids, indent):
    body = strip_mvel(snippet)
    stmts = Parser(tokenize(body)).parse_statements()
    emitter = Emitter(kind, self_param, arg_ids)
    return emitter.emit_statements(stmts, indent, top_level=True)


def lua_quote(value):
    return '"' + str(value).replace("\\", "\\\\").replace('"', '\\"') + '"'


def convert_rule(data, overrides):
    identifier = data["identifier"]
    arg_ids = [arg["identifier"] for arg in (data.get("arguments") or [])]
    out = []
    out.append("-- arara, the cool TeX automation tool")
    out.append("-- Copyright (c) 2026, Island of TeX")
    out.append("-- All rights reserved.")
    out.append("--")
    out.append("-- This rule is part of arara.")
    out.append("return {")
    out.append(f"  identifier = {lua_quote(identifier)},")
    out.append(f"  name = {lua_quote(data['name'])},")
    authors = ", ".join(lua_quote(author) for author in data.get("authors", []))
    out.append(f"  authors = {{ {authors} }},")
    out.append("  commands = {")
    for index, command in enumerate(data.get("commands", [])):
        out.append("    {")
        if command.get("name"):
            out.append(f"      name = {lua_quote(command['name'])},")
        out.append("      command = function(ctx)")
        key = (identifier, "command", index)
        if key in overrides:
            body = overrides[key]
        else:
            body = convert_snippet(
                command["command"], "command", None, arg_ids, 4
            )
        out.extend(body)
        out.append("      end")
        out.append("    },")
    out.append("  },")
    arguments = data.get("arguments") or []
    if arguments:
        out.append("  arguments = {")
        for argument in arguments:
            arg_id = argument["identifier"]
            out.append("    {")
            out.append(f"      identifier = {lua_quote(arg_id)},")
            if argument.get("required"):
                out.append("      required = true,")
            if argument.get("default") is not None:
                out.append(
                    f"      default = {lua_quote(argument['default'])},"
                )
            if argument.get("flag"):
                out.append("      flag = function(ctx, value)")
                key = (identifier, "flag", arg_id)
                if key in overrides:
                    body = overrides[key]
                else:
                    body = convert_snippet(
                        argument["flag"], "flag", arg_id, arg_ids, 4
                    )
                out.extend(body)
                out.append("      end")
            out.append("    },")
        out.append("  }")
    else:
        out.append("  arguments = {}")
    out.append("}")
    return "\n".join(out) + "\n"


def main():
    rules_dir = sys.argv[1] if len(sys.argv) > 1 else "rules"
    overrides = load_overrides()
    failed = []
    for path in sorted(glob.glob(os.path.join(rules_dir, "*.yaml"))):
        with open(path) as handle:
            data = yaml.safe_load(handle.read())
        target = path[: -len(".yaml")] + ".lua"
        try:
            lua = convert_rule(data, overrides)
        except Unsupported as error:
            failed.append((path, str(error)))
            continue
        with open(target, "w") as handle:
            handle.write(lua)
        print(f"converted {os.path.basename(path)}")
    if failed:
        print("\nfailed conversions (need manual attention):")
        for path, error in failed:
            print(f"  {os.path.basename(path)}: {error}")
        sys.exit(1)


def load_overrides():
    """Hand-written Lua bodies for snippets using Java regex interop."""

    def lines(text):
        return text.rstrip("\n").split("\n")

    overrides = {}
    overrides[("makeglossaries", "command", 0)] = lines(
        """        if isEmpty(ctx.clean) then
          return getCommand("makeglossaries", ctx.options,
                            getBasename(ctx.reference.fileName))
        else
          local prefix
          if isUnix() then
            prefix = { "rm", "-f" }
          else
            prefix = { "cmd", "/c", "del" }
          end
          local base = getBasename(ctx.reference.path)
          local removals = {}
          table.insert(removals, getCommand(prefix, base .. ".glsdefs"))
          local lines = readFromFile(base .. "." .. "aux")
          local partial = ctx.clean[1] == "partial"
          local istDone = partial
          for _, line in ipairs(lines) do
            local g1, g2, g3 = line:match(
              "\\\\@newglossary%{.-%}%{(.-)%}%{(.-)%}%{(.-)%}")
            if g1 then
              for _, extension in ipairs({ g1, g2, g3 }) do
                table.insert(removals,
                  getCommand(prefix, base .. "." .. extension))
              end
            elseif not istDone then
              local ist = line:match("\\\\@istfilename%{(.-)%}")
              if ist then
                table.insert(removals, getCommand(prefix, ist))
                istDone = true
              end
            end
          end
          return removals
        end"""
    )
    overrides[("bib2gls", "command", 0)] = lines(
        """        if not isEmpty(ctx.clean) then
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
              "\\\\glsxtr@resource%{.-%}%{([^%}]+)%}")
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
        end"""
    )
    return overrides


if __name__ == "__main__":
    main()
