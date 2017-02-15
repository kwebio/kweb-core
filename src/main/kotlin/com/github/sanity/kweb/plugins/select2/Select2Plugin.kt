package com.github.sanity.kweb.plugins.select2

import com.github.sanity.kweb.dom.element.creation.SelectElement
import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.plugins.jqueryCore.jqueryCore
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
import org.wasabifx.wasabi.app.AppServer
import java.util.*
import java.util.Collections.emptyList

/**
 * TODO: Rather than using AJAX, this should override the transport to use
 * TODO: the existing websocket for server-supplied suggestions.
 */

class Select2Plugin : KWebPlugin(dependsOn = setOf(jqueryCore)) {
    val postPath = "/select2${random.nextInt(Int.MAX_VALUE).toString(16)}"

    val suggestionHandlers = HashMap<String, (String) -> Suggestions>()

    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        endHead.append("""
            <link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.3/css/select2.min.css" rel="stylesheet" />
            <script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.3/js/select2.min.js"></script>
""".trimIndent())
    }

    override fun appServerConfigurator(appServer: AppServer) {
        appServer.get(postPath+"/:handlerid", {
            val handlerid = request.routeParams["handlerid"]
            val handler = suggestionHandlers[handlerid] ?: throw RuntimeException("Unknown handlerid $handlerid")
            val searchTerms = request.queryParams["q"]
            val suggestions = if (searchTerms != null) handler.invoke(searchTerms) else Suggestions(results = emptyList())
            response.send(suggestions.toJson(), "application/json")
        })
    }

    internal fun suggestionsAjaxBlock(handler: (String) -> Suggestions, delay : Int = 250, cache : Boolean = true) : Map<String, String> {
        val suggestionHandlerId = random.nextInt(Int.MAX_VALUE).toString(16)
        suggestionHandlers.put(suggestionHandlerId, handler)
        val ajaxMap = HashMap<String, String>()
        ajaxMap.apply {
            jsPut("url", postPath+"/"+suggestionHandlerId)
            jsPut("dataType", "json")
            jsPut("delay", delay)
            jsPut("cache", cache)
        }
        return ajaxMap
    }
}

data class Suggestions(val results : List<Result>, val more : Boolean? = null)

data class Result(val id : String, val text : String)

val select2 = Select2Plugin()

fun SelectElement.select2(
        placeholder : String? = null,
        allowClear : Boolean? = null,
        minimumInputLength : Int? = null,
        suggestions : ((String) -> Suggestions)? = null
) {
    val plugin = plugin(Select2Plugin::class)
    val configMap = HashMap<String, String>()
    configMap.apply {
        jsPut("placeholder", placeholder)
        jsPut("allowClear", allowClear)
        jsPut("minimumInputLength", minimumInputLength)
    }
    if (suggestions != null) {
        configMap.put("ajax", plugin.suggestionsAjaxBlock(suggestions).toJs())
    }

    val configString = configMap
    this.execute("$(\"#${this.id}\").select2(${configMap.toJs()});")
}

data class Select2Config(
        val placeholder : String? = null,
        val allowClear : Boolean? = null,
        val tags : Boolean? = null,
        val ajax : String? = null
)

private fun MutableMap<String, String>.jsPut(key : String, value : Any?) {
    if (value != null) {
        put(key, value.toJson())
    }
}

private fun Map<String, String>.toJs() : String {
    return "{"+this.entries.map {"${it.key} : ${it.value}"}.joinToString(separator = ", ")+"}"
}