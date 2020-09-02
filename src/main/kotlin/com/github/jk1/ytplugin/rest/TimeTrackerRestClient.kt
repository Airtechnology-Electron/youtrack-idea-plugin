package com.github.jk1.ytplugin.rest

import com.github.jk1.ytplugin.logger
import com.github.jk1.ytplugin.tasks.YouTrackServer
import com.github.jk1.ytplugin.timeTracker.TrackerNotification
import com.github.jk1.ytplugin.timeTracker.TrackerNotifier
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*


class TimeTrackerRestClient(override val repository: YouTrackServer) : RestClientTrait, ResponseLoggerTrait {

    fun postNewWorkItem(issueId: String, time: String, type: String) : Int {
        val getGroupsUrl = "${repository.url}/api/issues/${issueId}/timeTracking/workItems"

        val method = PostMethod(getGroupsUrl)
        method.params.contentCharset = "UTF-8"

        val res: URL? = this::class.java.classLoader.getResource("post_work_item_body.json")
        val jsonBody = res?.readText()
                ?.replace("\"{minutes}\"", time, true)
                ?.replace("\"{date}\"", (Date().time).toString(), true)
                ?.replace("\"{type}\"", type, true)


        method.requestEntity = StringRequestEntity(jsonBody, "application/json", StandardCharsets.UTF_8.name())
        var status = 0
        method.connect {
            status = httpClient.executeMethod(method)
            when (status) {
                200 -> {
                    logger.debug("Successfully posted")
                }
            }
        }
        return status
    }
}