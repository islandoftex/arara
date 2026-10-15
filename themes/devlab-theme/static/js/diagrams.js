const diagramElements = Array.from(document.querySelectorAll('[data-devlab-diagram]'));

function decodeSource(encodedSource) {
  const binary = window.atob(encodedSource);
  const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0));

  return new TextDecoder().decode(bytes).trim();
}

function sanitizeSvg(svgMarkup) {
  const parser = new DOMParser();
  const svgDocument = parser.parseFromString(svgMarkup, 'image/svg+xml');

  if (svgDocument.querySelector('parsererror')) {
    throw new Error('The diagram renderer returned invalid SVG.');
  }

  svgDocument.querySelectorAll('script, foreignObject, iframe, object, embed').forEach((element) => {
    element.remove();
  });

  svgDocument.querySelectorAll('style').forEach((style) => {
    style.textContent = style.textContent.replace(
      /@import\s+(?:url\(\s*(?:"[^"]*"|'[^']*'|[^)]*)\s*\)|"[^"]*"|'[^']*')[^;]*;/gi,
      '',
    );
  });

  svgDocument.querySelectorAll('*').forEach((element) => {
    Array.from(element.attributes).forEach((attribute) => {
      if (/^on/i.test(attribute.name)) {
        element.removeAttribute(attribute.name);
        return;
      }

      if ((attribute.name === 'href' || attribute.name === 'xlink:href') && !attribute.value.startsWith('#')) {
        element.removeAttribute(attribute.name);
      }
    });
  });

  const svg = svgDocument.documentElement;
  svg.classList.add('diagram-svg');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('focusable', 'false');
  svg.removeAttribute('width');
  svg.removeAttribute('height');

  return document.importNode(svg, true);
}

function showError(diagram, source) {
  const stage = diagram.querySelector('[data-devlab-diagram-stage]');
  const message = document.createElement('p');
  const sourceBlock = document.createElement('pre');
  const code = document.createElement('code');

  message.className = 'diagram-error-message';
  message.textContent = diagram.dataset.diagramError || 'Diagram could not be rendered.';
  code.textContent = source;
  sourceBlock.appendChild(code);
  stage.replaceChildren(message, sourceBlock);
  stage.removeAttribute('aria-busy');
  diagram.dataset.diagramState = 'error';
}

function renderDiagram(diagram, renderMermaidSVG) {
  let source = '';

  try {
    source = decodeSource(diagram.dataset.diagramSource || '');

    if (!source) {
      throw new Error('The diagram source is empty.');
    }

    const svgMarkup = renderMermaidSVG(source, {
      bg: 'var(--diagram-bg)',
      fg: 'var(--diagram-fg)',
      line: 'var(--diagram-line)',
      accent: 'var(--diagram-accent)',
      muted: 'var(--diagram-muted)',
      surface: 'var(--diagram-surface)',
      border: 'var(--diagram-border)',
      font: 'system-ui',
      transparent: true,
      padding: 28,
      nodeSpacing: 30,
      layerSpacing: 46,
      componentSpacing: 30,
    });

    const stage = diagram.querySelector('[data-devlab-diagram-stage]');
    stage.replaceChildren(sanitizeSvg(svgMarkup));
    stage.removeAttribute('aria-busy');
    diagram.dataset.diagramState = 'ready';
  } catch (_) {
    showError(diagram, source);
  }
}

if (diagramElements.length) {
  import('../vendor/beautiful-mermaid/beautiful-mermaid.min.js')
    .then(({ renderMermaidSVG }) => {
      diagramElements.forEach((diagram) => renderDiagram(diagram, renderMermaidSVG));
    })
    .catch(() => {
      diagramElements.forEach((diagram) => {
        let source = '';

        try {
          source = decodeSource(diagram.dataset.diagramSource || '');
        } catch (_) {
          // The fallback still remains useful when the encoded source is malformed.
        }

        showError(diagram, source);
      });
    });
}
