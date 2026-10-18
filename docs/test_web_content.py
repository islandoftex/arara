"""Checks for the stable website-to-PDF extraction contract."""
import importlib.util
from pathlib import Path
import unittest
import tempfile
import os
import shutil
import subprocess

spec = importlib.util.spec_from_file_location('extract', Path(__file__).with_name('extract-web-content.py'))
extract = importlib.util.module_from_spec(spec)
spec.loader.exec_module(extract)


class ExtractionTest(unittest.TestCase):
    def document(self, body):
        return extract.Document('<nav>Not in the book</nav><h1 data-arara-document-title>Chapter</h1>'
                                '<div data-arara-document-content>' + body + '</div><footer>Footer</footer>')

    def test_only_extracts_the_article_body(self):
        document = self.document('<h1 id="example">Example</h1><p>Keep <em>this</em>.</p>')
        self.assertEqual(document.get('title'), 'Chapter')
        self.assertEqual(document.get('content'), '<h1 id="example">Example</h1><p>Keep <em>this</em>.</p>')

    def test_preserves_code_entities_comments_and_void_elements(self):
        body = '<!--example--><pre>&lt;rule&gt; &amp; value\n  indented</pre><br><img src="example.svg"/>'
        self.assertEqual(self.document(body).get('content'), body)

    def test_missing_or_duplicate_markers_fail(self):
        for source in ('<div>Old theme</div>',
                       '<h1 data-arara-document-title>Title</h1><div data-arara-document-content>Missing end',
                       '<h1 data-arara-document-title>Title</h1><h1 data-arara-document-title>Again</h1>'
                       '<div data-arara-document-content>Body</div>'):
            with self.subTest(source=source), self.assertRaises(ValueError):
                extract.Document(source)

    def test_pdf_assets_resolve_locally_and_missing_assets_fail(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            (root / 'image.svg').write_text('<svg/>')
            body = '<img src="https://example.org/arara/image.svg">'
            self.assertIn((root / 'image.svg').as_uri(),
                          extract.localize_assets(body, root, 'https://example.org/arara'))
            with self.assertRaises(ValueError):
                extract.localize_assets(body.replace('image.svg', 'missing.svg'), root,
                                        'https://example.org/arara')

    def test_empty_content_fails(self):
        with self.assertRaises(ValueError):
            self.document('  \n').get('content')


class BookScriptTest(unittest.TestCase):
    def test_scripts_read_immutable_site_without_altering_it(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            docs = root / 'docs'
            (docs / 'resources').mkdir(parents=True)
            (docs / 'resources/version.txt').write_text('test-version')
            binaries = root / 'bin'
            binaries.mkdir()
            converter = binaries / 'weasyprint'
            # Exercise the shell assembly without typesetting on every unit run.
            converter.write_text('#!' + shutil.which('bash') + '\ncp "$3" "$4"\n')
            converter.chmod(0o755)
            source = '<nav>Not in the book</nav><h1 data-arara-document-title>Chapter</h1>' \
                     '<div data-arara-document-content><h1 id="section">Section</h1>' \
                     '<p>Keep this example.</p></div>'
            chapters = ('introduction', 'concepts', 'cli', 'configuration', 'logging',
                        'projects', 'methods', 'rules', 'building', 'deploying', 'yaml', 'mvel')
            pages = [root / 'website/public/manual' / chapter / 'index.html' for chapter in chapters]
            pages.append(root / 'website/public/quickstart/index.html')
            for page in pages:
                page.parent.mkdir(parents=True)
                page.write_text(source)
                page.chmod(0o444)
            env = dict(os.environ, PATH=str(binaries) + os.pathsep + os.environ['PATH'])
            for name in ('htmlmanualtopdf.sh', 'htmlquickstarttopdf.sh'):
                subprocess.run(['bash', str(Path(__file__).with_name(name).resolve())],
                               cwd=docs, env=env, check=True, capture_output=True)
            for page in pages:
                self.assertEqual(page.read_text(), source)
            for name in ('arara-manual.pdf', 'arara-quickstart.pdf'):
                assembled = (docs / name).read_text()
                self.assertIn('Keep this example.', assembled)
                self.assertNotIn('Not in the book', assembled)
            pages[0].unlink()
            failed = subprocess.run(['bash', str(Path(__file__).with_name('htmlmanualtopdf.sh').resolve())],
                                    cwd=docs, env=env, capture_output=True)
            self.assertNotEqual(failed.returncode, 0)


if __name__ == '__main__':
    unittest.main()
