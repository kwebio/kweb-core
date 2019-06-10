package io.kweb.dev.hotswap

import mu.KotlinLogging
import org.hotswap.agent.annotation.LoadEvent
import org.hotswap.agent.annotation.OnClassLoadEvent
import org.hotswap.agent.annotation.Plugin

/**
 * WARNING:
 *
 * This is incomplete.  In particular, Kweb.refreshAllPages() must be called on a hotswap reload.
 *
 * @author Ian Clarke
 */

private val logger = KotlinLogging.logger {}

@Plugin(
        name = "KWebHotswapPlugin",
        testedVersions = arrayOf("0.0.26"),
        expectedVersions = arrayOf("0.0")
)
class KwebHotswapPlugin {

    companion object {
        private val listeners = ArrayList<() -> Unit>()

        @OnClassLoadEvent(classNameRegexp = ".*", events = arrayOf(LoadEvent.REDEFINE))
        @JvmStatic fun onAnyReload() {
            logger.debug { "Hotswap load event detected, calling listeners" }
            listeners.forEach { it.invoke() }
        }

        fun addHotswapReloadListener(listener : (() -> Unit)) {
            listeners.add(listener)
        }
    }
}