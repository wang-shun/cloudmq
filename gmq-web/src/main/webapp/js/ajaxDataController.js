function ajaxDataController(url, params, callback, async, method) {


    if (!method) {
        throw 'method 参数未设置'
    }

    params = params || {}
    async = async || true

    $.ajax({
        async: async,
        url: url,
        dataType: 'json',
        data: params,
        type: method,
        contentType: 'application/json;charset=UTF-8',
        complete: function (xhr) {

            try {
                var result = JSON.parse(xhr.status),
                    DOM_ID = 'promptDialog',
                    code = parseInt(result),
//                        message = result.status.message,
                    prompt = {
                        'id': DOM_ID,
                        'title': '操作结果',
                        'message': errorDic(code)
                    }
            } catch (err) {
                message = err.message
            }

            XUI.gotoPageHref('user/list.do');
//                if (code == 200) {
//                    window.location.href="$root/user/list.do"
//                }

//                callback(result.data)
        }
    });

// TODO: 需要添加请求未成功的验证

//    return {
//        'insert': function(url, params, callback, async) {
//
//            dataHandle(url, JSON.stringify(params), callback, async, 'post')
//        },
//        'update': function(url, params, callback, async) {
//
//            dataHandle(url, JSON.stringify(params), callback, async, 'put')
//        },
//        'delete': function(url, params, callback, async) {
//
//            dataHandle(url, JSON.stringify(params), callback, async, 'delete')
//        },
//        'select': function(url, params, callback, async) {
//
//            dataHandle(url, params, callback, async, 'get')
//        }
//    };
}