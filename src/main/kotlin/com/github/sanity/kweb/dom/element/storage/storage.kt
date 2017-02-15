package com.github.sanity.kweb.dom.element.storage

import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.dom.storage.StorageReceiver
import com.github.sanity.kweb.dom.storage.StorageType

/**
 * Created by ian on 2/12/17.
 */

val Element.localStorage get() = StorageReceiver(this.rootReceiver, StorageType.local)

val Element.sessionStorage get() = StorageReceiver(this.rootReceiver, StorageType.session)