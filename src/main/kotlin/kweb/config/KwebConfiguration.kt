package kweb.config

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kweb.Kweb
import mu.KotlinLogging
import java.time.Duration
import java.util.*

/**
 * A configuration class for Kweb parameterization. Extend this if you have custom needs
 * on how to inject configuration values, otherwise [KwebDefaultConfiguration] is probably
 * good enough for your use case
 *
 * Please note this is not [Kweb.Feature.Configuration], which is a Ktor specific config block
 */
abstract class KwebConfiguration {
    private val logger = KotlinLogging.logger {}

    /**
     * If [Kweb.debug] is enabled, then pages that take longer than [buildpageTimeout]
     * to load will display a warning message
     *
     * See [Duration.parse] for valid formats, e.g PT5S, or PT48H
     */
    abstract val buildpageTimeout: Duration

    /**
     * Enable stats for the client state cache. Small performance penalty per operation,
     * large gains in observability. Consider this in production.
     */
    abstract val clientStateStatsEnabled: Boolean

    /**
     * Clients that last connected more than [clientStateTimeout] will be cleaned
     * up every minute.
     *
     * Don't put this too low, you may end up cleaning semi-active clients, e.g someone
     * who left a page open for a long duration without actions, such as a monitoring page.
     */
    abstract val clientStateTimeout: Duration

    /**
     * Values are initialized eagerly, but objects are not, so be sure to "touch" this class
     * on initialization for failing fast.
     *
     * We can also add some smarter validation here later if needed
     */
    fun validate() {
        logger.debug { "Configuration has been initialized successfully" }
    }


    /**
     * Override this function to handle uncaught exceptions of client callbacks.
     * E.g. if the browser sends a websocket message back to the kweb instance
     * and the message handler throws an uncaught exception, kweb will invoke
     * this exception handler to expose the fact that a message could not be
     * properly handled
     */
    open fun onWebsocketMessageHandlingFailure(ex: Exception){

    }

    /**
     * Override the default robots.txt behavior, which is to return with a 404. Passed a Ktor [ApplicationCall]
     * which may be used to return whatever you wish.
     *
     * // @sample robots_txt_sample
     */
    open suspend fun robotsTxt(call: ApplicationCall) {
        call.response.status(HttpStatusCode.NotFound)
        call.respondText("robots.txt has not been specified)")
    }

    /**
     * Override the default robots.txt behavior, which is to return with a 404. Passed a Ktor [ApplicationCall]
     * which may be used to return whatever you wish.
     *
     * // @sample favicon_sample
     */
    open suspend fun faviconIco(call: ApplicationCall) {
        call.respondText("favicons not currently supported by kweb", status = HttpStatusCode.NotFound)
    }

    /**
     * Message that is shown to a disconnected client.
     *
     */
    open val clientOfflineToastTextTemplate: String = "Connection to server lost, attempting to reconnect"

    protected object Accessor {
        private val env = System.getenv()

        /**
         * Gets the value of the given property by searching runtime properties,
         * then env, then uppercase ENV. Each case is only examined if the previous one failed.
         *
         * So, if you provide a [key] value such as "kweb.test", the following will happen
         * - First "kweb.test" will be looked up as a run argument
         * - Then "kweb.test" is looked up in env variables (the JVM is kinda bad at lowercase ENV vars in my experience)
         * - Then "KWEB_TEST" is looked up in env variables
         * - Then null is returned
         */
        fun getProperty(key: String): String? =
            System.getProperty(key, env[key] ?: env[key.uppercase(Locale.getDefault()).replace('.', '_')])
    }
}