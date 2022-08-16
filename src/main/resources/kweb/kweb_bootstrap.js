function toWSUrl(s) {
    let l = window.location;
    return (l.protocol === "https:" ? "wss://" : "ws://") + l.host + "/" + s;
}

let kwebClientId = "--CLIENT-ID-PLACEHOLDER--";
let websocketEstablished = false;
let preWSMsgQueue = [];
let socket;
let bannerId = "CkU6vMWzW0Hbp"  // Random id to avoid conflicts
let reconnectTimeout = 2000
//let reconnectCount = 0

// FUNCTION CACHE PLACEHOLDER //

function handleInboundMessage(msg) {
    console.debug("")
    const yourId = msg["yourId"];
    const funcCalls = msg["functionCalls"]
    for (let i = 0; i < funcCalls.length; i++) {
        const funcCall = funcCalls[i];
        const debugToken = funcCall["debugToken"];
        if (kwebClientId != yourId) {
            console.error(
                "Received message from incorrect clientId, was " +
                yourId +
                ", should be " +
                kwebClientId
            );
        }

        let func;
        let js = funcCall["js"];
        const args = funcCall["arguments"];
        const params = funcCall["parameters"];
        const cacheId = funcCall["jsId"];
        const callbackId = funcCall["callbackId"];

        if (cacheId !== undefined) {
            if (cachedFunctions[cacheId] !== undefined) {
                func = cachedFunctions[cacheId];
            } else {
                if (params !== undefined) {
                    func = new Function(params, js);
                } else {
                    func = new Function(js);
                }
                cachedFunctions[cacheId] = func;
            }
        } else {
            //This is a special case that doesn't bother reading the cache, or trying to cache the function.
            //It will just run the javascript supplied to it. This special case is currently only used by Kweb.refreshPages()
            if (params !== undefined) {
                func = new Function(params, js);
            } else {
                func = new Function(js);
            }
        }

        if (callbackId !== undefined) {
            try {
                const data = func.apply(this, args)
                console.debug("Evaluated [ " + func.toString() + "]", data);
                const callback = {callbackId: callbackId, data: data};
                const message = {id: kwebClientId, callback: callback};
                sendMessage(JSON.stringify(message));
            } catch (err) {
                debugErr(debugToken, err, "Error Evaluating `" + func.toString() + "`: " + err);
            }
        } else {
            try {
                func.apply(this, args);
                console.debug("Executed Javascript", func.toString());
            } catch (err) {
                debugErr(debugToken, err, "Error Executing `" + func.toString() + "`: " + err);
            }
        }
    }
}

function debugErr(debugToken, err, errMsg) {
    if (debugToken !== undefined) {
        console.error(errMsg);
        const error = {
            debugToken: debugToken,
            error: {name: err.name, message: err.message}
        };
        const message = {id: kwebClientId, error: err};
        sendMessage(JSON.stringify(message));
    } else {
        console.error(errMsg)
    }
}

function connectWs() {
    var wsURL = toWSUrl("ws");
    console.debug("Establishing websocket connection", wsURL);
    socket = new WebSocket(wsURL);
    if (window.WebSocket === undefined) {
        document.body.innerHTML =
            "<h1>Unfortunately this website requires a browser that supports websockets (all modern browsers do)</h1>";
        console.error("Browser doesn't support window.WebSocket");
    } else {
        socket.onopen = function () {
            console.debug("socket.onopen event received");
            websocketEstablished = true;
            console.debug("Websocket established", wsURL);
            removeElementByIdIfExists(bannerId);
            sendMessage(JSON.stringify({id: kwebClientId, hello: true}));
            while (preWSMsgQueue.length > 0) {
                sendMessage(preWSMsgQueue.shift());
            }
            reconnectTimeout = 2000
        };
        socket.onmessage = function (event) {
            var msg = JSON.parse(event.data);
            console.debug("Message received from socket: ", event.data);
            handleInboundMessage(msg);
        };

        socket.onclose = function (evt) {
            console.debug("Socket closed");
            var explanation = "";
            if (evt.reason && evt.reason.length > 0) {
                explanation = "reason: " + evt.reason;
            } else {
                explanation = "without a reason specified";
            }

            if(evt.code == 1007){ //Server did restart or load balancer shifted session to other backend
                location.reload(true);
            }

            console.error("WebSocket was closed", explanation, evt);
            websocketEstablished = false;
            setTimeout(() => {
                reconnectLoopWs()
            }, reconnectTimeout)
        };
        socket.onerror = function (evt) {
            console.error("WebSocket error", evt);
            websocketEstablished = false;
        };
    }
}

function reconnectLoopWs() {
    reconnectTimeout *= 2

    showReconnectToast();

    setTimeout(function() {
        if (websocketEstablished == false) {
            if (reconnectTimeout < 600_000) {
                console.log("Attempting to reconnect")
                connectWs();
            } else {
                console.warn("Forcing page reload because the server is unresponsive for too long");
                location.reload(true);
            }
        }
    }, reconnectTimeout);
}

function showReconnectToast() {
    Toastify({
        text: `-- TOAST MESSAGE PLACEHOLDER --`,
        duration: reconnectTimeout,
        close: false,
        gravity: "bottom",
        position: "center",
        stopOnFocus: false,
        style: {
            color: "#ff0033",
            background: "white"
        }
    }).showToast();
}

function sendMessage(msg) {
    if (websocketEstablished) {
        console.debug("Sending WebSocket message", msg);
        socket.send(msg);
    } else {
        preWSMsgQueue.push(msg);
    }
}

function callbackWs(callbackId, data) {
    const msg = JSON.stringify({
        id: kwebClientId,
        callback: {callbackId: callbackId, data: data}
    });
    sendMessage(msg);
}

function sendKeepalive() {
    const msg = JSON.stringify({
        id: kwebClientId,
        keepalive: true
    });
    sendMessage(msg);
}

setInterval(sendKeepalive, 60*1000);

/*
 * Utility functions
 */
function hasClass(el, className) {
    if (el.classList) return el.classList.contains(className);
    else
        return !!el.className.render(new RegExp("(\\s|^)" + className + "(\\s|$)"));
}

class DiffPatchData {
    constructor(prefixEndIndex, postfixOffset, diffString) {
        this.prefixEndIndex = prefixEndIndex;
        this.postfixOffset = postfixOffset;
        this.diffString = diffString;
    }
}

//Used by setValue() in prelude.kt to return the difference between 2 strings
function get_diff_changes(htmlInputElement) {
    let newString = htmlInputElement.value;//reads the new string value from data-attribute data-value
    let oldString = htmlInputElement.dataset.previousInput;//reads the oldString from the data-attribute data-previous-input

    savePreviousInput(newString, htmlInputElement)//put the newString into the data attribute so it can be used as the oldString the next time this method is run

    if (oldString == undefined) {//the first time this is run previous-input should be undefined so we just return the new string
        return new DiffPatchData(0, 0, newString);
    }
    let commonPrefixEnd = 0;

    let oldStringLastIndex = oldString.length - 1;
    let newStringLastIndex = newString.length - 1;

    let commonPostfixOffset = -1; //if the postFix value is set to -1, it means there is no match on the end of the string

    let shorterStringLength = (oldString.length > newString.length) ? newString.length : oldString.length;

    for (let i = 0; i < shorterStringLength; i++) {
        if (oldString.charAt(i) === newString.charAt(i)) {
            commonPrefixEnd = i+1;
        } else break;
    }
    for(let offset = 0; offset < shorterStringLength - commonPrefixEnd; offset++) {
        if (oldString.charAt(oldStringLastIndex - offset) === newString.charAt(newStringLastIndex - offset)) {
            commonPostfixOffset = offset+1;
        } else break;
    }
    return new DiffPatchData(commonPrefixEnd, commonPostfixOffset, newString.substring(commonPrefixEnd, newString.length - commonPostfixOffset));
}

//Used to save the previous value of an input field to a data-attribute
function savePreviousInput(previousInputString, htmlInputElement) {
    htmlInputElement.dataset.previousInput = previousInputString
}

function removeElementByIdIfExists(id) {
    var e = document.getElementById(id);
    if (e) {
        e.parentNode.removeChild(e);
    }
}

var docCookies = {
    getItem: function (sKey) {
        if (!sKey || !this.hasItem(sKey)) {
            return "__COOKIE_NOT_FOUND_TOKEN__";
        }
        return unescape(
            document.cookie.replace(
                new RegExp(
                    "(?:^|.*;\\s*)" +
                    escape(sKey).replace(/[\-\.\+\*]/g, "\\$&") +
                    "\\s*\\=\\s*((?:[^;](?!;))*[^;]?).*"
                ),
                "$1"
            )
        );
    },

    setItem: function (sKey, sValue, vEnd, sPath, sDomain, bSecure) {
        if (!sKey || /^(?:expires|max\-age|path|domain|secure)$/.test(sKey)) {
            return;
        }
        var sExpires = "";
        if (vEnd) {
            switch (typeof vEnd) {
                case "number":
                    sExpires = "; max-age=" + vEnd;
                    break;
                case "string":
                    sExpires = "; expires=" + vEnd;
                    break;
                case "object":
                    if (vEnd.hasOwnProperty("toGMTString")) {
                        sExpires = "; expires=" + vEnd.toGMTString();
                    }
                    break;
            }
        }
        document.cookie =
            escape(sKey) +
            "=" +
            escape(sValue) +
            sExpires +
            (sDomain ? "; domain=" + sDomain : "") +
            (sPath ? "; path=" + sPath : "") +
            (bSecure ? "; secure" : "");
    },
    removeItem: function (sKey) {
        if (!sKey || !this.hasItem(sKey)) {
            return;
        }
        var oExpDate = new Date();
        oExpDate.setDate(oExpDate.getDate() - 1);
        document.cookie =
            encodeURIComponent(sKey) + "=; expires=" + oExpDate.toGMTString() + "; path=/";
    },
    hasItem: function (sKey) {
        return new RegExp(
            "(?:^|;\\s*)" + encodeURIComponent(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\="
        ).test(document.cookie);
    }
};

function buildPage() {
    <!-- BUILD PAGE PAYLOAD PLACEHOLDER -->
    connectWs();
}
