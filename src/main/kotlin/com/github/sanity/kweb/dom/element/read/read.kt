package com.github.sanity.kweb.dom.element.read

import com.github.sanity.kweb.dom.element.Element

/**
 * Created by ian on 1/13/17.
 */


val Element.read: ElementReader get() = ElementReader(receiver, jsExpression)
