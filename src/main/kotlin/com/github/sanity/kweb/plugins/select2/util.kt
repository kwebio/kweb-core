package com.github.sanity.kweb.plugins.select2

import com.github.sanity.kweb.toJson

/**
 * Created by ian on 2/22/17.
 */

internal fun MutableMap<String, String>.jsPut(key : String, value : Any?) {
    if (value != null) {
        put(key, value.toJson())
    }
}

internal fun Map<String, Any>.toJs() : String {
    //return this.toJson()
    return "{"+this.entries.map {"${it.key} : ${it.value}"}.joinToString(separator = ", ")+"}"
}