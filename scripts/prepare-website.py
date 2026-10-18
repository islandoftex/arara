#!/usr/bin/env python3
"""Stage the existing website for Zola 0.23 without editing its sources."""
import argparse
import ast
import os
from pathlib import Path
import re
import shutil
import stat

TOKEN = re.compile(r'{{\s*(tex|latex|get_url)\((.*?)\)\s*}}|{%\s*(hint|nicetoknow|messagebox)\((.*?)\)\s*%}|{%\s*(end)\s*%}', re.S)


def arguments(source):
    call = ast.parse('shortcode(' + source + ')', mode='eval').body
    if call.args or any(item.arg is None for item in call.keywords):
        raise ValueError('Shortcodes require named literal arguments')
    return {item.arg: ast.literal_eval(item.value) for item in call.keywords}


def raw(text):
    if '{% endraw %}' in text:
        raise ValueError('Unexpected endraw in legacy content')
    return '{% raw %}' + text + '{% endraw %}' if text else ''


def adapt(text):
    """Translate only the six legacy shortcodes, shielding all other braces."""
    front, body = text.split('+++', 2)[1:]
    output = []
    stack = []
    end = 0
    for match in TOKEN.finditer(body):
        output.append(raw(body[end:match.start()]))
        inline, inline_args, block, block_args, closing = match.groups()
        if inline:
            args = arguments(inline_args)
            if inline in ('tex', 'latex'):
                if args:
                    raise ValueError('Logo shortcodes do not accept arguments')
                logo = '<span class="tex">T<sub>e</sub>X</span>' if inline == 'tex' else '<span class="latex">L<sup>a</sup>T<sub>e</sub>X</span>'
                output.append(raw(logo))
            else:
                if set(args) != {'url'}:
                    raise ValueError('get_url requires url')
                output.append('{{< arara.url url={' + repr(args['url']) + '} />}}')
        elif block:
            args = arguments(block_args)
            if set(args) - {'title', 'reference'}:
                raise ValueError('Unknown callout arguments')
            indent = re.search(r'(?:^|\n)([ \t]*)$', body[:match.start()])
            prefix = indent.group(1) if indent else ''
            depth = 1
            closing_start = None
            for following in TOKEN.finditer(body, match.end()):
                if following.group(3):
                    depth += 1
                elif following.group(5):
                    depth -= 1
                    if depth == 0:
                        closing_start = following.start()
                        break
            if closing_start is None:
                raise ValueError('Unclosed legacy shortcode')
            lines = body[match.end():closing_start].splitlines()
            prefixes = [re.match(r'[ \t]*', line).group() for line in lines if line.strip()]
            dedent = os.path.commonprefix(prefixes) if prefixes else ''
            attrs = ' indent={' + repr(prefix) + '} dedent={' + repr(dedent) + '}' + ''.join(' ' + key + '={' + repr(value) + '}' for key, value in args.items())
            output.append('{% <arara.' + block + attrs + '> %}')
            stack.append(block)
        elif closing:
            if not stack:
                raise ValueError('Unexpected shortcode end')
            output.append('{% </arara.' + stack.pop() + '> %}')
        end = match.end()
    output.append(raw(body[end:]))
    if stack:
        raise ValueError('Unclosed legacy shortcode')
    return '+++' + front + '+++\n' + ''.join(output)


def prepare(repo, destination, theme):
    if destination.exists():
        raise ValueError('Destination must not exist: ' + str(destination))
    shutil.copytree(repo / 'website', destination,
                    ignore=shutil.ignore_patterns('themes', 'public', '.git'))
    shutil.copytree(theme, destination / 'themes/devlab-theme',
                    ignore=shutil.ignore_patterns('.git'))
    # Nix sources are read-only; Zola copies theme assets before site overrides.
    for path in destination.rglob('*'):
        path.chmod(path.stat().st_mode | stat.S_IWUSR)
    for source, target, start in [('README.md', '_index.md', 7),
                                  ('CHANGELOG.md', 'CHANGELOG.md', 2)]:
        text = ''.join((repo / source).read_text().splitlines(keepends=True)[start:])
        text = text.replace('## ', '# ')
        with (destination / 'content' / target).open('a') as stream:
            stream.write(text)
    for path in (destination / 'content').rglob('*.md'):
        path.write_text(adapt(path.read_text()))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--theme', type=Path)
    args = parser.parse_args()
    prepare(args.repo, args.output, args.theme or args.repo / 'website/themes/devlab-theme')
