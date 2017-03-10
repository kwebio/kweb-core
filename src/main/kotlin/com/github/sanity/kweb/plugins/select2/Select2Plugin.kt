package com.github.sanity.kweb.plugins.select2

import com.github.sanity.kweb.plugins.KWebPlugin
import com.github.sanity.kweb.plugins.jqueryCore.jqueryCore
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
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

/*    override fun appServerConfigurator(appServer: AppServer) {
        appServer.get(postPath+"/:handlerid", {
            val handlerid = request.routeParams["handlerid"]
            val handler = suggestionHandlers[handlerid] ?: throw RuntimeException("Unknown handlerid $handlerid")
            val searchTerms = request.queryParams["q"]
            val suggestions = if (searchTerms != null) handler.invoke(searchTerms) else Suggestions(results = emptyList())
            response.send(suggestions.toJson(), "application/json")
        })
    }*/

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

data class Suggestions(val results : List<Item>, val more : Boolean? = null)

data class Item(val id : String, val text : String)

val select2 = Select2Plugin()
