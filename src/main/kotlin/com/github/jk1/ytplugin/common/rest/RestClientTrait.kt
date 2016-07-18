package com.github.jk1.ytplugin.common.rest

import com.github.jk1.ytplugin.common.components.ComponentAware
import com.intellij.tasks.impl.BaseRepository
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import java.net.URLEncoder


interface RestClientTrait : ComponentAware {

    fun createHttpClient(): HttpClient {
        return createHttpClient(taskManagerComponent.getActiveYouTrackRepository())
    }

    fun createHttpClient(repository: BaseRepository): HttpClient {
        val client = taskManagerComponent.getRestClient(repository)
        val credentials = UsernamePasswordCredentials(repository.username, repository.password)
        client.state.setCredentials(AuthScope.ANY, credentials)
        return client
    }

    fun <R> connect(closeable: HttpMethod, block: (HttpMethod) -> R): R {
        try {
            return block(closeable);
        } finally {
            closeable.releaseConnection()
        }
    }

    val String.urlencoded: String
        get() = URLEncoder.encode(this, "UTF-8")
}