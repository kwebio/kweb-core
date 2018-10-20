package io.kweb.dom.element.storage

import io.kweb.Kweb
import io.kweb.dom.Document
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

/**
 * Allows data to be stored in and retrieved from the browser's [local storage](https://www.w3schools.com/html/html5_webstorage.as).
 *
 * @sample local_storage_sample
 */
val Document.localStorage get() = StorageReceiver(receiver, StorageType.local)

/**
 * Allows data to be stored in and retrieved from the browser's [session storage](https://www.w3schools.com/html/html5_webstorage.as).
 */
val Document.sessionStorage get() = StorageReceiver(receiver, StorageType.session)

private fun local_storage_sample() {
    Kweb(port = 14189) {
        doc.localStorage["time"] = System.currentTimeMillis()
        GlobalScope.launch {
            val username : CompletableFuture<String?> = doc.sessionStorage["user_name"]
            println("Read username: ${username.await()}")
        }
    }
}