package com.github.sanity.kweb

import com.google.gson.Gson
import org.apache.commons.lang3.StringEscapeUtils
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Created by ian on 1/7/17.
 */


val random = Random()

val gson = Gson()

val scheduledExecutorService = Executors.newScheduledThreadPool(5)

fun wait(delay: Long, unit : TimeUnit, toRun : () -> Unit): ScheduledFuture<*> = scheduledExecutorService.schedule(toRun, delay, unit)

fun String.escapeEcma() = StringEscapeUtils.escapeEcmaScript(this)

fun Any.toJson(): String = gson.toJson(this)
