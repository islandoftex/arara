"""Regression checks for staging unchanged legacy Markdown on Tera 2."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('prepare', Path(__file__).with_name('prepare-website.py'))
prepare = importlib.util.module_from_spec(spec)
spec.loader.exec_module(prepare)


class PreparationTest(unittest.TestCase):
    def adapt(self, body):
        return prepare.adapt('+++\ntitle = "Existing"\nweight = 3\n+++\n' + body)

    def test_preserves_front_matter(self):
        self.assertTrue(self.adapt('Hello').startswith('+++\ntitle = "Existing"\nweight = 3\n+++\n'))

    def test_protects_literal_template_examples(self):
        result = self.adapt('```lua\n{{ value }}\n{% something %}\n```')
        self.assertIn('{% raw %}\n```lua\n{{ value }}\n{% something %}\n```{% endraw %}', result)

    def test_preserves_logo_and_url_shortcodes(self):
        result = self.adapt('{{ tex() }} {{ latex() }} {{ get_url(url="/images/a.svg") }}')
        self.assertIn('<span class="tex">T<sub>e</sub>X</span>', result)
        self.assertIn('<span class="latex">L<sup>a</sup>T<sub>e</sub>X</span>', result)
        self.assertIn("{{< arara.url url={'/images/a.svg'} />}}", result)

    def test_nested_callouts_close_the_correct_component(self):
        result = self.adapt('{% hint() %}outer {% messagebox(title="Inside") %}inner{% end %}{% end %}')
        self.assertIn('{% </arara.messagebox> %}{% </arara.hint> %}', result)

    def test_indented_callout_dedents_body_without_changing_code_indentation(self):
        result = self.adapt('    {% hint() %}\n A note\n\n ```yaml\n value: >\n     command\n ```\n    {% end %}')
        self.assertIn("indent={'    '} dedent={' '}", result)
        self.assertIn('     command', result)

    def test_rejects_unbalanced_callouts(self):
        for source in ('{% end %}', '{% hint() %}unfinished'):
            with self.subTest(source=source), self.assertRaises(ValueError):
                self.adapt(source)

    def test_staging_keeps_all_repository_content_unchanged(self):
        repo = Path(__file__).resolve().parents[1]
        sources = {p: p.read_bytes() for p in (repo / 'website/content').rglob('*.md')}
        with tempfile.TemporaryDirectory() as temp:
            output = Path(temp) / 'site'
            prepare.prepare(repo, output, repo / 'website/themes/devlab-theme')
            self.assertEqual(len(sources), len(list((output / 'content').rglob('*.md'))))
            for source, original in sources.items():
                self.assertEqual(original, source.read_bytes())
                generated = output / source.relative_to(repo / 'website')
                self.assertEqual(original.decode().split('+++', 2)[1],
                                 generated.read_text().split('+++', 2)[1])


if __name__ == '__main__':
    unittest.main()
