function toWSUrl(s) {
    let l = window.location;
    return (l.protocol === "https:" ? "wss://" : "ws://") + l.host + "/" + s;
}

let kwebClientId = "--CLIENT-ID-PLACEHOLDER--";
let websocketEstablished = false;
let preWSMsgQueue = [];
let socket;

let cachedFunctions = new Map();

function handleInboundMessage(msg) {
    console.debug("")
    const yourId = msg["yourId"];
    const debugToken = msg["debugToken"];
    if (kwebClientId != yourId) {
        console.error(
            "Received message from incorrect clientId, was " +
            yourId +
            ", should be " +
            kwebClientId
        );
    }

    const cacheId = msg["jsId"];
    var params;
    if (msg["parameters"] !== undefined) {
        params = msg["parameters"];
    }
    let args = msg["arguments"];
	var func;
	if (cacheId === undefined) {
	    func = new Function(params, msg["js"])
	    func.apply(this, args);
	    return;
	}
    if (cachedFunctions.get(cacheId) === undefined) {
		let js = msg["js"];
		func = new Function(params, js);
		cachedFunctions.set(cacheId, func);
    } else {
		func = cachedFunctions.get(cacheId);
	}

	if (msg["callbackId"] === undefined) {
		//execute without callback
		try {
			func.apply(this, args);
			console.debug("Executed JavaScript", func.toString());
		} catch (err) {
			if (debugToken != undefined) {
				console.error("Error evaluating [" + func.toString() + "]" + err);
				var error = {
					debugToken: debugToken,
					error: {name: err.name, message: err.message}
				};
				var message = {id: kwebClientId, error: error};
				sendMessage(JSON.stringify(message));
			}
			else {
				throw err;
			}
		}
	} else {
		//execute with callback
		try {
			const data = func.apply(this, args);
			console.debug("Evaluated [" + func.toString() + "]", data);
			const callback = {callbackId: msg["callbackId"], data: data};
			const message = {id: kwebClientId, callback: callback};
			sendMessage(JSON.stringify(message));
		} catch (err) {
			if (debugToken != undefined) {
				console.error("Error evaluating `" + func.toString() + "`: " + err);
				const error = {
					debugToken: debugToken,
					error: {name: err.name, message: err.message}
				};
				const message = {id: kwebClientId, error: error};
				sendMessage(JSON.stringify(message));
			} else {
				throw err;
			}
		}
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
            sendMessage(JSON.stringify({id: kwebClientId, hello: true}));
            while (preWSMsgQueue.length > 0) {
                sendMessage(preWSMsgQueue.shift());
            }
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

            console.error("WebSocket was closed", explanation, evt);
            websocketEstablished = false;
            if (evt.wasClean) {
                console.warn("Attempting reconnect...")
                connectWs()
            } else {
                console.warn("Forcing page reload");
                location.reload(true);
            }
        };
        socket.onerror = function (evt) {
            console.error("WebSocket error", evt);
            websocketEstablished = false;
            console.warn("Forcing page reload");
            location.reload(true);
        };
    }
}

function sendMessage(msg) {
    if (websocketEstablished) {
        console.debug("Sending WebSocket message", msg);
        socket.send(msg);
    } else {
        console.debug(
            "Queueing WebSocket message as connection isn't established",
            msg
        );
        preWSMsgQueue.push(msg);
    }
}

function callbackWs(callbackId, data) {
    var msg = JSON.stringify({
        id: kwebClientId,
        callback: {callbackId: callbackId, data: JSON.stringify(data)}
    });
    sendMessage(msg);
}

/*
 * Utility functions
 */
function hasClass(el, className) {
    if (el.classList) return el.classList.contains(className);
    else
        return !!el.className.render(new RegExp("(\\s|^)" + className + "(\\s|$)"));
}

function addClass(el, className) {
    if (el.classList) el.classList.add(className);
    else if (!hasClass(el, className)) el.className += " " + className;
}

function removeClass(el, className) {
    if (el.classList) el.classList.remove(className);
    else if (hasClass(el, className)) {
        var reg = new RegExp("(\\s|^)" + className + "(\\s|$)");
        el.className = el.className.replace(reg, " ");
    }
}

class DiffPatchData {
    constructor(prefixEndIndex, postfixOffset, diffString) {
        this.prefixEndIndex = prefixEndIndex;
        this.postfixOffset = postfixOffset;
        this.diffString = diffString;
    }
}

//Used by setValue() in prelude.kt to compare 2 strings and return the difference between the 2.
function get_diff_changes(htmlInputElement) {
    let newString = htmlInputElement.value;//reads the new string value from data-attribute data-vale
    let oldString = htmlInputElement.dataset.previousInput;//reads the oldString from the data-attribute data-previous-input

    savePreviousInput(newString, htmlInputElement)//put the newString into the data attribute so it can be used as the oldString the next time this method is run

    if (oldString === undefined) {//the first time this is run previous-input should be undefined so we just return the new string
        return new DiffPatchData(0, 0, newString);
    }
    let commonPrefixEnd = 0;

    let oldStringLastIndex = oldString.length - 1;
    let newStringLastIndex = newString.length - 1;

    let commonPostfixOffset = -1; //if the postFix value is set to -1, it means there is no match on the end of the string

    let shorterStringLength = (oldString.length > newString.length) ? newString.length : oldString.length;

    for (let i = 0; i < shorterStringLength; i++) {
        if (oldString.charAt(i) == newString.charAt(i)) {
            commonPrefixEnd = i+1;
        } else break;
    }
    for(let offset = 0; offset < shorterStringLength - commonPrefixEnd; offset++) {
        if (oldString.charAt(oldStringLastIndex - offset) == newString.charAt(newStringLastIndex - offset)) {
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
