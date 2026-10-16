// Exercise the actual Zola-generated index with the vendored browser plugins.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const [output, language, query, expectedTitle] = process.argv.slice(2);
const context = vm.createContext({ console });
context.window = context;
const scripts = ['elasticlunr.min.js'];
if (language === 'ru' || language === 'de') {
  scripts.push('js/search-language-support.js', 'vendor/lunr-languages/lunr.stemmer.support.min.js',
               `vendor/lunr-languages/lunr.${language}.min.js`);
}
scripts.push(`search_index.${language}.js`);
for (const script of scripts) {
  vm.runInContext(fs.readFileSync(path.join(output, script), 'utf8'), context);
}
const index = context.elasticlunr.Index.load(context.searchIndex);
const titles = index.search(query).map(result => index.documentStore.getDoc(result.ref).title);
assert(titles.includes(expectedTitle), `${language}: no ${expectedTitle} result for ${query}`);
