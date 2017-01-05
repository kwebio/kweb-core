package com.github.sanity.kweb

/**
 * Created by ian on 1/3/17.
 */

abstract class RemoteNode() {
    var parent: RemoteNode? = null
    var siblingIndex: Int = 0
}
