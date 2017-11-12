package com.github.jk1.ytplugin.commands

import com.github.jk1.ytplugin.issues.model.Issue

/**
 * Resolves and encodes persistent issue id once per command dialog. Encoded database id is required
 * by the most effective command suggestion REST method in YouTrack. YouTrack 5.2 does not expose
 * persistent ids via rest, making this optimization impossible.
 */
class CommandSession(val issue: Issue) {

    val compressedEntityId = when (issue.entityId) {
        null -> null
        else -> encodePersistentId(issue.entityId.split("-")[1].toInt())
    }

    fun hasEntityId() = compressedEntityId != null

    /**
     * Type id is removed, the remainder is split into 6-bit chunks, each encoded as a single symbol.
     */
    private fun encodePersistentId(number: Int, buffer: String = ""): String = when {
        number > 0 -> encodePersistentId(number.shr(6), buffer) + toChar(number.and(63))
        else -> buffer
    }

    /**
     * ASCII table conversion:
     * [0, 9]   -> ASCII 48-57, numbers
     * [10, 35] -> ASCII 65-89, uppercase letters
     * [36, 63] -> ASCII 97-122, lowercase letters
     */
    private fun toChar(number: Int) = when {
        number < 10 -> 48 + number
        number < 36 -> 55 + number
        else -> 61 + number
    }.toChar().toString()

}