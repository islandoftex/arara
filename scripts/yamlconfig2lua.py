#!/usr/bin/env python3
# SPDX-License-Identifier: BSD-3-Clause
"""Convert arara's YAML configuration files to the Lua format.

Reads a configuration file in the deprecated YAML format and emits an
equivalent Lua configuration table, ready to be saved next to the
original under one of the Lua configuration file names the command line
looks up. Unknown keys are rejected upfront (the Lua parser rejects
them as well, but the converter can name them before any file is
written), and null values are omitted: the Lua parser treats an absent
key exactly like a null one.

Usage: yamlconfig2lua.py FILE [-o OUTPUT]
"""

import argparse
import re
import sys

import yaml

# the configuration model keys, in canonical emission order (mirroring
# the shared configuration class constructor)
KEYS = [
    "paths",
    "filetypes",
    "language",
    "loops",
    "verbose",
    "logging",
    "header",
    "dbname",
    "logname",
    "laf",
    "preambles",
    "defaultPreamble",
    "prependPreambleIfDirectivesGiven",
]

# the only filetype entries the model accepts
FILETYPE_KEYS = {"extension", "pattern"}


def config_ctor(loader, node):
    return loader.construct_mapping(node)


yaml.SafeLoader.add_constructor("!config", config_ctor)


def is_identifier(name):
    return re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", name) is not None


def lua_string(value):
    escaped = (
        str(value)
        .replace("\\", "\\\\")
        .replace('"', '\\"')
        .replace("\r", "\\r")
        .replace("\n", "\\n")
    )
    return '"' + escaped + '"'


def emit_value(value, indent):
    """Emit a plain configuration value as Lua source."""
    pad = " " * indent
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, int):
        return str(value)
    if isinstance(value, str):
        return lua_string(value)
    if isinstance(value, list):
        if not value:
            return "{}"
        items = ",\n".join(
            pad + "  " + emit_value(item, indent + 2) for item in value
        )
        return "{\n" + items + ",\n" + pad + "}"
    if isinstance(value, dict):
        if not value:
            return "{}"
        items = []
        for key, entry in value.items():
            if is_identifier(key):
                header = key + " = "
            else:
                header = "[" + lua_string(key) + "] = "
            items.append(pad + "  " + header + emit_value(entry, indent + 2))
        return "{\n" + ",\n".join(items) + ",\n" + pad + "}"
    raise ValueError(f"unsupported configuration value: {value!r}")


def validate(data):
    """Reject anything the Lua configuration parser would reject."""
    if not isinstance(data, dict):
        raise ValueError("the configuration root must be a mapping")
    unknown = [key for key in data if key not in KEYS]
    if unknown:
        raise ValueError(
            "unknown configuration keys: " + ", ".join(sorted(unknown))
        )
    for filetype in data.get("filetypes", []) or []:
        if not isinstance(filetype, dict):
            raise ValueError("every filetype entry must be a mapping")
        if "extension" not in filetype:
            raise ValueError("every filetype entry requires an extension")
        extra = [key for key in filetype if key not in FILETYPE_KEYS]
        if extra:
            raise ValueError(
                "unsupported filetype keys: " + ", ".join(sorted(extra))
            )


def convert(data):
    """Emit the Lua source of the configuration table."""
    lines = ["return {"]
    for key in KEYS:
        if key in data and data[key] is not None:
            lines.append(
                "  " + key + " = " + emit_value(data[key], 2) + ","
            )
    lines.append("}")
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(
        description="Convert a YAML configuration file to the Lua format."
    )
    parser.add_argument("file", help="the YAML configuration file")
    parser.add_argument(
        "-o",
        "--output",
        help="the output file (defaults to standard output)",
    )
    arguments = parser.parse_args()

    text = open(arguments.file, encoding="utf-8").read()
    if not text.startswith("!config"):
        raise SystemExit("the configuration file must start with !config")

    data = yaml.load(text, Loader=yaml.SafeLoader)
    try:
        validate(data)
    except ValueError as error:
        raise SystemExit(f"error: {error}")
    output = convert(data)

    if arguments.output:
        with open(arguments.output, "w", encoding="utf-8") as handle:
            handle.write(output)
    else:
        sys.stdout.write(output)


if __name__ == "__main__":
    main()
