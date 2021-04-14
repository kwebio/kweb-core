package kweb

import mu.KotlinLogging
import java.time.Duration

/**
 * A central configuration class for Kweb parameterization
 *
 * Please note this is not [Kweb.Feature.Configuration], which is a Ktor specific config block
 *
 */
object KwebConfiguration {
    private val logger = KotlinLogging.logger {}

    /**
     * See [Duration.parse] for valid formats, e.g PT5S, or PT48H
     */
    val BUILDPAGE_TIMEOUT: Duration =
        Accessor.getProperty("kweb.buildpage.timeout")?.let { Duration.parse(it) }
            ?: Duration.ofSeconds(5)

    /**
     * Don't put this too low, you may end up cleaning semi-active clients
     */
    val CLIENT_STATE_TIMEOUT: Duration =
        Accessor.getProperty("kweb.client.state.timeout")?.let { Duration.parse(it) }
            ?: Duration.ofHours(48)

    /**
     * Values are initialized eagerly, but objects are not, so be sure to "touch" this class
     * on initialization for failing fast.
     *
     * We can also add some smarter validation here later if needed
     */
    fun validate() {
        logger.debug("Configuration has been initialized successfully")
    }

    private object Accessor {
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
            System.getProperty(key, env[key] ?: env[key.toUpperCase().replace('.', '_')])
    }
}