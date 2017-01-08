package com.github.sanity.kweb.clientConduits

/**
 * Created by ian on 1/1/17.
 */

abstract class ClientConduit() {
    abstract fun execute(clientId: String, message: String)

    abstract fun evaluate(clientId: String, expression: String, handler: (String) -> Boolean)

    abstract fun executeWithCallback(clientId: String, js: String, callbackId: Int, handler: (String) -> Boolean)

}

