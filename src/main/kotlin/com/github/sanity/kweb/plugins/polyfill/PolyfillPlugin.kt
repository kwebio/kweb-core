package com.github.sanity.kweb.plugins.polyfill

import com.github.sanity.kweb.plugins.KWebPlugin

/**
 * Created by ian on 1/21/17.
 */
class PolyfillPlugin : KWebPlugin() {
    // TODO: Support specifying features, see https://polyfill.io/v2/docs/

    override fun decorate(startHead: StringBuilder, endHead: StringBuilder) {
        startHead.append("""<script src="https://cdn.polyfill.io/v2/polyfill.min.js"></script>""")
    }
}

val polyfill = PolyfillPlugin()