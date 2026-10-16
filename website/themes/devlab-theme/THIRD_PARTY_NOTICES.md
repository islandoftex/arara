# Third-party notices

## SVG icons

DevLab vendors its icons in `templates/components/icons/`; they are rendered
inline and require no CDN or JavaScript package at build or runtime.

| Library | Version | License | Source |
| --- | --- | --- | --- |
| Tabler Icons | 3.48.0 | MIT | [repository](https://github.com/tabler/tabler-icons/tree/v3.48.0), [SVG mapping](third_party/tabler-icons/SOURCES.md) |
| Simple Icons | 16.34.0 | CC0-1.0 (library) | [repository](https://github.com/simple-icons/simple-icons/tree/16.34.0), [SVG sources](third_party/simple-icons/SOURCES.md) |

Tabler supplies the interface icons, default terminal header mark and favicon.
Simple Icons supplies the GitHub, GitLab, Codeberg, Matrix, Zulip, Telegram and
Discord logos. Their path geometry is preserved; SVG wrappers add theme classes,
explicit sizes and accessibility attributes. Brand icons inherit `currentColor`.

License copies are stored in `third_party/tabler-icons/` and
`third_party/simple-icons/`. Simple Icons' upstream disclaimer and individual
brand source, license and guideline metadata are retained alongside its license.

## Diagrams

DevLab vendors a browser bundle at `static/vendor/beautiful-mermaid/beautiful-mermaid.min.js`. It is loaded only by pages that render a `devlab.diagram` component.

The bundle was produced with esbuild 0.25.9 from these unmodified packages.
Its entrypoint exports only `renderMermaidSVG`, the function used by the theme.
Unused upstream exports are removed during bundling.

| Package | Version | License | Source |
| --- | --- | --- | --- |
| beautiful-mermaid | 1.1.3 | MIT | [repository](https://github.com/lukilabs/beautiful-mermaid), [npm package](https://www.npmjs.com/package/beautiful-mermaid/v/1.1.3) |
| elkjs | 0.11.1 | EPL-2.0 | [repository](https://github.com/kieler/elkjs), [npm package](https://www.npmjs.com/package/elkjs/v/0.11.1) |
| entities | 7.0.1 | BSD-2-Clause | [repository](https://github.com/fb55/entities), [npm package](https://www.npmjs.com/package/entities/v/7.0.1) |

Copies of their licenses are stored under `third_party/`. Corresponding source is available from the linked repositories and from each exact package version in the npm registry.

The vendored bundle is not required to develop or build a DevLab site. Node.js and npm were used to create this checked-in browser asset. Theme consumers build with Zola alone.

The entrypoint is `export { renderMermaidSVG } from 'beautiful-mermaid';`.
The bundle uses esbuild's browser platform, ES2020 target, ESM format,
minification, tree shaking and no legal comments. Full license texts remain
in `third_party/`.

SHA-256 of the bundle:
`c3492563c1bc6c1c70d08029a9cbaafa72cb804305ef2e4872636b817ff599d4`.

## Search language plugins

German and Russian search use Lunr Languages 1.22.0 under MPL-1.1.
The unmodified browser modules are in `static/vendor/lunr-languages/`.
Original source, the full license and recorded hashes are in
[the source notice](third_party/lunr-languages/README.md).
These source files are retained in the repository and do not enter the site output.
