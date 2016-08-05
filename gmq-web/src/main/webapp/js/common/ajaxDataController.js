function ajaxDataController(requestUrl, params, gotoUrl, callback, async, method) {
    if (!method) {
        throw 'method 参数未设置.'
    }

    params = params || {};
    async = async || true;

    $.ajax({
        async: async,
        url: requestUrl,
        timeout: 3000,
        dataType: 'json',
        data: params,
        type: method,
        contentType: 'application/json;charset=utf-8',
        success: function (data) {
            if (gotoUrl) {
                XUI.gotoPageHref(gotoUrl);
            } else if (callback && isFunction(callback)) {
                callback(data);
            }
        },
        error: function (xhr) {
            try {
                console.log("httpStatus=" + xhr.status + ",descition=" + xhr.responseText);
                if (xhr.status == 'timeout') {
                    $(this).abort();
                }
            } catch (e) {
            }
        }
    });
}

function isFunction(funName) {
    return Object.prototype.toString.call(funName) === '[object Function]';
}