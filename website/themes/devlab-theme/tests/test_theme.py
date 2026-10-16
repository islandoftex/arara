"""Build small consumer sites and check the rendered theme contracts.

Run with Python 3.11+ and Zola 0.23.6+: ZOLA_BINARY=/path/to/zola python3 tests/test_theme.py
"""
import os
import re
from pathlib import Path
import subprocess
import shutil
import tempfile
import unittest
from html.parser import HTMLParser

REPO = Path(__file__).resolve().parents[1]
ZOLA = os.environ.get("ZOLA_BINARY", "zola")


def markdown(title, front="", body="Content."):
    return f'+++\ntitle = "{title}"\n{front}\n+++\n{body}\n'


class Document(HTMLParser):
    def __init__(self, html):
        super().__init__()
        self.elements = []
        self.text = []
        self.feed(html)

    def handle_starttag(self, tag, attrs):
        self.elements.append((tag, dict(attrs)))

    def handle_data(self, data):
        self.text.append(data)

    def attributes(self, tag=None, class_name=None):
        return [attrs for name, attrs in self.elements
                if (tag is None or name == tag)
                and (class_name is None or class_name in attrs.get("class", "").split())]


class ThemeTests(unittest.TestCase):
    def build(self, config="", files=None, expected_error=None):
        temporary = tempfile.TemporaryDirectory(prefix="devlab-test-")
        self.addCleanup(temporary.cleanup)
        root = Path(temporary.name)
        (root / "themes").mkdir()
        (root / "themes/devlab-theme").symlink_to(REPO, target_is_directory=True)
        (root / "zola.toml").write_text(
            'base_url="https://example.test/prefix/"\ntitle="Fixture"\n'
            'theme="devlab-theme"\ncompile_sass=true\n' + config)
        content = {"content/_index.md": markdown("Home")}
        content.update(files or {})
        for path, text in content.items():
            target = root / path
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(text)
        result = subprocess.run([ZOLA, "build"], cwd=root, capture_output=True, text=True)
        if expected_error is not None:
            self.assertNotEqual(result.returncode, 0, "Invalid configuration unexpectedly built")
            self.assertIn(expected_error, result.stdout + result.stderr)
            return None
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        return root / "public"

    def test_minimal_consumer(self):
        output = self.build()
        doc = Document((output / "index.html").read_text())
        self.assertEqual(len(doc.attributes("h1")), 1)
        self.assertTrue((output / "main.css").exists())
        scripts = [a.get('src', '') for a in doc.attributes('script')]
        self.assertFalse(any(src.endswith('/copy-button.js') or src.endswith('/tabs.js') for src in scripts))

    def test_header_divider_scope_with_prefix_and_translations(self):
        output = self.build(config='[languages.de]\n[extra.devlab.navigation]\nlinks=[]\n', files={
            'content/_index.de.md': markdown('Start'),
            'content/product.md': markdown('Product', 'template="landing.html"\n'),
            'content/manual/_index.md': markdown('Manual', 'template="docs.html"\n'),
        })
        for route, is_home in [('index.html', True), ('de/index.html', True),
                               ('product/index.html', False), ('manual/index.html', False)]:
            with self.subTest(route=route):
                doc = Document((output / route).read_text())
                header = doc.attributes('header', 'site-header')[0]
                self.assertEqual('data-scroll-header' in header, is_home)
                self.assertEqual(any(a.get('src', '') == 'https://example.test/prefix/js/header-divider.js'
                                     for a in doc.attributes('script')), is_home)

        docs_home = self.build(files={'content/_index.md': markdown('Manual', 'template="docs.html"\n')})
        doc = Document((docs_home / 'index.html').read_text())
        self.assertNotIn('data-scroll-header', doc.attributes('header', 'site-header')[0])
        self.assertFalse(any(a.get('src', '').endswith('/header-divider.js') for a in doc.attributes('script')))

    def test_content_enhancements_load_only_when_used(self):
        output = self.build(files={
            'content/code.md': markdown('Code', body='```sh\nhello\n```'),
            'content/tabs.md': markdown('Tabs', body='{% <devlab.tabs> %}{% <devlab.tab name="One"> %}Text.{% </devlab.tab> %}{% </devlab.tabs> %}'),
        })
        for route, copy, tabs in [('code', True, False), ('tabs', False, True)]:
            doc = Document((output / route / 'index.html').read_text())
            scripts = [a.get('src', '') for a in doc.attributes('script')]
            self.assertEqual(any(src.endswith('/copy-button.js') for src in scripts), copy)
            self.assertEqual(any(src.endswith('/tabs.js') for src in scripts), tabs)

    def test_multilingual_search_loads_and_runs_matching_pipeline(self):
        output = self.build('build_search_index=true\n[languages.ru]\nbuild_search_index=true\n'
                            '[languages.de]\nbuild_search_index=true\n', {
            'content/_index.ru.md': markdown('Home RU'),
            'content/_index.de.md': markdown('Home DE'),
            'content/guide.md': markdown('Installation'),
            'content/guide.ru.md': markdown('Установка'),
            'content/guide.de.md': markdown('Installation'),
            'content/exercise.md': markdown('Exercise'),
            'content/exercise.de.md': markdown('Übung'),
        })
        ru = (output / 'ru/guide/index.html').read_text()
        search_input = Document(ru).attributes('input', 'site-search-input')[0]
        self.assertEqual(search_input['type'], 'text')
        self.assertEqual(search_input['inputmode'], 'search')
        en = (output / 'guide/index.html').read_text()
        self.assertNotIn('lunr.ru.min.js', en)
        self.assertNotIn('lunr.de.min.js', en)
        self.assertLess(ru.index('elasticlunr.min.js'), ru.index('lunr.stemmer.support.min.js'))
        self.assertLess(ru.index('lunr.stemmer.support.min.js'), ru.index('lunr.ru.min.js'))
        self.assertLess(ru.index('lunr.ru.min.js'), ru.index('search_index.ru.js'))
        de = (output / 'de/guide/index.html').read_text()
        self.assertLess(de.index('elasticlunr.min.js'), de.index('lunr.stemmer.support.min.js'))
        self.assertLess(de.index('lunr.stemmer.support.min.js'), de.index('lunr.de.min.js'))
        self.assertLess(de.index('lunr.de.min.js'), de.index('search_index.de.js'))
        self.assertEqual(Document(de).attributes('input', 'site-search-input')[0]['placeholder'], 'Suchen...')
        self.assertIn('Suche', [text.strip() for text in Document(de).text])
        node = os.environ.get('NODE_BINARY', 'node')
        if not shutil.which(node):
            self.skipTest('Node.js is needed for JavaScript pipeline verification')
        for language, query, title in [('en', 'Installation', 'Installation'), ('ru', 'Установка', 'Установка'),
                                       ('ru', 'установки', 'Установка'), ('de', 'Installation', 'Installation'),
                                       ('de', 'Übungen', 'Übung')]:
            result = subprocess.run([node, str(REPO / 'tests/search_pipeline.cjs'), str(output), language, query, title], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        custom = self.build('build_search_index=true\n[languages.ru]\nbuild_search_index=true\n'
                            '[extra.devlab.search.language_scripts]\nru=["custom-language.js"]\n',
                            {'content/_index.ru.md': markdown('Home RU')})
        custom_html = (custom / 'ru/index.html').read_text()
        self.assertIn('https://example.test/prefix/custom-language.js', custom_html)
        self.assertNotIn('lunr.ru.min.js', custom_html)

    def test_layout_context_and_site_script_hooks(self):
        files = {
            'templates/base.html': '{% extends "devlab-theme/templates/base.html" %}'
                '{% block extra_scripts %}<script src="/site.js"></script>{% endblock extra_scripts %}',
            'content/_index.ru.md': markdown('Home RU'),
            'content/docs/_index.md': markdown('Docs', 'template="docs.html"\npage_template="doc-page.html"'),
            'content/docs/chapter.md': markdown('Chapter', body='## One\nText.\n## Two\nText.'),
            'content/docs/plain.md': markdown('Plain', 'template="page.html"'),
            'content/docs/generic/_index.md': markdown('Generic', 'template="section.html"'),
            'content/blog/_index.md': markdown('Blog', 'template="blog.html"\npage_template="blog-page.html"'),
            'content/blog/_index.ru.md': markdown('Blog RU', 'template="blog.html"\npage_template="blog-page.html"'),
            'content/blog/post.ru.md': markdown('Post RU', body='## One\nText.\n## Two\nText.'),
            'content/article.md': markdown('Article', 'template="blog-page.html"', '## One\nText.\n## Two\nText.'),
        }
        for mode in ['single', 'catalog']:
            files[f'content/{mode}/_index.md'] = markdown(mode,
                f'template="downloads-hub.html"\n[extra]\ndata="data/{mode}.toml"')
            files[f'data/{mode}.toml'] = f'mode="{mode}"\n[[product]]\nid="app"\nname="App"\nurl="/article/"\n'
        output = self.build('[languages.ru]\n[extra.devlab.collections.manual]\nsection="docs/_index.md"\n'
                            '[extra.devlab.blog]\n', files)
        for route in ['', 'docs', 'docs/chapter', 'docs/plain', 'docs/generic',
                      'blog', 'ru/blog/post', 'article', 'single', 'catalog']:
            with self.subTest(route=route):
                doc = Document((output / route / 'index.html').read_text())
                scripts = [a.get('src', '') for a in doc.attributes('script')]
                self.assertIn('/site.js', scripts)
                is_docs = route in ['docs', 'docs/chapter']
                self.assertEqual('site-main-docs' in doc.attributes('main')[0]['class'].split(), is_docs)
                if route in ['docs/plain', 'docs/generic']:
                    self.assertTrue(doc.attributes('footer', 'site-footer'))
                    self.assertFalse(any(src.endswith('/js/toc.js') for src in scripts))
                if route in ['ru/blog/post', 'article']:
                    self.assertTrue(doc.attributes(class_name='blog-toc'))
                    self.assertTrue(any(src.endswith('/js/toc.js') for src in scripts))
                self.assertEqual(any(src.endswith('/js/downloads-hub.js') for src in scripts), route == 'catalog')
                self.assertEqual(any(src.endswith('/js/header-divider.js') for src in scripts),
                                 route in ['', 'blog', 'ru/blog/post', 'article', 'single', 'catalog'])

    def test_plain_text_and_attributes_in_markdown_and_html(self):
        value = 'R&D <em>literal</em> "quoted"'
        url = 'https://example.test/?a=1&b="quoted"'
        data = f'''[extra]
value = '{value}'
url = '{url}'
[[extra.members]]
name = '{value}'
nickname = '{value}'
support = {{label = '{value}', path = '{url}'}}
role = '{value}'
bio = '{value}'
initials = '{value}'
path = '{url}'
avatar = '{url}'
links = [{{name = '{value}', path = '{url}'}}]
'''
        components = '''
{{<devlab.people members={page.extra.members} />}}
{{<devlab.badge text={page.extra.value} />}}
{% <devlab.callout title={page.extra.value}> %}**Markdown body**{% </devlab.callout> %}
{% <devlab.cards> %}
{% <devlab.card title={page.extra.value} href={page.extra.url} tag={page.extra.value}> %}**Markdown body**{% </devlab.card> %}
{% </devlab.cards> %}
{% <devlab.tabs label={page.extra.value} sync={page.extra.value}> %}
{% <devlab.tab name={page.extra.value} value={page.extra.value}> %}Body{% </devlab.tab> %}
{% </devlab.tabs> %}
{% <devlab.details summary={page.extra.value}> %}Body{% </devlab.details> %}
{% <devlab.file_tree label={page.extra.value}> %}
{% <devlab.folder name={page.extra.value}> %}{{<devlab.file name={page.extra.value} note={page.extra.value} />}}{% </devlab.folder> %}
{% </devlab.file_tree> %}
{% <devlab.timeline> %}
{% <devlab.timeline_item title={page.extra.value} label={page.extra.value} date={page.extra.value}> %}Body{% </devlab.timeline_item> %}
{% </devlab.timeline> %}
{{<devlab.icon name="info" label={page.extra.value} extra_class={page.extra.value} />}}
{% <devlab.diagram id="test-diagram" label={page.extra.value} description={page.extra.value} error_message={page.extra.value}> %}flowchart TD
A --> B{% </devlab.diagram> %}
'''
        output = self.build(files={
            "content/md.md": markdown("Markdown", 'template="page.html"\n' + data, components),
            "content/html.md": markdown("HTML", 'template="component-test.html"\n' + data),
            "templates/component-test.html": '{% extends "base.html" %}{% block content %}' + components + '{% endblock content %}',
        })
        for path in ["md/index.html", "html/index.html"]:
            with self.subTest(path=path):
                doc = Document((output / path).read_text())
                self.assertEqual(doc.attributes("em"), [], "plain text became markup")
                self.assertIn(value, ''.join(doc.text))
                self.assertTrue(doc.attributes("strong"), "Markdown body lost its formatting")
                self.assertEqual(doc.attributes("a", "person-link")[0]["aria-label"], value + " — " + value)
                self.assertEqual(doc.attributes("a", "person-link")[0]["href"], url)
                self.assertEqual(doc.attributes("a", "person-support")[0]["href"], url)
                self.assertEqual(doc.attributes("a", "person-support")[0]["aria-label"], value + " — " + value)
                self.assertEqual(doc.attributes("a", "card-link")[0]["href"], url)
                self.assertEqual(doc.attributes("div", "content-tabs")[0]["data-tabs-label"], value)
                self.assertEqual(doc.attributes("section", "content-tab-panel")[0]["data-tab-name"], value)
                self.assertEqual(doc.attributes("figure", "content-diagram")[0]["data-diagram-error"], value)
                for _, attrs in doc.elements:
                    self.assertNotIn('quoted"', attrs, "quote broke an HTML attribute")

    def test_people_optional_fields_and_translated_routes(self):
        output = self.build(config='[languages.de]\n', files={
            'content/team.de.md': markdown('Team', '''
[extra]
[[extra.members]]
name = "Alex"
nickname = "alexm"
avatar = "/images/avatar.svg"
path = "/team/"
links = [{name = "Team", path = "/team/", icon = "github"}]
support = {label = "Unterstützen", path = "/team/?support=1#alex"}
[[extra.members]]
name = "Mira"
''', '''
{{<devlab.people members={page.extra.members} content_lang={lang} heading_level={2} />}}
{{<devlab.people members={[]} />}}
'''),
        })
        doc = Document((output / 'de/team/index.html').read_text())
        self.assertEqual(len(doc.attributes('ul', 'people-grid')), 1)
        self.assertEqual(len(doc.attributes('article', 'person-card')), 2)
        self.assertEqual(len(doc.attributes('h2', 'person-name')), 2)
        self.assertEqual(len(doc.attributes('p', 'person-nickname')), 1)
        self.assertEqual(len(doc.attributes('p', 'person-bio')), 0)
        self.assertEqual(len(doc.attributes('a', 'person-support')), 1)
        self.assertEqual(doc.attributes('a', 'person-support')[0]['href'],
                         'https://example.test/prefix/de/team/?support=1#alex')
        self.assertEqual(doc.attributes('a', 'person-link')[0]['href'],
                         'https://example.test/prefix/de/team')
        self.assertEqual(doc.attributes('img')[0]['src'],
                         'https://example.test/prefix/images/avatar.svg')
        self.assertIn('M', doc.text)

    def test_people_rejects_incomplete_records(self):
        for record, message in [
            ('name = " "', 'Each devlab.people member needs a non-empty name.'),
            ('name = "Alex"\nsupport = {path = "/team/"}', 'A devlab.people support link needs label and path.'),
            ('name = "Alex"\nsupport = {label = "Support", path = " "}', 'A devlab.people support link needs label and path.'),
        ]:
            with self.subTest(record=record):
                self.build(files={'content/team.md': markdown('Team',
                           '[extra]\n[[extra.members]]\n' + record,
                           '{{<devlab.people members={page.extra.members} />}}')},
                           expected_error=message)

    def test_linked_card_allows_body_links_and_heading_levels(self):
        output = self.build(files={'content/cards.md': markdown('Cards', body='''
{% <devlab.card title="Guide" href="/guide/" heading_level={2}> %}
Read the [separate reference](https://example.org/reference).
{% </devlab.card> %}
{% <devlab.card title="Fallback" heading_level={9}> %}Text.{% </devlab.card> %}
''')})
        html = (output / 'cards/index.html').read_text()
        doc = Document(html)
        self.assertEqual(len(doc.attributes('div', 'card')), 2)
        self.assertEqual(len(doc.attributes('h2', 'card-title')), 1)
        self.assertEqual(len(doc.attributes('h3', 'card-title')), 1)
        self.assertEqual(doc.attributes('a', 'card-link')[0]['href'], 'https://example.test/prefix/guide')
        self.assertTrue(any(a.get('href') == 'https://example.org/reference' for a in doc.attributes('a')))
        class Links(HTMLParser):
            depth = 0
            nested = False
            def handle_starttag(self, tag, attrs):
                if tag == 'a':
                    self.nested |= self.depth > 0
                    self.depth += 1
            def handle_endtag(self, tag):
                if tag == 'a':
                    self.depth -= 1
        links = Links()
        links.feed(html)
        self.assertFalse(links.nested)
        self.build(files={'content/card.md': markdown('Card', body='{{<devlab.card href="/guide/" />}}')},
                   expected_error='needs a title')

    def test_small_extension_slots_and_custom_icons(self):
        output = self.build('[extra.devlab.footer]\nlinks=['
                            '{name="Launch",path="/launch/",icon="rocket"},'
                            '{name="Unknown icon",path="/unknown/",icon="missing"}]\n', files={
            'templates/base.html': '{% extends "devlab-theme/templates/base.html" %}'
                '{% block main_before %}<p>Before content</p>{% endblock main_before %}'
                '{% block main_after %}<p>After content</p>{% endblock main_after %}',
            'templates/partials/header-brand.html': '<a class="site-logo" href="/">Custom brand</a>',
            'templates/partials/header-tools.html': '<span>Custom tools</span>'
                '{% include "partials/theme-toggle.html" %}',
            'templates/partials/icon.html': '{% if name == "rocket" %}<svg viewBox="0 0 24 24"><path data-custom-icon="rocket" d="M0 0h1"/></svg>'
                '{% else %}{% include "partials/devlab/icons.html" %}{% endif %}',
            'content/icons.md': markdown('Icons', body='{{<devlab.icon name="rocket" label="Rocket" />}}'
                '{{<devlab.icon name="info" label="Info" />}}'),
        })
        doc = Document((output / 'icons/index.html').read_text())
        text = ''.join(doc.text)
        for value in ['Before content', 'After content', 'Custom brand', 'Custom tools']:
            self.assertIn(value, text)
        self.assertEqual(sum(a.get('data-custom-icon') == 'rocket' for a in doc.attributes('path')), 2)
        footer_links = doc.attributes('a', 'site-footer-link')
        self.assertEqual(footer_links[0].get('aria-label'), 'Launch')
        self.assertNotIn('aria-label', footer_links[1])
        self.assertIn('Unknown icon', text)
        self.assertTrue(any(a.get('aria-label') == 'Rocket' for a in doc.attributes('span')))
        self.assertTrue(doc.attributes('button', 'theme-toggle'))

    def test_shared_page_slots_without_layout_copies(self):
        output = self.build('[extra.devlab.collections.manual]\nsection="docs/_index.md"\n', {
            'templates/partials/page-heading-extra.html': '<p data-heading-extra>{{ context_item.title }} addition</p>',
            'templates/partials/page-meta.html': '<p data-page-meta>{{ devlab_layout }} metadata</p>',
            'templates/partials/page-actions.html': '<p data-page-actions>{{ context_item.title }} actions</p>',
            'content/plain.md': markdown('Plain'),
            'content/section/_index.md': markdown('Section'),
            'content/article.md': markdown('Article', 'template="blog-page.html"'),
            'content/docs/_index.md': markdown('Docs', 'template="docs.html"\npage_template="doc-page.html"'),
            'content/docs/chapter.md': markdown('Chapter'),
        })
        for route in ['plain', 'section', 'article', 'docs/chapter']:
            doc = Document((output / route / 'index.html').read_text())
            for attribute in ['data-heading-extra', 'data-page-meta', 'data-page-actions']:
                self.assertTrue(any(attribute in a for a in doc.attributes('p')), (route, attribute))
            self.assertEqual(len(doc.attributes('h1')), 1)

    def test_independent_collections_and_configuration_precedence(self):
        files = {'content/_index.ru.md': markdown('Home RU'), 'content/_index.fr.md': markdown('Home FR')}
        for folder, label in [('manual', 'Manual'), ('api', 'API')]:
            for suffix in ['', '.ru']:
                files[f'content/{folder}/_index{suffix}.md'] = markdown(label,
                    'template="docs.html"\npage_template="doc-page.html"\nsort_by="weight"')
                files[f'content/{folder}/first{suffix}.md'] = markdown(label + ' first', 'weight=1', '## One\nText.\n## Two\nText.')
                files[f'content/{folder}/second{suffix}.md'] = markdown(label + ' second', 'weight=2')
        files['content/manual/first.md'] = markdown('Manual first',
            'weight=1\n[extra.devlab]\nshow_toc=false\n[extra.devlab.page_actions]\nshow_copy_link=false')
        files['content/manual/nested/_index.md'] = markdown('Nested',
            'template="docs.html"\npage_template="doc-page.html"\nweight=3\n[extra.devlab]\nshow_toc=false')
        files['content/manual/nested/child.md'] = markdown('Child', body='## One\nText.\n## Two\nText.')
        files['content/api/relocated.md'] = markdown('Relocated', 'weight=3\npath="outside/reference"')
        config = '''[languages.ru]
[languages.fr]
[extra.devlab.navigation]
links=[{collection="manual",name="Manual"},{collection="api",name="API"}]
[extra.devlab.collections.manual]
section="manual/_index.md"
title="User manual"
[extra.devlab.collections.manual.page_actions]
show_copy_link=true
edit_url="https://example.org/edit/"
[extra.devlab.collections.api]
section="api/_index.md"
title="API reference"
show_toc=false
'''
        output = self.build(config, files)
        for prefix in ['', 'ru/']:
            for folder, other in [('manual', 'api'), ('api', 'manual')]:
                doc = Document((output / f'{prefix}{folder}/first/index.html').read_text())
                links = doc.attributes('a', 'docs-nav-link')
                self.assertTrue(links)
                self.assertTrue(all(f'/prefix/{prefix}{folder}/' in a['href'] or (folder == 'api' and not prefix and '/outside/reference/' in a['href']) for a in links))
                self.assertFalse(any(f'/{other}/' in a['href'] for a in links))
                self.assertEqual(doc.attributes('a', 'docs-prev-next-link-next')[0]['href'],
                                 f'https://example.test/prefix/{prefix}{folder}/second/')
        first = Document((output / 'manual/first/index.html').read_text())
        self.assertFalse(first.attributes('button', 'docs-copy-link-button'))
        self.assertTrue(first.attributes('a', 'docs-toc-edit'), 'nested action fields lost inherited edit_url')
        child = Document((output / 'manual/nested/child/index.html').read_text())
        self.assertFalse(any(a.get('src', '').endswith('/toc.js') for a in child.attributes('script')))
        self.assertTrue(child.attributes('button', 'docs-copy-link-button'))
        untranslated = Document((output / 'fr/index.html').read_text())
        self.assertFalse(untranslated.attributes('a', 'site-nav-link'))
        relocated = Document((output / 'outside/reference/index.html').read_text())
        self.assertTrue(relocated.attributes('a', 'docs-nav-link'))
        self.assertIn('API reference', ''.join(relocated.text))
        self.build('[extra.devlab.collections.manual]\nsection="manual/_index.md"\n'
                   'navigation=[{section="api/_index.md"}]\n', files, expected_error='navigation must stay inside')
        for navigation, error in [('[{}]', 'entries require a section source path'),
                                  ('[{section="manual/nested/_index.md"},{section="manual/nested/_index.md"}]', 'entries must be unique'),
                                  ('[{section="manual/_index.md"},{section="manual/nested/_index.md"}]', 'entries must not overlap'),
                                  ('[{section="manual/nested/_index.md"},{section="manual/_index.md"}]', 'entries must not overlap')]:
            self.build('[extra.devlab.collections.manual]\nsection="manual/_index.md"\n'
                       f'navigation={navigation}\n', files, expected_error=error)
        self.build('[extra.devlab.collections.bad]\nsection="missing/_index.md"\n', expected_error='missing')
        self.build('[extra.devlab.navigation]\nlinks=[{collection="unknown"}]\n', expected_error='unknown devlab collection')

    def test_collection_navigation_with_relocated_pages(self):
        files = {
            'content/manual/_index.md': markdown('Manual', 'template="docs.html"\npage_template="doc-page.html"'),
            'content/manual/part/_index.md': markdown('Part', 'template="docs.html"'),
            'content/manual/part/child.md': markdown('Relocated', 'path="outside/reference"'),
            'content/manual/other/_index.md': markdown('Other', 'template="docs.html"'),
            'content/manual/other/child.md': markdown('Other child'),
        }
        config = '[extra.devlab.navigation]\nlinks=[{collection="manual"}]\n' \
                 '[extra.devlab.collections.manual]\nsection="manual/_index.md"\n'
        for navigation in ['', 'navigation=[{section="manual/part/_index.md"},{section="manual/other/_index.md"}]\n']:
            output = self.build(config + navigation, files)
            for route in ['manual/part', 'outside/reference']:
                with self.subTest(navigation=navigation, route=route):
                    doc = Document((output / route / 'index.html').read_text())
                    groups = doc.attributes('li', 'docs-nav-group')
                    active = [a['data-docs-nav-group'] for a in groups if 'data-docs-nav-active-branch' in a]
                    self.assertEqual(active, ['https://example.test/prefix/manual/part/'] * 2)
                    for class_name in ['site-nav-link', 'site-menu-link']:
                        self.assertEqual(doc.attributes('a', class_name)[0].get('aria-current'), 'location')

    def test_docs_catalog_lists_one_level_with_or_without_manifest(self):
        files = {
            'content/docs/_index.md': markdown('Docs', 'template="docs.html"\npage_template="doc-page.html"'),
            'content/docs/first.md': markdown('First'),
            'content/docs/part/_index.md': markdown('Part', 'template="docs.html"'),
            'content/docs/part/child.md': markdown('Child'),
            'content/docs/part/deep/_index.md': markdown('Deep', 'template="docs.html"'),
            'content/docs/part/deep/last.md': markdown('Last'),
            'content/docs/transparent/_index.md': markdown('Transparent', 'transparent=true'),
            'content/docs/transparent/promoted.md': markdown('Promoted'),
        }
        config = '[extra.devlab.collections.manual]\nsection="docs/_index.md"\n'
        for manifest in ['', 'navigation=[{section="docs/part/_index.md"}]\n']:
            output = self.build(config + manifest, files)
            expected = {'docs/part'} if manifest else {'docs/first', 'docs/part', 'docs/transparent/promoted'}
            for route, targets in [('docs', expected), ('docs/part', {'docs/part/child', 'docs/part/deep'})]:
                with self.subTest(manifest=manifest, route=route):
                    doc = Document((output / route / 'index.html').read_text())
                    cards = doc.attributes('a', 'docs-overview-card')
                    self.assertEqual(len(cards), len(targets))
                    self.assertEqual({a['href'] for a in cards},
                                     {f'https://example.test/prefix/{target}/' for target in targets})

    def test_navigation_labels_are_text_and_do_not_change_reading_order(self):
        config = '''[languages.de]
[extra.devlab.collections.manual]
section="manual/_index.md"
navigation=[
 {label='Documentation & <manual>',translations={de='Dokumentation & <Handbuch>'}},
 {section="manual/guide/_index.md"},
 {label="Applications",translations={de="Anwendungen"}},
 {section="manual/apps/_index.md"},
 {label="Untranslated"},
 {section="manual/english/_index.md"},
 {label="Empty"},
]
'''
        files = {}
        for suffix, title in [('', 'Manual'), ('.de', 'Handbuch')]:
            files[f'content/manual/_index{suffix}.md'] = markdown(title,
                'template="docs.html"\npage_template="doc-page.html"')
            for folder in ['guide', 'apps']:
                files[f'content/manual/{folder}/_index{suffix}.md'] = markdown(folder,
                    'template="docs.html"\nsort_by="weight"')
                files[f'content/manual/{folder}/first{suffix}.md'] = markdown('First', 'weight=1')
        files['content/manual/english/_index.md'] = markdown('English', 'template="docs.html"')
        files['content/manual/english/first.md'] = markdown('English only')
        output = self.build(config, files)
        for prefix, label_count, label in [('', 3, 'Documentation & <manual>'),
                                           ('de/', 2, 'Dokumentation & <Handbuch>')]:
            with self.subTest(prefix=prefix):
                html = (output / f'{prefix}manual/index.html').read_text()
                doc = Document(html)
                self.assertEqual(len(doc.attributes('p', 'docs-nav-label-text')), label_count * 2)
                self.assertIn(label, doc.text)
                self.assertFalse(doc.attributes('manual') or doc.attributes('handbuch'))
                self.assertNotIn('Empty', doc.text)
                label_fragments = re.findall(r'<li class="docs-nav-label">(.*?)</li>', html, re.S)
                for fragment in label_fragments:
                    heading = Document(fragment)
                    self.assertFalse(heading.attributes('a') or heading.attributes('button'))
                    self.assertTrue(all('tabindex' not in a for _, a in heading.elements))
                targets = ['guide', 'apps'] + ([] if prefix else ['english'])
                self.assertEqual([a['href'] for a in doc.attributes('a', 'docs-overview-card')],
                    [f'https://example.test/prefix/{prefix}manual/{folder}/' for folder in targets])
                if prefix:
                    self.assertNotIn('Untranslated', doc.text)
                    self.assertIn('Anwendungen', doc.text)
                first = Document((output / f'{prefix}manual/guide/first/index.html').read_text())
                self.assertEqual(first.attributes('a', 'docs-prev-next-link-next')[0]['href'],
                                 f'https://example.test/prefix/{prefix}manual/apps/first/')
                self.assertFalse(first.attributes('a', 'docs-prev-next-link-previous'))
                second = Document((output / f'{prefix}manual/apps/first/index.html').read_text())
                self.assertEqual(second.attributes('a', 'docs-prev-next-link-previous')[0]['href'],
                                 f'https://example.test/prefix/{prefix}manual/guide/first/')

    def test_navigation_rejects_empty_and_ambiguous_labels(self):
        files = {'content/manual/_index.md': markdown('Manual', 'template="docs.html"'),
                 'content/manual/part/_index.md': markdown('Part', 'template="docs.html"')}
        for navigation, error in [
            ('[{label=" "}]', 'labels must be non-empty text'),
            ('[{label="Apps",section="manual/part/_index.md"}]', 'labels cannot have a section or path'),
            ('[{label="Apps",path="/apps/"}]', 'labels cannot have a section or path'),
            ('[{label="Apps",translations={en=" "}}]', 'label translations must be non-empty text'),
            ('[{section="manual/part/_index.md"},{label="Apps"},{section="manual/part/_index.md"}]', 'entries must be unique'),
        ]:
            with self.subTest(navigation=navigation):
                self.build('[extra.devlab.collections.manual]\nsection="manual/_index.md"\n'
                           f'navigation={navigation}\n', files, expected_error=error)

    def test_collection_mobile_tree_without_global_links(self):
        files = {
            'content/manual/_index.md': markdown('Manual', 'template="docs.html"\npage_template="doc-page.html"'),
            'content/manual/chapter.md': markdown('Chapter'),
        }
        output = self.build('[extra.devlab.navigation]\nlinks=[]\n'
                            '[extra.devlab.collections.manual]\nsection="manual/_index.md"\n', files)
        chapter = Document((output / 'manual/chapter/index.html').read_text())
        self.assertTrue(chapter.attributes('button', 'site-menu-button'))
        self.assertTrue(any(a.get('data-site-menu-initial-panel') == 'docs' for a in chapter.attributes('div')))
        self.assertFalse(chapter.attributes('button', 'site-menu-back'))
        home = Document((output / 'index.html').read_text())
        self.assertFalse(home.attributes('button', 'site-menu-button'))
        self.build('[extra.devlab.collections.manual]\n', files,
                   expected_error='Every devlab collection needs a section source path')
        self.build('[extra.devlab.navigation]\nlinks=[{name="Docs"}]\n', files,
                   expected_error='Navigation links require name and path, or a collection')

    def test_ordered_landing_blocks_on_pages_and_sections(self):
        front = '''[extra.devlab.home]
layout="wide"
blocks=[
 {type="hero",id="intro",eyebrow="Product",actions=[{label="Read",path="/guide/"}]},
 {type="cta",id="start",title="Start here"},
 {type="features",id="features",title="Capabilities",items=[{title="Fast",description="Small runtime"}]},
 {type="content",id="body"},
 {type="features",id="more",items=[{title="Composable"}]},
 {type="learn",id="learn",title="Learn",items=[{title="Guide",path="/guide/"}]},
 {type="workflow",id="workflow",items=[{title="Install",description="Add the theme",kicker="Setup",icon="folder",path="/install/"},{title="Build"}]},
]
'''
        output = self.build(files={
            'content/_index.md': markdown('Home', front, '**Home body**'),
            'content/product.md': markdown('Product', 'template="landing.html"\n' + front, '**Product body**'),
            'content/other/_index.md': markdown('Other', 'template="landing.html"\n' + front, '**Other body**'),
        })
        for route, title in [('', 'Home'), ('product', 'Product'), ('other', 'Other')]:
            doc = Document((output / route / 'index.html').read_text())
            self.assertEqual(len(doc.attributes('h1')), 1)
            self.assertEqual([a['id'] for a in doc.attributes('section') if a.get('id') in ['intro','start','features','body','more','learn','workflow']],
                             ['intro','start','features','body','more','learn','workflow'])
            self.assertIn(title + ' body', ''.join(doc.text))
            self.assertTrue(doc.attributes('h3', 'feature-card-title'))
            self.assertEqual(len(doc.attributes('h2', 'feature-card-title')), 1)
            self.assertEqual(len(doc.attributes('h2', 'landing-step-title')), 2)
            self.assertTrue(doc.attributes('div', 'landing-learn-inner'))
            self.assertEqual(len(doc.attributes('li', 'landing-workflow-step')), 2)
            self.assertFalse(doc.attributes('li', 'feature-card'))
            self.assertEqual(doc.attributes('ol', 'landing-workflow')[0]['role'], 'list')
            self.assertEqual(doc.attributes('a', 'feature-card-link')[0]['href'], 'https://example.test/prefix/guide')
            self.assertEqual(doc.attributes('a', 'landing-step-link')[0]['href'], 'https://example.test/prefix/install')
            self.assertIn('Setup', ''.join(doc.text))
            self.assertIn('Add the theme', ''.join(doc.text))
        for blocks, error in [('[{type="missing"}]', 'Unknown landing block'),
                              ('[{type="hero"},{type="hero"}]', 'at most one hero'),
                              ('[{type="cta"},{type="hero"}]', 'first block'),
                              ('[{type="cta",id="x"},{type="content",id="x"}]', 'ids must be unique')]:
            self.build(files={'content/_index.md': markdown('Home', '[extra.devlab.home]\nblocks=' + blocks)}, expected_error=error)

    def test_reader_order_includes_section_introductions_and_mobile_toc(self):
        output = self.build('''[extra.devlab.collections.book]
section="book/_index.md"
reading_order=["book/_index.md","book/first.md","book/part/_index.md","book/part/chapter.md","book/last.md"]
[extra.devlab.collections.book.page_actions]
edit_url="https://example.org/edit/"
[extra.devlab.navigation]
links=[{collection="book",name="Book"}]
''', {
            'content/book/_index.md': markdown('Book', 'template="reader.html"\npage_template="reader.html"\nsort_by="weight"', 'Introduction.\n## One\nText.\n## Two\nText.'),
            'content/book/first.md': markdown('First', 'weight=1', '## One\nText.\n## Two\nText.'),
            'content/book/part/_index.md': markdown('Part introduction', 'weight=2\ntemplate="reader.html"\npage_template="reader.html"\nsort_by="weight"', 'Part introduction body.'),
            'content/book/part/chapter.md': markdown('Chapter', 'weight=1'),
            'content/book/last.md': markdown('Last', 'weight=3'),
        })
        sequence = ['book', 'book/first', 'book/part', 'book/part/chapter', 'book/last']
        for index, route in enumerate(sequence):
            with self.subTest(route=route):
                doc = Document((output / route / 'index.html').read_text())
                self.assertIn('site-main-reader', doc.attributes('main')[0]['class'])
                self.assertEqual(len(doc.attributes('h1')), 1)
                self.assertEqual([a['href'] for a in doc.attributes('a', 'docs-nav-link')[:len(sequence)]], [f'https://example.test/prefix/{item}/' for item in sequence])
                previous = doc.attributes('a', 'docs-prev-next-link-previous')
                next_link = doc.attributes('a', 'docs-prev-next-link-next')
                self.assertEqual([a['href'] for a in previous],
                                 [f'https://example.test/prefix/{sequence[index-1]}/'] if index else [])
                self.assertEqual([a['href'] for a in next_link],
                                 [f'https://example.test/prefix/{sequence[index+1]}/'] if index+1 < len(sequence) else [])
                self.assertTrue(doc.attributes('a', 'docs-page-edit-inline'))
                self.assertTrue(any(a.get('media') == 'print' for a in doc.attributes('link')))
        chapter = Document((output / 'book/first/index.html').read_text())
        self.assertTrue(chapter.attributes('details', 'docs-toc-inline'))
        self.assertIn('Part introduction body.', ''.join(Document((output / 'book/part/index.html').read_text()).text))

    def test_reader_explicit_order_distinguishes_pages_from_sections(self):
        files = {
            'content/book/_index.md': markdown('Book', 'template="reader.html"\npage_template="reader.html"'),
            'content/book/chapter_index.md': markdown('Chapter index', 'slug="chapter-index"'),
            'content/other.md': markdown('Other'),
        }
        config = '[extra.devlab.collections.book]\nsection="book/_index.md"\n'
        output = self.build(config + 'reading_order=["book/_index.md","book/chapter_index.md"]\n', files)
        chapter = Document((output / 'book/chapter-index/index.html').read_text())
        self.assertEqual(chapter.attributes('a', 'docs-prev-next-link-previous')[0]['href'],
                         'https://example.test/prefix/book/')
        for order, error in [('["book/chapter_index.md","book/chapter_index.md"]', 'entries must be unique'),
                             ('["other.md"]', 'entries must belong to their collection'),
                             ('["book/missing.md"]', 'missing')]:
            self.build(config + f'reading_order={order}\n', files, expected_error=error)

    def test_reader_partial_translations_and_automatic_transparent_sections(self):
        files = {'content/_index.ru.md': markdown('Home RU')}
        for suffix in ['', '.ru']:
            files[f'content/book/_index{suffix}.md'] = markdown('Book', 'template="reader.html"\npage_template="reader.html"\nsort_by="weight"')
            files[f'content/book/first{suffix}.md'] = markdown('First', 'weight=1')
            files[f'content/book/last{suffix}.md'] = markdown('Last', 'weight=3')
            files[f'content/book/transparent/_index{suffix}.md'] = markdown('Transparent', 'transparent=true\nsort_by="weight"\npage_template="reader.html"')
            files[f'content/book/transparent/promoted{suffix}.md'] = markdown('Promoted', 'weight=2')
            files[f'content/book/part/_index{suffix}.md'] = markdown('Part', 'weight=1\ntemplate="reader.html"\npage_template="reader.html"\nsort_by="weight"')
        files['content/book/part/chapter.md'] = markdown('Untranslated chapter', 'weight=1')
        config = '[languages.ru]\n[extra.devlab.collections.book]\nsection="book/_index.md"\n'
        automatic = ['book', 'book/first', 'book/transparent/promoted', 'book/last', 'book/part', 'book/part/chapter']
        explicit = ['book', 'book/first', 'book/transparent/promoted', 'book/part', 'book/part/chapter', 'book/last']
        for sequence, options in [(automatic, ''), (explicit, 'reading_order=["book/_index.md","book/first.md","book/transparent/promoted.md","book/part/_index.md","book/part/chapter.md","book/last.md"]\n')]:
            output = self.build(config + options, files)
            for prefix in ['', 'ru/']:
                expected = [item for item in sequence if not prefix or item != 'book/part/chapter']
                for index, route in enumerate(expected):
                    with self.subTest(options=options, prefix=prefix, route=route):
                        doc = Document((output / f'{prefix}{route}/index.html').read_text())
                        links = doc.attributes('a', 'docs-nav-link')[:len(expected)]
                        self.assertEqual([a['href'] for a in links], [f'https://example.test/prefix/{prefix}{item}/' for item in expected])
                        previous = doc.attributes('a', 'docs-prev-next-link-previous')
                        next_link = doc.attributes('a', 'docs-prev-next-link-next')
                        self.assertEqual([a['href'] for a in previous], [f'https://example.test/prefix/{prefix}{expected[index-1]}/'] if index else [])
                        self.assertEqual([a['href'] for a in next_link], [f'https://example.test/prefix/{prefix}{expected[index+1]}/'] if index+1 < len(expected) else [])

    def test_localized_ui_language_routes_and_page_metadata(self):
        config = '''build_search_index=true
[languages.ru]
build_search_index=true
[languages.fr]
[extra.devlab.i18n.language_names]
en="English"
ru="Русский"
[extra.devlab.i18n.translations.ru]
search='Поиск "R&D"'
[extra.devlab.i18n.translations.fr]
toc="Sommaire"
'''
        files = {'content/_index.ru.md': markdown('Home RU'), 'content/_index.fr.md': markdown('Home FR')}
        for suffix in ['', '.ru']:
            files[f'content/plain{suffix}.md'] = markdown('Plain',
                '[extra.devlab.seo]\nog_image="/social.webp"\nog_image_alt=\'R&D "preview"\'')
            files[f'content/post{suffix}.md'] = markdown('Post', 'template="blog-page.html"\ndate=2026-09-01', '## One\nText.\n## Two\nText.')
        output = self.build(config, files)
        ru = Document((output / 'ru/plain/index.html').read_text())
        self.assertEqual(ru.attributes('a', 'skip-link')[0].get('href'), '#main-content')
        self.assertIn('Перейти к содержимому', ''.join(ru.text))
        self.assertEqual(ru.attributes('input', 'site-search-input')[0]['placeholder'], 'Поиск...')
        self.assertIn('Поиск "R&D"', ''.join(ru.text))
        self.assertEqual(ru.attributes('button', 'theme-toggle')[0]['data-theme-dark-label'], 'Тёмная тема')
        alternate = {a['hreflang']: a['href'] for a in ru.attributes('link') if 'hreflang' in a}
        self.assertEqual(alternate, {'en':'https://example.test/prefix/plain/', 'ru':'https://example.test/prefix/ru/plain/'})
        switches = [a for a in ru.attributes('a') if 'hreflang' in a]
        self.assertEqual({a['hreflang'] for a in switches}, {'en', 'ru'})
        self.assertTrue(any(a.get('aria-current') == 'page' and a['hreflang'] == 'ru' for a in switches))
        meta = {a.get('property', a.get('name')): a.get('content') for a in ru.attributes('meta')}
        self.assertEqual(meta['og:type'], 'website')
        self.assertEqual(meta['og:image'], 'https://example.test/prefix/social.webp')
        self.assertEqual(meta['og:image:alt'], 'R&D "preview"')
        self.assertEqual(meta['twitter:image:alt'], 'R&D "preview"')
        post = Document((output / 'ru/post/index.html').read_text())
        self.assertTrue(any(a.get('property') == 'og:type' and a['content'] == 'article' for a in post.attributes('meta')))
        self.assertIn('На этой странице', ''.join(post.text))
        french = Document((output / 'fr/index.html').read_text())
        self.assertIn('Skip to content', ''.join(french.text), 'missing keys must fall back to English')


    def test_translated_docs_navigation(self):
        files = {
            'content/_index.ru.md': markdown('Home RU'),
            'content/_index.fr.md': markdown('Home FR'),
        }
        for suffix, label in [('', 'EN'), ('.ru', 'RU')]:
            for path, title, front in [
                ('docs/_index', 'Docs', 'sort_by="weight"'),
                ('docs/guide/_index', 'Guide', 'sort_by="weight"\nweight=1'),
                ('docs/guide/deep/_index', 'Deep', 'sort_by="weight"\nweight=3'),
                ('docs/guide/transparent/_index', 'Transparent', 'sort_by="weight"\nweight=4\ntransparent=true'),
            ]:
                files[f'content/{path}{suffix}.md'] = markdown(
                    title + ' ' + label,
                    'template="docs.html"\npage_template="doc-page.html"\n' + front)
            for path, weight in [('docs/guide/a', 1), ('docs/guide/b', 2),
                                 ('docs/guide/deep/c', 1), ('docs/guide/transparent/d', 5)]:
                files[f'content/{path}{suffix}.md'] = markdown(path + ' ' + label, f'weight={weight}')
        # This curated section deliberately has no Russian translation.
        files['content/docs/english/_index.md'] = markdown('English only', 'weight=2\ntemplate="docs.html"')
        config = '[languages.ru]\ntitle="RU"\n[languages.fr]\ntitle="FR"\n[extra.devlab.collections.manual]\nsection="docs/_index.md"\n'
        for curated in [False, True]:
            with self.subTest(curated=curated):
                navigation = 'navigation=[{section="docs/guide/_index.md"},{section="docs/english/_index.md"}]\n' if curated else ''
                output = self.build(config + navigation, files)
                for route in ['docs/guide/a', 'docs/guide/deep/c', 'docs/guide/transparent/d']:
                    doc = Document((output / 'ru' / route / 'index.html').read_text())
                    links = doc.attributes('a', 'docs-nav-link')
                    self.assertTrue(links)
                    self.assertTrue(all('/prefix/ru/docs/' in a['href'] for a in links), links)
                    self.assertTrue(any(a.get('aria-current') == 'page' for a in links))
                    self.assertTrue(any(a['href'].endswith('/ru/docs/guide/transparent/d/') for a in links))
                    self.assertNotIn('English only', ''.join(doc.text))
                page = Document((output / 'ru/docs/guide/a/index.html').read_text())
                self.assertEqual(page.attributes('a', 'docs-prev-next-link-next')[0]['href'],
                                 'https://example.test/prefix/ru/docs/guide/b/')
                last = Document((output / 'ru/docs/guide/transparent/d/index.html').read_text())
                self.assertTrue(last.attributes('a', 'docs-prev-next-link-previous'))
                for route in ['ru/docs', 'ru/docs/guide']:
                    doc = Document((output / route / 'index.html').read_text())
                    links = doc.attributes('a', 'docs-overview-card') + doc.attributes('a', 'docs-overview-section-link')
                    self.assertTrue(links)
                    self.assertTrue(all('/prefix/ru/docs/' in a['href'] for a in links), links)
                untranslated = Document((output / 'fr/index.html').read_text())
                self.assertEqual(untranslated.attributes('a', 'docs-nav-link'), [])
                self.assertFalse(any('/fr/docs' in a.get('href', '') for a in untranslated.attributes('a')))

    def test_author_taxonomy_urls_and_profile_override(self):
        files = {
            'content/_index.ru.md': markdown('Home RU'),
            'content/blog/_index.md': markdown('Blog', 'template="blog.html"\npage_template="blog-page.html"'),
            'content/blog/_index.ru.md': markdown('Blog RU', 'template="blog.html"\npage_template="blog-page.html"'),
            'content/blog/post.md': markdown('Post', 'date=2026-09-01\n[taxonomies]\nauthors=["Alex Morgan", "external"]'),
            'content/blog/post.ru.md': markdown('Post RU', 'date=2026-09-01\n[taxonomies]\nauthors=["Alex Morgan", "external"]'),
            'data/authors.toml': '[external]\nname="External Author"\npath="https://example.org/profile?x=1&y=2"\n',
        }
        config = 'taxonomies=[{name="authors"}]\n[languages.ru]\ntaxonomies=[{name="authors"}]\n'
        output = self.build(config, files)
        for language in ['', 'ru/']:
            doc = Document((output / language / 'blog/post/index.html').read_text())
            expected = 'https://example.test/prefix/' + language + 'authors/alex-morgan/'
            self.assertEqual([a['href'] for a in doc.attributes('a', 'post-byline-author')],
                             [expected, 'https://example.org/profile?x=1&y=2'])
            self.assertEqual([a['href'] for a in doc.attributes('link') if a.get('rel') == 'author'],
                             [expected, 'https://example.org/profile?x=1&y=2'])
            profile = output / language / 'authors/alex-morgan/index.html'
            self.assertTrue(profile.exists())
            self.assertEqual(Document(profile.read_text()).attributes('a', 'author-feed-link'), [])

    def test_author_and_head_feeds(self):
        for feeds, label in [(['atom.xml'], ''), (['rss.xml'], ''),
                             (['rss.xml', 'atom.xml'], 'Публикации: {author}')]:
            with self.subTest(feeds=feeds):
                config = ('generate_feeds=true\nfeed_filenames=' + str(feeds).replace("'", '"')
                          + '\ntaxonomies=[{name="authors",feed=true}]\n')
                if label:
                    config += f'[extra.devlab.i18n.translations.en]\nauthor_feed="{label}"\n'
                files = {'content/post.md': markdown('Post', 'date=2026-09-01\n[taxonomies]\nauthors=["Alex Morgan"]')}
                output = self.build(config, files)
                doc = Document((output / 'authors/alex-morgan/index.html').read_text())
                link = doc.attributes('a', 'author-feed-link')[0]
                self.assertEqual(link['href'], 'https://example.test/prefix/authors/alex-morgan/' + feeds[0])
                self.assertEqual(link['aria-label'], (label or 'Posts by {author}').replace('{author}', 'Alex Morgan'))
                for feed in feeds:
                    self.assertTrue((output / 'authors/alex-morgan' / feed).exists())
                links = [a for a in doc.attributes('link') if a.get('rel') == 'alternate']
                self.assertEqual([a['href'].rsplit('/', 1)[-1] for a in links], feeds)
                self.assertEqual([a['type'] for a in links],
                                 ['application/atom+xml' if f == 'atom.xml' else 'application/rss+xml' for f in feeds])

    def test_single_downloads_optional_fields_and_escaping(self):
        data = '''mode = "single"
[labels]
download = 'Скачать "файл"'
[[channel]]
id = "stable"
label = "Stable"
[[channel]]
id = "preview"
label = "Preview"
[[product]]
id = "app"
name = 'App <em>literal</em>'
[[product.artifact]]
channel = "stable"
name = 'Archive <b>literal</b>'
url = '/releases/app.zip?a=1&b=2'
sha_url = '/releases/app.sha256'
status = "ready"
[[product.artifact]]
channel = "stable"
name = "Example"
url = "https://example.test/demo.zip"
sha_url = "https://example.test/demo.sha256"
demo = true
recommended = true
status = "ready"
[[product.artifact]]
channel = "preview"
name = "Unpublished"
sha_url = "https://example.test/orphan.sha256"
[verification]
commands = 'printf "<b>literal</b>"'
'''
        output = self.build(files={
            'content/downloads/_index.md': markdown('Downloads', 'template="downloads-hub.html"\n[extra]\ndata="data/releases.toml"', '**Installation notes**'),
            'data/releases.toml': data,
        })
        doc = Document((output / 'downloads/index.html').read_text())
        links = doc.attributes('a', 'dlh-download')
        self.assertEqual(len(links), 1)
        self.assertEqual(links[0]['href'], 'https://example.test/prefix/releases/app.zip?a=1&b=2')
        self.assertIn('Скачать "файл"', links[0]['aria-label'])
        self.assertFalse(doc.attributes('em'))
        self.assertFalse(doc.attributes('b'))
        self.assertIn('App <em>literal</em>', ''.join(doc.text))
        self.assertIn('printf "<b>literal</b>"', ''.join(doc.text))
        self.assertIn('Installation notes', ''.join(doc.text))
        all_hrefs = [a.get('href', '') for a in doc.attributes('a')]
        self.assertFalse(any('demo.zip' in a or 'demo.sha256' in a or 'orphan.sha256' in a for a in all_hrefs))
        panels = doc.attributes('section', 'content-tab-panel')
        self.assertEqual(len(panels), 2)
        self.assertTrue(all('hidden' not in p for p in panels))
        scripts = [a.get('src', '') for a in doc.attributes('script')]
        self.assertFalse(any('downloads-hub.js' in a for a in scripts))

    def test_catalog_routes_localization_and_empty_product(self):
        data = '''mode = "catalog"
[labels]
results = "Найдено {count} из {total}"
no_artifacts = "Файлов пока нет"
[[product]]
id = "app"
name = 'App "quoted"'
url = "@/catalog/app.ru.md"
'''
        output = self.build('default_language="en"\n[languages.ru]\n', {
            'content/catalog/_index.md': markdown('Catalog'),
            'content/catalog/_index.ru.md': markdown('Каталог', 'template="downloads-hub.html"\n[extra]\ndata="data/releases.toml"'),
            'content/catalog/app.md': markdown('App'),
            'content/catalog/app.ru.md': markdown('Приложение', 'template="downloads-product.html"\n[extra]\ndata="data/releases.toml"\nproduct="app"'),
            'data/releases.toml': data,
        })
        doc = Document((output / 'ru/catalog/index.html').read_text())
        cards = doc.attributes('a', 'dlh-card')
        self.assertEqual(cards[0]['href'], 'https://example.test/prefix/ru/catalog/app/')
        self.assertEqual(cards[0]['data-name'], 'App "quoted"')
        self.assertNotIn('hidden', cards[0])
        self.assertIn('hidden', doc.attributes('div', 'dlh-toolbar')[0])
        self.assertEqual(doc.attributes('section', 'dlh-catalog')[0]['data-result-label'], 'Найдено {count} из {total}')
        self.assertTrue(any(a.get('src', '').endswith('/js/downloads-hub.js') for a in doc.attributes('script')))
        detail = Document((output / 'ru/catalog/app/index.html').read_text())
        self.assertIn('Файлов пока нет', ''.join(detail.text))
        self.assertIn('https://example.test/prefix/ru/catalog/', [a['href'] for a in detail.attributes('a')])
        self.assertFalse(detail.attributes('a', 'dlh-download'))
        self.assertFalse(detail.attributes('img'))

    def test_downloads_reject_invalid_data(self):
        valid = '[[channel]]\nid="stable"\nlabel="Stable"\n[[product]]\nid="app"\nname="App"\n'
        cases = [
            ('mode="typo"\n' + valid, 'mode must be single or catalog'),
            ('mode="single"\n', 'exactly one'),
            ('mode="catalog"\n' + valid, 'need a url'),
            (valid + '[[product]]\nid="app"\nname="Duplicate"\n', 'product IDs must be unique'),
            ('[[channel]]\nid="stable"\nlabel="Again"\n' + valid, 'channel IDs must be unique'),
            (valid + '[[product.artifact]]\nchannel="missing"\nname="Archive"\n', 'artifact.channel must match'),
            (valid + '[[product.artifact]]\nchannel="stable"\nname=""\n', 'non-empty name'),
        ]
        for data, error in cases:
            with self.subTest(error=error):
                self.build(files={
                    'content/downloads/_index.md': markdown('Downloads', 'template="downloads-hub.html"\n[extra]\ndata="data/releases.toml"'),
                    'data/releases.toml': data,
                }, expected_error=error)
        self.build(files={
            'content/catalog/_index.md': markdown('Catalog'),
            'content/catalog/app.md': markdown('App', 'template="downloads-product.html"\n[extra]\ndata="data/releases.toml"\nproduct="missing"'),
            'data/releases.toml': valid,
        }, expected_error='must match exactly one product')

    def test_single_downloads_documentation_recipe(self):
        # Execute the copy-ready quick start so documentation cannot drift from the template.
        import re
        guide = (REPO / 'content/docs/guides/downloads.md').read_text()
        snippets = re.findall(r'```toml\n(.*?)\n```', guide, re.S)
        output = self.build(files={
            'content/downloads/_index.md': snippets[0],
            'data/downloads.toml': snippets[1],
        })
        doc = Document((output / 'downloads/index.html').read_text())
        self.assertEqual(len(doc.attributes('li', 'dlh-artifact')), 2)
        self.assertFalse(doc.attributes('a', 'dlh-download'))

    def test_downloads_hero_preview_and_card_sections(self):
        data = '''mode = "single"
[[channel]]
id = "stable"
label = "Stable"
[[product]]
id = "app"
name = "App"
vendor = "Vendor A"
[product.status]
label = "Compatibility"
title = "Before you install"
items = [
  { label = "Engine", value = "Zola 0.23.6 or newer" },
  { label = "Dependencies", value = "Zola only; no Node.js" },
]
[[product.artifact]]
channel = "stable"
name = "Archive"
variant = "tar.gz"
url = "https://example.test/app.tar.gz"
status = "ready"
[[product.artifact]]
channel = "stable"
name = "Archive"
variant = "zip"
sha_url = "/releases/app.zip.sha256"
status = "ready"
[[mirror]]
name = "Mirror one"
url = "/mirrors/app"
[verification]
title = "Verify the archive"
commands = 'sha256sum -c app.tar.gz.sha256'
'''
        output = self.build(files={
            'content/downloads/_index.md': markdown('Downloads', 'template="downloads-hub.html"\n[extra]\ndata="data/releases.toml"'),
            'data/releases.toml': data,
        })
        html = (output / 'downloads/index.html').read_text()
        doc = Document(html)

        # The wide preview follows the copy block, so the hero is media-right.
        classes = [set(attrs.get('class', '').split()) for _, attrs in doc.elements]
        copy_index = next(i for i, cls in enumerate(classes) if 'dlh-product-copy' in cls)
        visual_index = next(i for i, cls in enumerate(classes) if 'dlh-product-visual' in cls)
        self.assertLess(copy_index, visual_index)

        # Status, verification and mirrors share the card-section layout.
        sections = doc.attributes('section', 'dlh-card-section')
        self.assertEqual([s['aria-labelledby'] for s in sections],
                         ['dlh-status-title', 'dlh-verification-title', 'dlh-mirrors-title'])
        self.assertEqual(len(doc.attributes('div', 'dlh-status-item')), 2)

        # Every artifact is a card with its own metadata and action row.
        self.assertEqual(len(doc.attributes('li', 'dlh-artifact')), 2)
        self.assertEqual(len(doc.attributes('dl', 'dlh-artifact-meta')), 2)
        self.assertEqual(len(doc.attributes('span', 'dlh-unavailable')), 1)

        tabs = doc.attributes('div', 'content-tabs')
        self.assertEqual([t['data-tabs-sync'] for t in tabs], ['downloads-channel'])

        mirrors = doc.attributes('li', 'dlh-mirror-card')
        self.assertEqual(len(mirrors), 1)
        self.assertEqual(doc.attributes('a', 'dlh-mirror-name')[0]['href'],
                         'https://example.test/prefix/mirrors/app')
        self.assertTrue(doc.attributes('code', 'dlh-mirror-url'))

        # Keeping the block inside .content is what attaches the copy button.
        self.assertIn('<div class="content"><pre class="dlh-code"><code>sha256sum -c app.tar.gz.sha256</code></pre></div>', html)

    def test_catalog_filter_controls_are_rendered_at_build_time(self):
        import re
        data = '''mode = "catalog"
[featured]
title = "Featured"
[[support]]
id = "maintained"
label = "Maintained"
[[support]]
id = "planned"
label = "Planned"
[[channel]]
id = "stable"
label = "Stable"
[[product]]
id = "alpha"
name = "Alpha"
vendor = "Vendor B"
support = "maintained"
url = "@/catalog/alpha.md"
[[product]]
id = "beta"
name = "Beta"
vendor = "Vendor A"
support = "maintained"
url = "@/catalog/beta.md"
[[product]]
id = "gamma"
name = "Gamma"
support = "planned"
url = "@/catalog/gamma.md"
'''
        files = {
            'content/catalog/_index.md': markdown('Catalog', 'template="downloads-hub.html"\n[extra]\ndata="data/releases.toml"'),
            'data/releases.toml': data,
        }
        for product in ('alpha', 'beta', 'gamma'):
            files[f'content/catalog/{product}.md'] = markdown(
                product.title(), f'template="downloads-product.html"\n[extra]\ndata="data/releases.toml"\nproduct="{product}"')
        output = self.build(files=files)
        html = (output / 'catalog/index.html').read_text()

        vendor_select = re.search(r'id="dlh-vendor".*?</select>', html, re.S).group(0)
        self.assertEqual(re.findall(r'<option value="([^"]*)"', vendor_select),
                         ['all', 'Vendor A', 'Vendor B'])
        self.assertEqual(re.search(r'data-dlh-count="maintained">(\d+)<', html).group(1), '2')
        self.assertEqual(re.search(r'data-dlh-count="planned">(\d+)<', html).group(1), '1')

        see_all = Document(html).attributes('a', 'dlh-featured-more')
        self.assertEqual(see_all[0]['href'], '#dlh-product-grid')
        self.assertIn('data-dlh-see-all', see_all[0])

        script = (REPO / 'static/js/downloads-hub.js').read_text()
        self.assertIn("data-dlh-see-all", script)
        self.assertNotIn("createElement('option')", script)

    def test_minimal_footer_replacement(self):
        # The customization guide promises a self-contained replacement, so the
        # documented example is executed here and must work on every include site.
        footer = '''{% set footer_site = config.title %}
{% if footer_compact is defined and footer_compact %}
<p class="my-footer-min">{{ footer_site }}</p>
{% else %}
<footer class="my-footer">
  <p>&copy; {{ now() | date(format="%Y") }} {{ footer_site }}</p>
  <nav aria-label="Footer"><a href="{{ get_url(path='/') }}">Home</a></nav>
</footer>
{% endif %}
'''
        output = self.build(
            '[extra.devlab.footer]\nshow = false\n'
            '[extra.devlab.collections.manual]\nsection = "docs/_index.md"\n',
            {
                'templates/partials/footer.html': footer,
                'content/docs/_index.md': markdown('Docs', 'template="docs.html"\npage_template="doc-page.html"'),
                'content/docs/page.md': markdown('Page'),
            })
        home = Document((output / 'index.html').read_text())
        self.assertTrue(home.attributes('footer', 'my-footer'))
        self.assertFalse(home.attributes('footer', 'site-footer'))

        # The Docs sidebar and the mobile Docs panel use the compact branch.
        docs = Document((output / 'docs/page/index.html').read_text())
        self.assertTrue(docs.attributes('p', 'my-footer-min'))
        self.assertFalse(docs.attributes('footer', 'my-footer'))

    def test_compact_docs_footer_uses_icon_links(self):
        import re
        data = ('[extra.devlab.footer]\nnote = "Demo note"\nshow_link_names = true\n'
                'links = [\n'
                '  { name = "Community", path = "/community/", icon = "link" },\n'
                '  { name = "Atom feed", path = "/atom.xml", icon = "rss" },\n'
                ']\n')
        output = self.build(
            data + '[extra.devlab.collections.manual]\nsection = "docs/_index.md"\n',
            {
                'content/docs/_index.md': markdown('Docs', 'template="docs.html"\npage_template="doc-page.html"'),
                'content/docs/page.md': markdown('Page'),
            })
        html = (output / 'docs/page/index.html').read_text()

        # The desktop Docs sidebar and the mobile Docs panel keep the note and
        # copyright above a row of icon-only links.
        compact = re.findall(r'<footer class="site-footer site-footer-compact">.*?</footer>', html, re.S)
        self.assertEqual(len(compact), 2)
        for block in compact:
            self.assertIn('Demo note', block)
            self.assertIn('site-footer-copyright', block)
            self.assertEqual(block.count('site-footer-link"'), 2)
            # The visible name moves to the accessible name of the icon link.
            self.assertIn('aria-label="Community"', block)
            self.assertIn('aria-label="Atom feed"', block)
            self.assertNotIn('site-footer-link-name', block)
            self.assertNotIn('site-footer-links-title', block)

        # A Docs page has no page-level footer, so the compact pair above is all of it.
        # A regular page keeps the page footer with the visible names the site asked for.
        home = (output / 'index.html').read_text()
        full = re.findall(r'<footer class="site-footer">.*?</footer>', home, re.S)
        self.assertEqual(len(full), 1)
        self.assertIn('site-footer-links-title', full[0])
        self.assertEqual(full[0].count('site-footer-link-name'), 2)


if __name__ == "__main__":
    unittest.main()
