function getCurrentTabUrl(callback) {
    getCurrentTab((tab) => {
        var url = tab.url;
        console.assert(typeof url == 'string', 'tab.url should be a string');
        callback(url);
    });
};

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

function walkTextNodes(node, path, callback) {
    const childNodes = node.childNodes;
    var result = "";
    for (var i = 0; i < childNodes.length; i++) {
        if (childNodes[i].nodeType == 1 && childNodes[i].nodeName.localeCompare("SCRIPT") != 0) {
            getWholeTag(childNodes[i], (tag) => {
                walkTextNodes(childNodes[i], path + tag, (text) => result += text);
            });
        } else if (childNodes[i].nodeType == 3 && childNodes[i].data.replace(/\s*/g, '') != '') {
            var data = childNodes[i].data.split(/\s+/);
            for (var j = 0; j < data.length; j++) {
                if (data[j].replace(/\s*/g, '') == '') continue;
                result += "-1\t" + "node[0]=" + data[j] + "\tpath[0]=" + path + '\n';
            }
        }
    }
    callback(result);
};

function getText(callback) {
    getCurrentTab((tab) => {
        chrome.tabs.sendRequest(tab.id, { action: "getHTML" }, function (response) {
            htmlToDom(response.html, (dom) => {
                dom.documentElement.normalize();
                walkTextNodes(dom.documentElement, "", (text) => callback(text));
            });
        });
    });
}

function download(filename, text) {
    getText((text) => {
        text = "__BOS__\n" + text + "__EOS__\n"
        var element = document.createElement('a');
        var blob = new Blob([text], { type: "text/plain;charset=utf-8" });
        element.href = window.URL.createObjectURL(blob);
        element.download = filename + ".txt";
        element.click();
    });
};

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('button').addEventListener('click', () => {
        getCurrentTabUrl((url) => {
            var filename = document.getElementById('input').value;
            if (filename == '') filename = "ali";
            download(filename, "HELLO");
        });
    });
});