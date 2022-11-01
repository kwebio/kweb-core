package kweb.docs

import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kweb.*
import kweb.util.json

fun main() {
    Kweb(port = 16097) {
        // ANCHOR: alert
doc.body {
    browser.callJsFunction("""alert("Hello World!")""")
}
        // ANCHOR_END: alert

        // ANCHOR: parameters
doc.body {
    val greeting = "Hello".json
    val name = "World".json
    browser.callJsFunction("""alert({} + " " + {} + "!")""", greeting, name)
}
        // ANCHOR_END: parameters

        // ANCHOR: with_result
doc.body {
    elementScope().launch {
        val result : JsonElement = browser.callJsFunctionWithResult("Date.now()")
        println("Result: ${result.jsonPrimitive.intOrNull}")
    }
}
        // ANCHOR_END: with_result
    }
}
