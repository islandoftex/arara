// Lunr language modules expect this factory; Zola's Elasticlunr engine
// processes string tokens and does not provide it.
window.elasticlunr.generateStopWordFilter = function (words) {
  const stopWords = new Set(words);
  return function (token) {
    return token && !stopWords.has(token) ? token : undefined;
  };
};
