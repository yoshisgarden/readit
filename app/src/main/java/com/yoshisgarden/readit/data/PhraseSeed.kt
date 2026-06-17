package com.yoshisgarden.readit.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** JSON shape of assets/phrases_v1.json. */
@Serializable
data class PhraseJson(
    val id: Long,
    val english: String,
    val japanese: String,
    val category: String,
    val example_en: String,
    val example_ja: String,
)

object PhraseSeed {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Bump this whenever the *content* of phrases_v1.json changes without the entry
     * count changing. Existing installs re-import the bundled phrases when their stored
     * seed version is older than this (favorites & SRS progress are preserved).
     */
    const val SEED_VERSION = 3

    /** Reads and parses the bundled seed phrases. */
    fun load(context: Context): List<Phrase> {
        val text = context.assets.open("phrases_v1.json")
            .bufferedReader().use { it.readText() }
        val now = System.currentTimeMillis()
        return json.decodeFromString<List<PhraseJson>>(text).map {
            Phrase(
                id = it.id,
                english = it.english,
                japanese = it.japanese,
                category = it.category,
                exampleEn = it.example_en,
                exampleJa = it.example_ja,
                createdAt = now,
            )
        }
    }
}
