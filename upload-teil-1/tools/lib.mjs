export const q = (prompt, options, answer, why) => ({ prompt, options, answer, why });

export const chunk = (en, de, patternId, template, templateDe, slot, tip = '') => ({
  en, de, patternId, template, templateDe, slot, tip,
});

export const L = (
  title, goal, pillarId, connectorLevel, dialog, questions, chunks, roleplay, quiz, noticing = '',
  recycles = [],
) => ({ title, goal, pillarId, connectorLevel, dialog, questions, chunks, roleplay, quiz, noticing, recycles });

export const S = (id, pathId, title, subtitle, scene, icon, lessons, extra = {}) => ({
  id, pathId, title, subtitle, scene, icon, lessons, ...extra,
});
