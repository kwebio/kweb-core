package com.github.sanity.kweb

import com.google.gson.Gson
import org.apache.commons.lang3.StringEscapeUtils
import java.util.*

/**
 * Created by ian on 1/7/17.
 */


val random = Random()

val gson = Gson()

fun String.escapeEcma() = StringEscapeUtils.escapeEcmaScript(this)

//fun String.quote() = "\"$this\""

fun Any.toJson(): String = gson.toJson(this)
