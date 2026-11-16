// Statischer Worker: der Build liegt in env.ASSETS. Die Datenschutz-Seite ist ein Verzeichnis
// (dist/datenschutz/index.html) und wird von den Assets unter /datenschutz ausgeliefert –
// ein eigener Rewrite würde dort eine Weiterleitungsschleife auslösen.
export default {
  async fetch(request, env) {
    return env.ASSETS.fetch(request);
  },
};
