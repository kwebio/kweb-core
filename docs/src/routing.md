# URL Routing

In a web application, routing is the process of using URLs to drive the
user interface (UI). URLs are a prominent feature in every web browser,
and have several main functions:

-   Bookmarking - Users can bookmark URLs in their web browser to save
    content they want to come back to later.
-   Sharing - Users can share content with others by sending a link to a
    certain page.
-   Navigation - URLs are used to drive the web browser's back/forward
    functions.

Traditionally, visiting a different URL within the same website would
cause a new page to be downloaded from the server, but current
state-of-the-art websites are able to modify the page in response to URL
changes without a full refresh.

With Kweb's routing mechanism you get this automatically.

## A simple example

```kotlin
import kweb.Kweb
import kweb.dom.element.new
import kweb.dom.element.creation.tags.h1
import kweb.routing.route

fun main() {
    Kweb(port = 16097) {
        doc.body {
            route {
                path("/users/{userId}") { params ->
                    val userId = params.getValue("userId")
                    h1().text(userId.map { "User id: $it" })
                }
                path("/lists/{listId}") { params ->
                    val listId = params.getValue("listId")
                    h1().text(listId.map { "List id: $it" })
                }
            }
        }
    }
}
```

Now, if you visit <http://localhost:16097/users/997>, you will see:

<kbd>
User id: 997
</kbd>

The value of these parameters can then be retrieved from the *params*
map, but note that the values are wrapped in a `KVar<String>` object.
This means that you can use all of Kweb's [state
management](https://docs.kweb.io/en/latest/state.html) features to
render parts of the DOM using this value.

The key advantage here is that if the URL changes the page can be
updated without a full page refresh, but rather only changing the parts
of the DOM that need to change - this is much faster and more efficient.

## Handing 404s

You can override the default 404 Page Not Found message in the event
that none of the routes match, making it easy to integrate the 404 page
with the style of your overall website:

```kotlin
route {
    path("/users/{userId}") { params ->
        // ...
    }
    notFound {
      h1().text("Page not found!")
    }
}
```

## Modifying the URL

You can obtain *and modify* the URL of the current page using
[WebBrowser.url](https://github.com/kwebio/kweb-core/blob/master/src/main/kotlin/kweb/WebBrowser.kt#L98).

This returns a `KVar<String>` which contains the URL relative to the
origin - so for the page `http://foo/bar/z` the `url` would be `/bar/z`.

Here is a more realistic example:

```kotlin
import kweb.Kweb
import kweb.dom.element.creation.tags.a
import kweb.dom.element.new
import kweb.routing.route
import kweb.state.*

fun main() {
    Kweb(port = 16097) {
        doc.body {
            route {
                path("/") {
                    url.value = "/number/1"
                }
                path("/number/{num}") { params ->
                    val num = params.getValue("num").toInt()
                    a().text(num.map {"Number $it"}).on.click {
                        num.value++
                    }
                }
            }
        }
    }
}
```

If you visit `http://localhost:16097/` the URL will immediately update
to `http://localhost:16097/number/1` without a page refresh, and you'll
see a hyperlink with text `Number 1`. If you click on this link you'll
see that the number increments (both in the URL and in the link text),
also without a page refresh.

The line `num.value++` is worthy of additional attention as there is
more going on here than meets the eye. `num` is a `KVar<Int>`, and so it
can be incremented via its `value` property. This will cause the page
URL to update, which will in-turn cause the DOM to update to reflect the
new URL. All of this is handled for you automatically by Kweb.
