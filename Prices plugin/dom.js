chrome.extension.onRequest.addListener(function (request, sender, sendResponse) {
    if (request.action == "getHTML")
        sendResponse({ html: document.documentElement.outerHTML });
    else if (request.action == "getSelection")
        getSelected((res) => sendResponse(res));
    else
        sendResponse({});
});

function getSelected(callback) {
    var response = [];
    if (typeof (window.getSelection()) != "undefined") {
        var sel = window.getSelection();
        if (sel.rangeCount) {
            for (var i = 0; i < sel.rangeCount; i++) {
                var html = "";
                var container = document.createElement("div");
                getSelectedArray(sel.getRangeAt(i), (arr) => response = response.concat(arr));
            }
        }
    }
    callback(response);
}

function getSelectedArray(range, callback) {
    var nodes = [];
    getRangeSelectedNodes(range, (arr) => nodes = arr);
    var result = [];
    for (var i = 0; i < nodes.length; i++) {
        nodeToDict(nodes[i], (arr) => result = result.concat(arr))
    }
    var to = "hi";
    callback(result);
}

function getRangeSelectedNodes(range, callback) {
    var node = range.startContainer;
    var endNode = range.endContainer;
    if (node == endNode) {
        callback([node]);
        return;
    }
    var rangeNodes = [];
    while (node && node != endNode) {
        nextNode(node, (next) => {
            node = next;
            rangeNodes.push(next);
        })
    }
    node = range.startContainer;
    while (node && node != range.commonAncestorContainer) {
        rangeNodes.unshift(node);
        node = node.parentNode;
    }
    callback(rangeNodes);
}

function nodeToDict(node, callback) {
    var result = [];
    var path = "";
    if (node.nodeType == 3 && node.data.replace(/\s*/g, '') != '') {
        getPath(node, "", (txt) => path = txt);
        var data = node.data.split(/\s+/);
        for (var j = 0; j < data.length; j++) {
            if (data[j].replace(/\s*/g, '') == '') continue;
            result.push({ "text": data[j], "path": path });
        }
    }
    callback(result);
}

function getPath(node, path, callback) {
    const parent = node.parentElement;
    if (parent.parentElement != null) {
        if (parent.nodeName.localeCompare("SCRIPT") != 0) {
            getWholeTag(parent, (tag) => {
                getPath(parent, tag + path, (text) => callback(text));
            });
        } else {
            getPath(parent, path, (text) => callback(text));
        }
    } else {
        callback(path);
    }
};

function nextNode(node, callback) {
    if (node.hasChildNodes()) {
        callback(node.firstChild);
    } else {
        while (node && !node.nextSibling) {
            node = node.parentNode;
        }
        if (!node) {
            callback(null);
            return;
        }
        callback(node.nextSibling);
    }
}

function getWholeTag(node, callback) {
    var result = node.nodeName.toLowerCase();
    if (node.hasAttribute("class")) {
        result += ":" + node.getAttribute("class").replace(/\s*/g, '');
    }
    result += "/";
    callback(result);
}