package com.github.jk1.ytplugin.commands

import com.github.jk1.ytplugin.ComponentAware
import com.github.jk1.ytplugin.commands.model.CommandAssistResponse
import com.github.jk1.ytplugin.commands.model.YouTrackCommand
import com.github.jk1.ytplugin.logger
import com.intellij.openapi.project.Project
import com.intellij.util.containers.hash.LinkedHashMap
import java.util.concurrent.TimeUnit

/**
 * Command assist response cache to make command completion more responsive and avoid UI lags. This is
 * especially helpful for large YouTrack installations, where command backend is known to be slow to
 * respond from time to time.
 */
class CommandSuggestResponseCache(override val project: Project) : ComponentAware {

    private val cache = SuggestResponseCache()

    companion object {
        private val CACHE_ENTRY_TTL = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES)
    }

    operator fun get(command: YouTrackCommand): CommandAssistResponse? {
        synchronized(this) {
            val key = CommandCacheKey(command.command, command.caret, command.url())
            val result = cache[key]
            if (result == null) {
                logger.debug("Command suggestion cache miss: $key")
            } else {
                logger.debug("Command suggestion cache hit: $key")
            }
            return result
        }
    }

    operator fun set(command: YouTrackCommand, value: CommandAssistResponse) {
        synchronized(this) {
            val key = CommandCacheKey(command.command, command.caret, command.url())
            logger.debug("New value added to command suggestion cache: $key")
            cache.put(key, value)
        }
    }

    private fun YouTrackCommand.url() = taskManagerComponent.getYouTrackRepository(session.issue).url

    data class CommandCacheKey(val command: String, val caret: Int, val serverUrl: String)

    inner class SuggestResponseCache : LinkedHashMap<CommandCacheKey, CommandAssistResponse>(10, true) {

        override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<CommandCacheKey, CommandAssistResponse>,
                key: CommandCacheKey, value: CommandAssistResponse):
                Boolean = this.size > 30

        override fun get(key: CommandCacheKey?): CommandAssistResponse? {
            super.get(key)?.let {
                if (Math.abs(System.currentTimeMillis() - it.timestamp) > CACHE_ENTRY_TTL) {
                    logger.debug("Stale value evicted from command suggestion cache: $key")
                    remove(key)
                }
            }
            return super.get(key)
        }
    }
}