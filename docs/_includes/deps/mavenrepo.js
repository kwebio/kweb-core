// This code will automiatcally pull the latest version number from the Github API and insert
// it in the instructions below, through the magic of ugly JavaScript.

var xmlhttp = new XMLHttpRequest();

xmlhttp.onreadystatechange = function() {
    if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
        var myArr = JSON.parse(xmlhttp.responseText);
        populateRelease(myArr);
    }
}
xmlhttp.open("GET", "https://api.github.com/repos/sanity/quickml/releases", true);
xmlhttp.send();

function populateRelease(arr) {
    var release = arr[0].tag_name;
    $("span.current_release").text(release);
    $("a.jdoclink").attr("href", "https://jitpack.io/com/github/sanity/kweb/"+release+"/javadoc/index.html");
}