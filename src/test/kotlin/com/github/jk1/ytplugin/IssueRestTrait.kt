package com.github.jk1.ytplugin

import com.github.jk1.ytplugin.rest.RestClientTrait
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.methods.DeleteMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.PutMethod

interface IssueRestTrait : RestClientTrait, YouTrackConnectionTrait {

    override val httpClient: HttpClient
        get() {
            val client = HttpClient()
            client.params.connectionManagerTimeout = 30000 // ms
            client.params.soTimeout = 30000 // ms
            client.params.credentialCharset = "UTF-8"
            client.params.isAuthenticationPreemptive = true
            val credentials = UsernamePasswordCredentials(username, password)
            client.state.setCredentials(AuthScope.ANY, credentials)
            return client
        }

    fun createIssue(summary: String = "summary"): String {
        val method = PutMethod("$serverUrl/rest/issue?project=$projectId&summary=${summary.urlencoded}")
        return method.connect {
            val status = httpClient.executeMethod(method)
            if (status == 201) {
                method.getResponseHeader("Location").toExternalForm().split("/").last().trim()
            } else {
                throw IllegalStateException("Unable to create issue: ${method.responseBodyAsString}")
            }
        }
    }

    fun touchIssue(id: String) {
        val method = PostMethod("$serverUrl/rest/issue/$id?summary=updatedsummary")
        return method.connect {
            val status = httpClient.executeMethod(method)
            if (status != 200) {
                throw IllegalStateException("Unable to update an issue: ${method.responseBodyAsString}")
            }
        }
    }

    fun deleteIssue(id: String) {
        val method = DeleteMethod("$serverUrl/rest/issue/$id")
        method.connect {
            val status = httpClient.executeMethod(method)
            if (status != 200) {
                throw IllegalStateException("Unable to create issue: ${method.responseBodyAsString}")
            }
        }
    }
}