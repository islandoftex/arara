#!/usr/bin/env python3
"""Extract the PDF contract from a rendered website page, failing on drift."""
import argparse
from html.parser import HTMLParser
from pathlib import Path
import re
from urllib.parse import unquote, urlsplit

VOID = {'area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'link',
        'meta', 'param', 'source', 'track', 'wbr'}


class Document(HTMLParser):
    def __init__(self, source):
        super().__init__(convert_charrefs=False)
        self.source = source
        self.lines = source.splitlines(keepends=True)
        self.offsets = [0]
        for line in self.lines:
            self.offsets.append(self.offsets[-1] + len(line))
        self.depth = 0
        self.ranges = {}
        self.active = {}
        self.feed(source)
        if self.active or set(self.ranges) != {'title', 'content'}:
            raise ValueError('Expected one complete document title and content region')

    def position_offset(self):
        line, column = self.getpos()
        return self.offsets[line - 1] + column

    def handle_starttag(self, tag, attrs):
        if tag not in VOID:
            self.depth += 1
        names = dict(attrs)
        for key in ('title', 'content'):
            if 'data-arara-document-' + key in names:
                if key in self.ranges or key in self.active or tag in VOID:
                    raise ValueError('Duplicate or invalid document ' + key)
                self.active[key] = (self.depth, tag, self.position_offset() + len(self.get_starttag_text()))

    def handle_startendtag(self, tag, attrs):
        # Void/self-closing elements do not change the surrounding depth.
        pass

    def handle_endtag(self, tag):
        for key, (depth, opening, start) in list(self.active.items()):
            if self.depth == depth and tag == opening:
                self.ranges[key] = self.source[start:self.position_offset()]
                del self.active[key]
        if tag not in VOID:
            self.depth -= 1

    def get(self, key):
        value = self.ranges[key].strip()
        if not value:
            raise ValueError('Empty document ' + key)
        return value


def localize_assets(content, root, site_url):
    prefix = site_url.rstrip('/') + '/'

    def replace(match):
        quote, url = match.groups()
        if not url.startswith(prefix):
            return match.group()
        relative = unquote(urlsplit(url[len(prefix):]).path)
        asset = root / relative
        if not asset.is_file():
            raise ValueError('Missing website asset: ' + str(asset))
        return 'src=' + quote + asset.resolve().as_uri() + quote

    return re.sub(r"src=([\"'])(.*?)\1", replace, content)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--title', action='store_true')
    parser.add_argument('--check-site', type=Path)
    parser.add_argument('--asset-root', type=Path)
    parser.add_argument('--site-url', default='https://islandoftex.gitlab.io/arara')
    parser.add_argument('page', type=Path, nargs='?')
    args = parser.parse_args()
    try:
        if args.check_site:
            chapters = ('introduction', 'concepts', 'cli', 'configuration', 'logging',
                        'projects', 'methods', 'rules', 'building', 'deploying', 'yaml', 'mvel')
            pages = [args.check_site / 'manual' / chapter / 'index.html' for chapter in chapters]
            pages.append(args.check_site / 'quickstart/index.html')
            for page in pages:
                document = Document(page.read_text())
                document.get('title')
                content = localize_assets(document.get('content'), args.check_site, args.site_url)
                if 'version https://git-lfs.github.com/spec/v1' in content:
                    raise ValueError('Unresolved LFS pointer in ' + str(page))
            print('Verified 12 manual chapters and the quickstart')
        elif args.page:
            content = Document(args.page.read_text()).get('title' if args.title else 'content')
            if args.asset_root and not args.title:
                content = localize_assets(content, args.asset_root, args.site_url)
            print(content)
        else:
            parser.error('Supply a page or --check-site')
    except (ValueError, OSError) as error:
        parser.exit(1, f'{args.page}: {error}\n')
