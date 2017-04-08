package io.kweb.dom.element.read

import io.kweb.dom.element.Element

/**
 * Created by ian on 1/13/17.
 */


val Element.read: ElementReader get() = ElementReader(webBrowser, jsExpression)
