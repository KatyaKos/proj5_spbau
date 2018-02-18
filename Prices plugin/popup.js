function getCurrentTab(callback) {
    var queryInfo = {
        active: true,
        currentWindow: true
    };

    chrome.tabs.query(queryInfo, (tabs) => {
        var tab = tabs[0];
        callback(tab);
    });
};

function htmlToDom(html, callback) {
    var parser = new DOMParser();
    var dom = parser.parseFromString(html, "text/html");
    callback(dom);
}

function getWholeTag(node, callback) {
    var result = node.nodeName.toLowerCase();
    if (node.hasAttribute("class")) {
        result += ":" + node.getAttribute("class").replace(/\s*/g, '');
    }
    result += "/";
    callback(result);
}

function getTagnameIfDefined(myText, myPath, tags, callback) {
    for (var i = 0; i < tags.length; i++) {
        var dict = tags[i];
        if (dict["text"] == myText && dict["path"] == myPath) {
            callback(dict["name"]);
            return;
        }
    }
    callback("");
}

function walkTextNodes(node, path, tags, callback) {
    const childNodes = node.childNodes;
    var result = "";
    for (var i = 0; i < childNodes.length; i++) {
        if (childNodes[i].nodeType == 1 && childNodes[i].nodeName.localeCompare("SCRIPT") != 0) {
            getWholeTag(childNodes[i], (tag) => {
                walkTextNodes(childNodes[i], path + tag, tags, (text) => result += text);
            });
        } else if (childNodes[i].nodeType == 3 && childNodes[i].data.replace(/\s*/g, '') != '') {
            var data = childNodes[i].data.split(/\s+/);
            for (var j = 0; j < data.length; j++) {
                if (data[j].replace(/\s*/g, '') == '') continue;
                var tagname = "";
                getTagnameIfDefined(data[j], path, tags, (res) => tagname = res);
                result += tagname + "\t" + "node[0]=" + data[j] + "\tpath[0]=" + path + '\n';
            }
        }
    }
    callback(result);
};

function getText(callback) {
    getCurrentTab((tab) => {
        chrome.tabs.sendRequest(tab.id, { action: "getHTML" }, function (response) {
            chrome.storage.local.get('tags', function (items) {
                var tags = items.tags;
                if (typeof (tags) == 'undefined') {
                    tags = [];
                }
                htmlToDom(response.html, (dom) => {
                    dom.documentElement.normalize();
                    walkTextNodes(dom.documentElement, "", tags, (text) => callback(text));
                });
            });
        });
    });
}

function download(filename) {
    getText((text) => {
        text = "___BEGIN___\n" + text + "___END___\n"
        var element = document.createElement('a');
        var blob = new Blob([text], { type: "text/plain;charset=utf-8" });
        element.href = window.URL.createObjectURL(blob);
        element.download = filename + ".txt";
        element.click();
        removeTags(function () { });
    });
};

function saveTag(tagname) {
    getCurrentTab((tab) => {
        chrome.tabs.sendRequest(tab.id, { action: "getSelection" }, function (responses) {
            var dict = [];
            for (var i = 0; i < responses.length; i++) {
                var text = responses[i].text;
                var path = responses[i].path;
                dict.push({ "name": tagname, "text": text, "path": path });
            }
            chrome.storage.local.get("tags", function (items) {
                if (typeof (items.tags) != 'undefined') {
                    dict = dict.concat(items.tags);
                }
                chrome.storage.local.set({ "tags": dict }, function () {
                    alert('Tag ' + tagname + ' saved');
                });
            });
        });
    });
}

function showTags() {
    chrome.storage.local.get('tags', function (items) {
        var result = "";
        var dict = items.tags;
        if (typeof (dict) == 'undefined') {
            alert("NO TAGS SAVED!\n")
            return;
        }
        for (var i = 0; i < dict.length; i++) {
            result += "tag" + i + ": name = " + dict[i]["name"] + ", text = " + dict[i]["text"] + "\n";
        }
        alert(result);
    });
}

function removeTags(callback) {
    chrome.storage.local.remove("tags", callback);
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('save_html').addEventListener('click', () => {
        var filename = document.getElementById('input_filename').value;
        if (filename != '') {
            chrome.storage.local.set({ "filename": filename }, download(filename));
        } else {
            chrome.storage.local.get("filename", function (items) {
                var name = items.filename;
                if (typeof (name) == 'undefined') name = "";
                download(name);
            });
        }
    });
    document.getElementById('save_tag').addEventListener('click', () => {
        var tagname = document.getElementById('input_tagname').value;
        if (!tagname) {
            alert('Error: No value specified');
            return;
        }
        saveTag(tagname);
    });
    document.getElementById('show').addEventListener('click', () => {
        showTags();
    });
    document.getElementById('delete').addEventListener('click', () => {
        removeTags(function () {
            alert('Removed all tags');
        });
    });
});