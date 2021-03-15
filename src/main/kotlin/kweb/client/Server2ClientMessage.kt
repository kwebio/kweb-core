package kweb.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable//(with = Server2ClientMsgSerializer::class)
@SerialName("Server2ClientMessage")
data class Server2ClientMessage(
        val yourId: String,
        var debugToken: String? = null,
        val jsId: Int? = null, //the id used to store or receive a js function in cache.
                                // Can be null in a special case that will skip caching and just run javascript code
        val js: String? = null, //the js function
        val parameters: String? = null, //null if we are executing a cached function
        val callbackId: Int? = null, //null if we are executing a function without a callback
        val arguments: List<kotlinx.serialization.json.JsonElement>? = null //null if we are executing a function with no arguments
)//parameters is a comma separated string of parameters for the js function

/*@Serializable
private class Server2ClientMsgSurrogate(
    val yourId: String,
    var debugToken: String? = null,
    val jsId: Int? = null,
    val jsBody: String? = null,
    val parameters: String? = null,
    val callbackId: Int? = null,
    var arguments: List<ArgumentSurrogate>? = null
)

@Serializable
@SerialName("Argument")
private sealed class ArgumentSurrogate() {
    @Serializable
    class StringValue(val value: String) : ArgumentSurrogate()
    @Serializable
    class IntValue(val value: Int) : ArgumentSurrogate()
    @Serializable
    class FloatValue(val value: Float) : ArgumentSurrogate()
    @Serializable
    class DoubleValue(val value: Double) : ArgumentSurrogate()
    @Serializable
    class ShortValue(val value: Short) : ArgumentSurrogate()
    @Serializable
    class LongValue(val value: Long) : ArgumentSurrogate()
    @Serializable
    class BoolValue(val value: Boolean) : ArgumentSurrogate()
    @Serializable
    class CharValue(val value: Char) : ArgumentSurrogate()
    @Serializable
    class ByteValue(val value: Byte) : ArgumentSurrogate()
}

object Server2ClientMsgSerializer : KSerializer<Server2ClientMessage> {
    override val descriptor: SerialDescriptor
        get() = Server2ClientMessage.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Server2ClientMessage) {
        val msgSurrogate = Server2ClientMsgSurrogate(value.yourId, value.debugToken, value.jsId,
        value.js, value.parameters, value.callbackId)
        value.arguments?.let {
            msgSurrogate.arguments = value.arguments.map {
                when (it) {
                    is String -> ArgumentSurrogate.StringValue(it)
                    is Boolean -> ArgumentSurrogate.BoolValue(it)
                    is Int -> ArgumentSurrogate.IntValue(it)
                    is Float -> ArgumentSurrogate.FloatValue(it)
                    is Double -> ArgumentSurrogate.DoubleValue(it)
                    is Long -> ArgumentSurrogate.LongValue(it)
                    is Short -> ArgumentSurrogate.ShortValue(it)
                    is Byte -> ArgumentSurrogate.ByteValue(it)
                    is Char -> ArgumentSurrogate.CharValue(it)
                    else -> error("Argument is an ${it!!::class.simpleName}")                }
            }
        }
        encoder.encodeSerializableValue(Server2ClientMsgSurrogate.serializer(), msgSurrogate)
    }

    override fun deserialize(decoder: Decoder): Server2ClientMessage {
        TODO("Not yet implemented")
    }
}*/
