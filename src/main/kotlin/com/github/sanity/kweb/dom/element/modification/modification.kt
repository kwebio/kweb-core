package com.github.sanity.kweb.dom.element.modification

import com.github.salomonbrys.kotson.toJson
import com.github.sanity.kweb.dom.element.Element
import com.github.sanity.kweb.escapeEcma
import com.github.sanity.kweb.random
import com.github.sanity.kweb.toJson
import java.util.*

/**
 * Created by ian on 1/13/17.
 */


/*********
 ********* Element modification functions.
 *********
 ********* These allow modification of an parent.  They are distinguished from
 ********* parent creation functions because they begin with a verb like "setString"
 ********* or "add".
 *********/

// TODO: Explicit support for global attributes from http://www.w3schools.com/tags/ref_standardattributes.asp
// TODO: These should probably be accessed via a field like parent.attr[GlobalAttributes.hidden], possibly
// TODO: using generics to ensure the correct return-type

fun Element.setAttribute(name: String, value: Any?): Element {
    if (value != null) {
        execute("$jsExpression.setAttribute(\"${name.escapeEcma()}\", ${value.toJson()});")
        if (name == "id") {
            jsExpression = "document.getElementById(${value.toJson()})"
        }
    }
    return this
}

fun Element.removeAttribute(name: String): Element {
    execute("$jsExpression.removeAttribute(\"${name.escapeEcma()}\");")
    return this
}

fun Element.setInnerHTML(value: String): Element {
    execute("$jsExpression.innerHTML=\"${value.escapeEcma()}\";")
    return this
}

fun Element.focus() : Element {
    execute("$jsExpression.focus();")
    return this
}

fun Element.setClasses(vararg value: String): Element {
    setAttribute("class", value.joinToString(separator = " ").toJson())
    return this
}

fun Element.addClasses(vararg classes: String, onlyIf : Boolean = true): Element {
    if (onlyIf) {
        for (class_ in classes) {
            if (class_.contains(' ')) {
                throw RuntimeException("Class names must not contain spaces")
            }
            execute("addClass($jsExpression, ${class_.toJson()});")
        }
    }
    return this
}

fun Element.removeClasses(vararg classes: String, onlyIf: Boolean = true): Element {
    if (onlyIf) {
        for (class_ in classes) {
            if (class_.contains(' ')) {
                throw RuntimeException("Class names must not contain spaces")
            }
            execute("removeClass($jsExpression, ${class_.toJson()});")
        }
    }
    return this
}

fun Element.activate(): Element {
    addClasses("is-active")
    return this
}

fun Element.deactivate(): Element {
    removeClasses("is-active")
    return this
}

fun Element.disable(): Element {
    setAttribute("disabled", true)
    return this
}

fun Element.enable(): Element {
    removeAttribute("disabled")
    return this
}

fun Element.removeChildren(): Element {
    execute("""
        while ($jsExpression.firstChild) {
            $jsExpression.removeChild($jsExpression.firstChild);
        }
     """.trimIndent())
    return this
}

fun Element.removeChildAt(position : Int) : Element {
    execute("$jsExpression.removeChild($jsExpression.childNodes[$position]);".trimIndent())
    return this
}

fun Element.setText(value: String): Element {
    removeChildren()
    addText(value)
    return this
}

fun Element.addText(value: String): Element {
    execute("""
                {
                    var ntn=document.createTextNode("${value.escapeEcma()}");
                    $jsExpression.appendChild(ntn);
                }
        """)
    return this
}

fun Element.addEventListener(eventName: String, returnEventFields : Set<String> = Collections.emptySet(), callback: (String) -> Unit): Element {
    val callbackId = Math.abs(random.nextInt())
    val eventObject = "{"+returnEventFields.map {"\"$it\" : event.$it"}.joinToString(separator = ", ")+"}"
    val js = jsExpression + """
            .addEventListener(${eventName.toJson()}, function(event) {
                callbackWs($callbackId, $eventObject);
            });
        """
    rootReceiver.executeWithCallback(js, callbackId) { payload ->
        callback.invoke(payload)
    }
    return this
}


fun Element.delete() {
    execute("$jsExpression.parentNode.removeChild($jsExpression)")
}
