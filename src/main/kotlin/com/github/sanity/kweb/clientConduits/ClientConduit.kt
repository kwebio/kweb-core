package com.github.sanity.kweb.clientConduits

import com.google.gson.Gson
import org.apache.commons.lang3.StringEscapeUtils
import java.util.concurrent.CompletableFuture

/**
 * Created by ian on 1/1/17.
 */
val gson = Gson()

abstract class ClientConduit(open val rh: CCReceiver.() -> Boolean) {
    abstract fun execute(clientId: String, message: String)

    abstract fun evaluate(clientId: String, expression: String, handler: (String) -> Boolean)

}

class CCReceiver(private val clientId: String, private val cc: ClientConduit, val response: String? = null) {
    fun execute(js: String) {
        cc.execute(clientId, js)
    }


    fun evaluate(js: String): CompletableFuture<String> {
        val cf = CompletableFuture<String>()
        evaluateWithCallback(js) {
            cf.complete(response)
            false
        }
        return cf
    }


    fun evaluateWithCallback(js: String, rh: CCReceiver.() -> Boolean) {
        cc.evaluate(clientId, js, { rh.invoke(CCReceiver(clientId, cc, it)) })
    }

    val doc = Document(this)
}

class Element(private val receiver: CCReceiver, private val jsExpression: String) {

    private fun <O> evaluate(js: String, outputMapper: (String) -> O): CompletableFuture<O>? {
        return receiver.evaluate(js).thenApply(outputMapper)
    }

    fun setAttribute(name: String, value: Any) {
        receiver.execute("$jsExpression.setAttribute(\"${name.escapeEcma()}\", ${if (value is String) value.escapeEcma() else value});")
    }

    fun setInnerHTML(value: String) {
        receiver.execute("""
            $jsExpression.innerHTML="${value.escapeEcma()}";
        """)
    }

    fun readAttribute(name: String): CompletableFuture<String> {
        return receiver.evaluate("($jsExpression.getAttribute(\"${name.escapeEcma()}\"));")
    }

    fun readInnerHtml(): CompletableFuture<String> {
        return receiver.evaluate("($jsExpression.innerHTML);")

    }

    fun read(): CompletableFuture<ReadableElement> {
        data class JsonResponse(val tagName: String, val attributes: Map<String, Object>)

        val cf = CompletableFuture<ReadableElement>()
        val response = receiver.evaluate(
                "({\"tagName\":$jsExpression.tagName, \"attributes\":$jsExpression.attributes})")
        return response.thenApply { json ->
            val jr = gson.fromJson(json, JsonResponse::class.java)
            ReadableElement(jr.tagName, jr.attributes)
        }

    }
}

class ReadableElement(val tag: String, val attributes: Map<String, Object>)

class Document(private val receiver: CCReceiver) {
    fun getElementById(id: String) = Element(receiver, "document.getElementById(\"$id\")")

    val body = Element(receiver, "document.body")
}

private fun String.escapeEcma() = StringEscapeUtils.escapeEcmaScript(this)