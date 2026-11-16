// Liest die Quellen der App und schreibt daraus kleine, typisierte Dateien für die Website.
// So stehen auf der Seite nur Angaben, die aus dem Projekt selbst stammen:
//   content.json     → Kapitel, Situationen, Zahlen
//   build.gradle.kts → kleinste unterstützte Android-Version
//   docs/datenschutz.md → die vollständige Datenschutzerklärung
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { marked } from 'marked';

const here = dirname(fileURLToPath(import.meta.url));
const root = resolve(here, '..');
const read = (path) => readFileSync(resolve(here, path), 'utf8');
const target = (path) => resolve(root, path);
const write = (path, text) => {
  mkdirSync(dirname(target(path)), { recursive: true });
  writeFileSync(target(path), text, 'utf8');
};

const content = JSON.parse(read('../../app/src/main/assets/content/content.json'));

const paths = [...content.paths].sort((a, b) => a.order - b.order);
const lessonsBySituation = new Map();
for (const lesson of content.lessons) {
  lessonsBySituation.set(lesson.situationId, (lessonsBySituation.get(lesson.situationId) ?? 0) + 1);
}

const chapters = paths.map((path) => {
  const situations = content.situations.filter((situation) => situation.pathId === path.id);
  return {
    id: path.id,
    order: path.order,
    title: path.title,
    subtitle: path.subtitle,
    situations: situations.length,
    lessons: situations.reduce((sum, situation) => sum + (lessonsBySituation.get(situation.id) ?? 0), 0),
    detail: situations.map((situation) => ({
      title: situation.title,
      subtitle: situation.subtitle,
      lessons: lessonsBySituation.get(situation.id) ?? 0,
    })),
  };
});

const canDo = new Set((content.canDo ?? []).map((entry) => entry.text));

// Die kleinste Android-Version steht im Gradle-Build; hier wird sie nur gelesen.
const ANDROID_NAMES = {
  26: '8.0',
  27: '8.1',
  28: '9',
  29: '10',
  30: '11',
  31: '12',
  32: '12L',
  33: '13',
  34: '14',
  35: '15',
  36: '16',
};
const gradle = read('../../app/build.gradle.kts');
const minSdk = Number(gradle.match(/minSdk\s*=\s*(\d+)/)?.[1] ?? 0);
const minAndroid = ANDROID_NAMES[minSdk] ?? `Android ${minSdk}`;

const stats = {
  chapters: content.paths.length,
  situations: content.situations.length,
  lessons: content.lessons.length,
  chunks: content.chunks.length,
  patterns: content.patterns.length,
  canDo: canDo.size,
  errorCards: (content.errorCards ?? []).length,
  minimalPairs: (content.minimalPairs ?? []).length,
  variants: content.situations.reduce((sum, situation) => sum + (situation.variants ?? []).length, 0),
  maxVariants: Math.max(0, ...content.situations.map((situation) => (situation.variants ?? []).length)),
  checkup: (content.checkup ?? []).length,
  personas: content.personas.length,
  minSdk,
  minAndroid,
};

const seasons = content.world.seasons.map((season) => ({ id: season.id, title: season.title, note: season.note }));
const personas = content.personas.map((persona) => ({ name: persona.name, role: persona.role, note: persona.note }));

write(
  'src/data/content.generated.ts',
  `// Erzeugt von scripts/build-content.mjs aus app/src/main/assets/content/content.json und
// app/build.gradle.kts. Nicht von Hand bearbeiten – mit "npm run content" neu erzeugen.

export type ChapterDetail = { title: string; subtitle: string; lessons: number };
export type Chapter = {
  id: string;
  order: number;
  title: string;
  subtitle: string;
  situations: number;
  lessons: number;
  detail: ChapterDetail[];
};
export type Season = { id: string; title: string; note: string };
export type Persona = { name: string; role: string; note: string };

export const world = ${JSON.stringify({ name: content.world.name, intro: content.world.intro }, null, 2)} as const;

export const stats = ${JSON.stringify(stats, null, 2)} as const;

export const chapters: Chapter[] = ${JSON.stringify(chapters, null, 2)};

export const seasons: Season[] = ${JSON.stringify(seasons, null, 2)};

export const personas: Persona[] = ${JSON.stringify(personas, null, 2)};
`,
);

// Die Datenschutz-Seite ist keine zweite Fassung, sondern die Erklärung aus dem Repository.
const privacyHtml = marked.parse(read('../../docs/datenschutz.md'));

write(
  'src/data/datenschutz.generated.ts',
  `// Erzeugt von scripts/build-content.mjs aus docs/datenschutz.md.
// Nicht von Hand bearbeiten – der Text wird im Repository gepflegt.

export const datenschutzHtml = ${JSON.stringify(privacyHtml)};
`,
);

console.log(
  `content.generated.ts: ${chapters.length} Kapitel, ${stats.situations} Situationen, ${stats.lessons} Lektionen, ab Android ${minAndroid}`,
);
console.log(`datenschutz.generated.ts: ${privacyHtml.length} Zeichen aus docs/datenschutz.md`);
