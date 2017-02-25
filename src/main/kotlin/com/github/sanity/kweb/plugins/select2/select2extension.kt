package com.github.sanity.kweb.plugins.select2

import com.github.sanity.kweb.dom.element.creation.SelectElement
import java.util.*

/**
 * Created by ian on 2/22/17.
 */

fun SelectElement.select2(
        placeholder : String? = null,
        allowClear : Boolean? = null,
        minimumInputLength : Int? = null,
        tokenSeparators : List<String>? = null,
        data : List<Item>? = null,
        suggestions : ((String) -> Suggestions)? = null,
        tags : Boolean? = null
) : Select2Element {
    val plugin = plugin(Select2Plugin::class)
    val configMap = HashMap<String, String>()
    configMap.apply {
        jsPut("placeholder", placeholder)
        jsPut("allowClear", allowClear)
        jsPut("minimumInputLength", minimumInputLength)
        jsPut("tokenSeparators", tokenSeparators)
        jsPut("tags", tags)
        if (data != null) {
            jsPut("data", data)
        }
    }
    if (suggestions != null) {
        configMap.put("ajax", plugin.suggestionsAjaxBlock(suggestions).toJs())
    }

    val configString = configMap
    val configMapJS = configMap.toJs()
    this.execute("$(\"#${this.id}\").select2($configMapJS);")
    return Select2Element(this)
}
