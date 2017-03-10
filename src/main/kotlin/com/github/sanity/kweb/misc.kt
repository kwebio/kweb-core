package com.github.sanity.kweb

import com.github.sanity.kweb.dom.element.Element
import com.google.gson.Gson
import org.apache.commons.lang3.StringEscapeUtils
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.jvmName

/**
 * Created by ian on 1/7/17.
 */


val random = Random()

val gson = Gson()

val scheduledExecutorService = Executors.newScheduledThreadPool(5)

fun wait(delay: Long, unit : TimeUnit, toRun : () -> Unit): ScheduledFuture<*> = scheduledExecutorService.schedule(toRun, delay, unit)

fun String.escapeEcma() = StringEscapeUtils.escapeEcmaScript(this)

fun Any.toJson(): String = gson.toJson(this)

fun <T> warnIfBlocking(maxTimeMs: Long, onBlock : (Thread) -> Unit, f : () -> T) : T {
    val runningThread = Thread.currentThread()
    val watcher = scheduledExecutorService.schedule(object : Runnable {
        override fun run() {
            onBlock(runningThread)
        }

    }, maxTimeMs, TimeUnit.MILLISECONDS)
    val r = f()
    watcher.cancel(false)
    return r
}

fun Array<StackTraceElement>.pruneAndDumpStackTo(logStatementBuilder: StringBuilder) {
    val disregardClassPrefixes = listOf(KWeb::class.jvmName, RootReceiver::class.jvmName, Element::class.jvmName, "org.wasabifx", "io.netty", "java.lang")
    this.filter { ste -> ste.lineNumber >= 0 && !disregardClassPrefixes.any { ste.className.startsWith(it) } }.forEach { stackTraceElement ->
        logStatementBuilder.appendln("        at ${stackTraceElement.className}.${stackTraceElement.methodName}(${stackTraceElement.fileName}:${stackTraceElement.lineNumber})")
    }
}