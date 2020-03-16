package kweb.client

data class Server2ClientMessage(
        val yourId: String,
        val debugToken: String?,
        val execute: Execute? = null,
        val evaluate: Evaluate? = null,
        val instructions: List<Instruction>? = null
) {

    data class Instruction(val type: Type, val parameters: List<Any?>) {
        enum class Type {
            SetAttribute,
            CreateElement,
            SetText,
            AddText,
            RemoveAttribute
        }
    }

    data class Execute(val js: String)
    data class Evaluate(val js: String, val callbackId: Int)
}