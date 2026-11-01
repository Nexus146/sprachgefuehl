package de.sprachgefuehl.app.data

import kotlinx.serialization.Serializable

@Serializable
data class ContentBundle(
    val version: Int = 1,
    val world: WorldInfo = WorldInfo(),
    val personas: List<Persona> = emptyList(),
    val paths: List<PathDef> = emptyList(),
    val situations: List<Situation> = emptyList(),
    val lessons: List<Lesson> = emptyList(),
    val pillars: List<Pillar> = emptyList(),
    val connectorLevels: List<ConnectorLevel> = emptyList(),
    val functions: List<FunctionDef> = emptyList(),
    val errorCards: List<ErrorCard> = emptyList(),
    val minimalPairs: List<MinimalPair> = emptyList(),
    val patterns: List<PatternDef> = emptyList(),
    val chunks: List<Chunk> = emptyList(),
    val canDo: List<CanDo> = emptyList(),
    val placement: List<PlacementItem> = emptyList(),
    val checkup: List<CheckupItem> = emptyList()
)

@Serializable
data class WorldInfo(
    val name: String = "Dein Viertel",
    val intro: String = "",
    val seasons: List<SeasonDef> = emptyList()
)

@Serializable
data class SeasonDef(
    val id: String,
    val title: String,
    val note: String = ""
)

/** A recurring neighbour: fixed voice, fixed places, so dialogues stay consistent. */
@Serializable
data class Persona(
    val id: String,
    val name: String,
    val role: String,
    val voice: String = "",
    val note: String = "",
    val situations: List<String> = emptyList()
)

@Serializable
data class PathDef(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val icon: String = "leaf",
    val order: Int = 0
)

@Serializable
data class Situation(
    val id: String,
    val pathId: String,
    val title: String,
    val subtitle: String = "",
    val scene: String = "",
    val level: String = "A1",
    val icon: String = "chat",
    val order: Int = 0,
    val place: String = "",
    val personaIds: List<String> = emptyList(),
    val season: String = "",
    val story: String = "",
    /** Same goal, different framing: the learner gets one of these, not all of them. */
    val variants: List<SituationVariant> = emptyList()
)

/**
 * A variant changes who, where and why – never what is learned. Goals, form and chunks stay the
 * same, so the path remains fair no matter which story the learner meets.
 */
@Serializable
data class SituationVariant(
    val id: String,
    val story: String,
    val scene: String = "",
    val place: String = "",
    val personaId: String = "",
    val greetingEn: String = "",
    val greetingDe: String = "",
    val interestId: String = ""
)

@Serializable
data class Lesson(
    val id: String,
    val situationId: String,
    val title: String,
    val goal: String,
    val level: String = "A1",
    val order: Int = 0,
    val pillarId: String = "",
    val connectorLevel: Int = 1,
    val steps: List<Step> = emptyList(),
    val chunkIds: List<String> = emptyList(),
    val recycles: List<String> = emptyList(),
    val xpMinutes: Int = 10
)

@Serializable
data class Step(
    val id: String,
    val type: String,
    val title: String = "",
    val promptEn: String = "",
    val promptDe: String = "",
    val audio: String = "",
    val options: List<Option> = emptyList(),
    val target: String = "",
    val accept: List<String> = emptyList(),
    val requiredForm: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val model: String = "",
    val hint: String = "",
    val tip: String = "",
    val explanationDe: String = "",
    val note: String = "",
    val lines: List<DialogLine> = emptyList(),
    val questions: List<Question> = emptyList(),
    val patternId: String = "",
    val connectorId: String = "",
    val pillarId: String = "",
    val errorCardId: String = "",
    val slots: List<String> = emptyList(),
    val chunkId: String = "",
    val reveal: String = ""
)

@Serializable
data class Option(
    val text: String = "",
    val label: String = "",
    val correct: Boolean = false,
    val icon: String = ""
)

@Serializable
data class DialogLine(
    val speaker: String = "you",
    val en: String = "",
    val de: String = "",
    val highlight: String = ""
)

@Serializable
data class Question(
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val answer: Int = 0,
    val why: String = ""
)

@Serializable
data class Chunk(
    val id: String,
    val en: String,
    val de: String,
    val patternId: String = "",
    val slot: String = "",
    val tip: String = "",
    val level: String = "A1",
    val tags: List<String> = emptyList()
)

@Serializable
data class PatternDef(
    val id: String,
    val template: String,
    val de: String,
    val pillarId: String = "",
    val register: String = "neutral",
    val chunks: List<String> = emptyList(),
    val related: List<String> = emptyList()
)

@Serializable
data class Pillar(
    val id: String,
    val title: String,
    val metaphor: String,
    val explanationDe: String,
    val examples: List<String> = emptyList(),
    val quiz: List<Question> = emptyList(),
    val order: Int = 0,
    val connectorLevel: Int = 0
)

@Serializable
data class ConnectorLevel(
    val id: String,
    val level: Int,
    val words: String,
    val explanationDe: String,
    val contrastDe: String,
    val examples: List<PairText> = emptyList()
)

@Serializable
data class PairText(
    val a: String = "",
    val b: String = ""
)

@Serializable
data class FunctionDef(
    val id: String,
    val title: String,
    val whenDe: String,
    val phrases: List<PairText> = emptyList(),
    val tipsDe: String = ""
)

@Serializable
data class ErrorCard(
    val id: String,
    val type: String,
    val rule: String = "",
    val title: String,
    val observation: String,
    val whyDe: String,
    val correctEn: String,
    val anchor: String,
    val checklist: List<String> = emptyList(),
    val nextStep: String = "",
    val examples: List<String> = emptyList(),
    val pillarId: String = ""
)

@Serializable
data class MinimalPair(
    val id: String,
    val a: String,
    val b: String,
    val ipaA: String = "",
    val ipaB: String = "",
    val tipDe: String
)

@Serializable
data class CanDo(
    val id: String,
    val lessonId: String,
    val text: String
)

@Serializable
data class PlacementItem(
    val id: String,
    val promptDe: String,
    val audioText: String,
    val options: List<String>,
    val answer: Int,
    val level: Int
)

/**
 * One task of the standort check. [skill] is one of hoeren, grammatik, bausteine, sprechen,
 * verknuepfen; [level] runs from 0 (start) to 3 (free).
 */
@Serializable
data class CheckupItem(
    val id: String,
    val skill: String,
    val level: Int,
    val type: String,
    val promptDe: String,
    val audioEn: String = "",
    val textEn: String = "",
    val options: List<String> = emptyList(),
    val answer: Int = 0,
    val model: String = "",
    val accept: List<String> = emptyList(),
    val whyDe: String = "",
    val pillarId: String = "",
    val chunkId: String = ""
)
