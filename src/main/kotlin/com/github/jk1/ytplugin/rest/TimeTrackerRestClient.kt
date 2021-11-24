package com.github.jk1.ytplugin.rest

import com.github.jk1.ytplugin.ComponentAware
import com.github.jk1.ytplugin.logger
import com.github.jk1.ytplugin.rest.MulticatchException.Companion.multicatchException
import com.github.jk1.ytplugin.tasks.YouTrackServer
import com.github.jk1.ytplugin.timeTracker.TrackerNotification
import com.intellij.notification.NotificationType
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit


class TimeTrackerRestClient(override val repository: YouTrackServer) : RestClientTrait, ResponseLoggerTrait {

    fun postNewWorkItem(issueId: String, time: String, type: String, comment: String, date: String) {
        val types = getAvailableWorkItemTypes()

        val storage = ComponentAware.of(repository.project).spentTimePerTaskStorage
        val method = HttpPost("${repository.url}/api/issues/${issueId}/timeTracking/workItems")
        val res: URL? = this::class.java.classLoader.getResource("post_work_item_body.json")
        val jsonBody = res?.readText()
                ?.replace("\"{minutes}\"", time, true)
                ?.replace("\"{date}\"", date, true)
                ?.replace("{authorId}", getMyIdAsAuthor(), true)
                ?.replace("{type}", type, true)
                ?.replace("{typeId}", types[type] ?: throw IllegalArgumentException("No work item type by name '$type'"), true)
                ?.replace("{comment}", comment, true)
        method.entity = jsonBody?.jsonEntity
        try {
            method.execute {
                logger.debug("Successfully posted work item ${types[type]} with time $time for issue $issueId")

                // clear saved time for issue as we post it to server now
                storage.resetSavedTimeForLocalTask(issueId)
            }

            val trackerNote = TrackerNotification()
            trackerNote.notify("Work timer stopped, spent time  $time min added to" +
                    " $issueId", NotificationType.INFORMATION)
        } catch (e: RuntimeException) {
            logger.debug(e)
            // save time in case of exceptions
            val timeInMills = TimeUnit.MINUTES.toMillis(time.toLong())
            storage.resetSavedTimeForLocalTask(issueId)
            storage.setSavedTimeForLocalTask(issueId, timeInMills)

            val trackerNote = TrackerNotification()
            trackerNote.notify("Unable to post time to YouTrack. See IDE log for details. Time $time min is saved",
                NotificationType.WARNING)
        }

    }

    private fun getMyIdAsAuthor(): String {
        return try {
            HttpGet("${repository.url}/api/admin/users/me")
                .execute {
                    it.asJsonObject.get("id").asString
                }
        } catch (e: Exception) {
            logger.debug(e)
            ""
        }
    }

    fun getAvailableWorkItemTypes(): Map<String, String> {
        val builder = URIBuilder("${repository.url}/api/admin/timeTrackingSettings/workItemTypes")
        builder.addParameter("fields", "name,id")
        val method = HttpGet(builder.build())
        return try {
            method.execute { element ->
                element.asJsonArray.associate {
                    Pair(it.asJsonObject.get("name").asString, it.asJsonObject.get("id").asString)
                }
            }
        } catch (e: Exception) {
            e.multicatchException(SocketException::class.java, UnknownHostException::class.java, SocketTimeoutException::class.java) {
                val trackerNote = TrackerNotification()
                trackerNote.notify("Connection to YouTrack server is lost, please check your network connection", NotificationType.WARNING)
                logger.warn("Connection to network lost: ${e.message}")
                mapOf()
            }
        }
    }
}