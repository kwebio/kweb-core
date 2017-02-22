package com.github.sanity.kweb

import com.github.sanity.kweb.plugins.KWebPlugin
import java.util.*

/**
 * Created by ian on 1/1/17.
 */

abstract class ClientConduit(open val createPage: RootReceiver.() -> Unit, internal open val plugins: List<KWebPlugin>) {
    val appliedPlugins = HashSet<KWebPlugin>()

    abstract fun execute(clientId: String, message: String)

    abstract fun evaluate(clientId: String, expression: String, handler: (String) -> Unit)

    abstract fun executeWithCallback(clientId: String, js: String, callbackId: Int, handler: (String) -> Unit)
}

class ReadableElement(val tag: String, val attributes: Map<String, Any>)

