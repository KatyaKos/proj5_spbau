chrome.extension.onRequest.addListener(function (request, sender, sendResponse) {
    if (request.action == "getHTML")
        sendResponse({ html: document.documentElement.outerHTML });
    else
        sendResponse({}); // Send nothing..
});