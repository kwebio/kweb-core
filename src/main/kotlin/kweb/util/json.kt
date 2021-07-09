package kweb.util

import kotlinx.serialization.json.JsonPrimitive

val String.json get() = JsonPrimitive(this)
val Number.json get() = JsonPrimitive(this)
val Boolean.json get() = JsonPrimitive(this)
