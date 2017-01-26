package com.github.sanity.kweb.plugins.materialdesignlite.loading

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.DivElement
import com.github.sanity.kweb.dom.element.creation.div
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.plugins.materialdesignlite.MDLElement
import com.github.sanity.kweb.plugins.materialdesignlite.events.mdlComponentUpgraded

/**
 * See https://getmdl.io/components/index.html#loading-section/progress
 */

fun MDLElement.progressBar(initialProgress : Int = 0, indeterminate : Boolean = false, attributes : Map<String, Any> = attr): ProgressBarDiv {
    val progressDiv = ProgressBarDiv(div(attributes
            .classes("mdl-progress", "mdl-js-progress")
            .classes("mdl-progress--indeterminate", onlyIf = indeterminate)
    ))
    progressDiv.on.mdlComponentUpgraded {
        progressDiv.setProgress(initialProgress)
    }
    return progressDiv
}

class ProgressBarDiv(wrapped : DivElement) : DivElement(wrapped) {
    fun setProgress(progress : Int) {
        execute("$jsExpression.MaterialProgress.setProgress($progress);")
    }
}