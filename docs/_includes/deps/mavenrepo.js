// This code will automiatcally pull the latest version number from the Github API and insert
// it in the instructions below, through the magic of ugly JavaScript.

var xmlhttp = new XMLHttpRequest();

xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        var myArr = JSON.parse(xmlhttp.responseText);
        populateRelease(myArr);
    }
}
xmlhttp.open("GET", "https://api.github.com/repos/sanity/kweb/releases", true);
xmlhttp.send();

function populateRelease(arr) {
    var release = arr[0].tag_name;
    $("pre.highlight").html(function(index, oldhtml) {
        var newhtml = oldhtml.replace('MAVEN_VERSION_PLACEHOLDER', release);
        return newhtml;
    })
    $("a.jdoclink").attr("href", "https://jitpack.io/com/github/sanity/kweb/"+release+"/javadoc/index.html");
}