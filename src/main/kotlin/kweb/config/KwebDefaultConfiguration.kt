package kweb.config

import java.time.Duration

/**
 * A default [KwebConfiguration] using runtime arguments
 */
open class KwebDefaultConfiguration : KwebConfiguration() {

    override val buildpageTimeout: Duration =
            Accessor.getProperty("kweb.buildpage.timeout")?.let { Duration.parse(it) }
                    ?: Duration.ofSeconds(5)

    override val clientStateStatsEnabled: Boolean =
        Accessor.getProperty("kweb.client.state.stats.enabled")?.toBooleanStrictOrNull()
            ?: false

    override val clientStateTimeout: Duration =
            Accessor.getProperty("kweb.client.state.timeout")?.let { Duration.parse(it) }
                    ?: Duration.ofHours(1)
}