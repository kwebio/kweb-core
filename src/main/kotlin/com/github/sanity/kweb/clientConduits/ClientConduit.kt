package com.github.sanity.kweb.clientConduits

import com.google.gson.Gson
import org.apache.commons.lang3.StringEscapeUtils
import java.util.concurrent.CompletableFuture

/**
 * Created by ian on 1/1/17.
 */
val gson = Gson()

abstract class ClientConduit(open val rh: CCReceiver.() -> Boolean) {
    abstract fun send(clientId: Long, message: ClientMessage)
    abstract fun send(clientId: Long, messages: List<ClientMessage>)

    class ClientMessage(val msg: String, val responseHandler: ((String) -> Boolean)?)
}

class CCReceiver(private val clientId: Long, private val cc: ClientConduit, val response: String? = null) {
    fun execute(js: String) {
        cc.send(clientId, ClientConduit.ClientMessage(js, null))
    }

    fun execute(js: String, rh: CCReceiver.() -> Boolean) {
        cc.send(clientId, ClientConduit.ClientMessage(js, { rh.invoke(CCReceiver(clientId, cc, it)) }))
    }

    fun executeWithFuture(js: String): CompletableFuture<String> {
        val cf = CompletableFuture<String>()
        execute(js) {
            cf.complete(response)
            false
        }
        return cf
    }

    fun doc() = Document(this)
}

class Element(private val receiver: CCReceiver, private val jsExpression: String) {

    fun setAttribute(name: String, value : Any) {
        receiver.execute("""
            $jsExpression.setAttribute("${name.escapeEcma()}", ${if (value is String) value.escapeEcma() else value});
        """)
    }

    fun setInnerHTML(value : String) {
        receiver.execute("""
            $jsExpression.innerHTML="${value.escapeEcma()}";
        """)
    }

    fun readAttribute(name : String) : CompletableFuture<String> {
        val cf = CompletableFuture<String>()
        receiver.execute(
                "%respond%($jsExpression.getAttribute(\"${name.escapeEcma()}\"));") {
            cf.complete(response)
            true
        }
        return cf
    }

    fun readInnerHtml() : CompletableFuture<String> {
        val cf = CompletableFuture<String>()
        receiver.execute(
                "%respond%($jsExpression.innerHTML);") {
            cf.complete(response)
            true
        }
        return cf
    }

    fun read(): CompletableFuture<ReadableElement> {
        val cf = CompletableFuture<ReadableElement>()
        receiver.execute(
                """
                {
                    var element=$jsExpression;
                    %respond%({"tagName":element.tagName, "attributes":element.attributes});
                }
            """) {
            data class JsonResponse(val tagName: String, val attributes: Map<String, Object>)
            val jsonObj = gson.fromJson(response, JsonResponse::class.java)
            cf.complete(ReadableElement(jsonObj.tagName, jsonObj.attributes))
            true
        }
        return cf
    }
}

class ReadableElement(val tag: String, val attributes: Map<String, Object>)

class Document(private val receiver: CCReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    fun body() = Element(receiver, "document.body")
}

private fun String.escapeEcma() = StringEscapeUtils.escapeEcmaScript(this)