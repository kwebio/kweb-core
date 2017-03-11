---
layout: page
title: "Your first kwebsite"
category: use
order: 2
date: 2017-03-08 10:34:51
---

Add a file like the following to your project:

```kotlin
fun main(args: Array<String>) {
    Kweb(port = 7823) {
        doc.body.h1().setText("Hello World!")
    }
}
```

Run the file and visit [http://localhost:7823/](http://localhost:7823/) in your web browser, ta-da!

### A more ambitious example

Let's edit the file to do something more interesting:

```kotlin
fun main(args: Array<String>) {
    Kweb(port = 7823) {
        var counter = 0
        val h1 = doc.body.h1()
        h1.setText("Hello World!")
        h1.on.click {
            h1.setText(counter.toString())
            counter++
        }
    }
}
```

Here we create a header-1 element as before, and set its text to "Hello World!" as before also.  In this example
we've assigned the header element to a variable called `h1`, and then we use the variable to set the text.

Next we create a click event listener on the header element, once clicked we set the text of the element to the value
of the variable `counter`, and increase the value of `counter`.

Kill Kweb if it is still running, and run this new version.  Try clicking on the text.

**Troubleshooting**: If you get an error like `Exception in thread "main" java.net.BindException: Address already in use` it means 
that the previous version is still running and therefore the new version is unable to listen on port 7823, make
sure you've killed it.

-----------
**Next: [Live coding]({{ site.baseurl }}{% post_url 2017-03-09-live-coding %}) >>>>**
