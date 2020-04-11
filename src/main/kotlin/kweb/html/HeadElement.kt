package kweb.html

import kweb.Element
import kweb.WebBrowser

class HeadElement(webBrowser: WebBrowser, id: String? = null) : Element(webBrowser, null, "document.head", "head", id)
open class TitleElement(parent: Element) : Element(parent)
