# Module kweb-core

Kweb is a self-contained Kotlin library that can be added easily to new or 
existing projects. When Kweb receives a HTTP request it responds with the 
initial HTML page, and some JavaScript that connects back to the web server
via a WebSocket. The page then waits and listens for instructions from the 
server, while notifying the server of relevant browser events.

# Package kweb

The top-level Kweb classes along with much of the Kweb DSL reside in this
package.