phantom.onError = function (msg, trace) {
    var msgStack = ['PHANTOM ERROR: ' + msg];
    if (trace && trace.length) {
        msgStack.push('TRACE:');
        trace.forEach(function (t) {
            msgStack.push(' -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t.function ? ' (in function ' + t.function + ')' : ''));
        });
    }
    console.error(msgStack.join('\n'));
    phantom.exit(1);
};
var page = require('webpage').create();
page.onConsoleMessage = function (msg, lineNum, sourceId) {
    console.info('CONSOLE: ' + msg);
};
page.includeJs("http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.selectorExpression", function () {
    page.open('http://127.0.0.1:7324', function (status) {
        if (status === "success") {
            window.setTimeout(function () {
                phantom.exit();
            }, 15000);
            //waitFor("$('.testclass').is(':visible')", "console.info('<h1> rendered');");
        }
    })
});

