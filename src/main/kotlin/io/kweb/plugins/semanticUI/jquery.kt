package io.kweb.plugins.semanticUI

import io.kweb.plugins.jqueryCore.JQueryReceiver

/**
 * Created by ian on 4/1/17.
 */

val JQueryReceiver.sUI get() = JQuerySUIReceiver(this)

class JQuerySUIReceiver(val jqueryReceiver: JQueryReceiver) {
    fun dimmer(action : DimmerAction)  {
        jqueryReceiver.execute("""dimmer('$action')""")
    }

    enum class DimmerAction {
        show, hide
    }
}
