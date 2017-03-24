package com.github.sanity.kweb.plugins.materialdesignlite.loading

import com.github.sanity.kweb.dom.attributes.attr
import com.github.sanity.kweb.dom.attributes.classes
import com.github.sanity.kweb.dom.element.creation.DivCreator
import com.github.sanity.kweb.dom.element.events.on
import com.github.sanity.kweb.plugins.materialdesignlite.MDLCreator
import com.github.sanity.kweb.plugins.materialdesignlite.events.mdlComponentUpgraded

/**
 * See https://getmdl.io/components/index.html#loading-section/progress
 */

fun MDLCreator.progressBar(initialProgress : Int = 0, indeterminate : Boolean = false, attributes : Map<String, Any> = attr): ProgressBar {
    val progressBar = ProgressBar(div(attributes
            .classes("mdl-progress", "mdl-js-progress")
            .classes("mdl-progress--indeterminate", onlyIf = indeterminate)
    ))
    progressBar.element.on.mdlComponentUpgraded {
        progressBar.setProgress(initialProgress)
    }
    return progressBar
}

class ProgressBar(val element: DivCreator) {
    fun setProgress(progress : Int) {
        element.execute("${element.jsExpression}.MaterialProgress.setProgress($progress);")
    }
}