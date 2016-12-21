/**
 * Created by tianyuliang on 2016/12/2.
 */

/***
 * 获取全局API接口
 * @param key 接口标识
 * @returns 具体某个API地址
 */
function globalApi(key) {
    var globalConfig = {
        "enblabRootPath": "1",   // 0:启用RootPat, 1:关闭RootPath（例如 http://127.0.0.1:8090/rootPath/login.html）

        "api.domain": "",
        "api.rootPath": "",
        "api.login": "/app/login",
        "api.logout": "/app/logout",
        "api.verify.sign": "/sso/token.do",
        "api.user.query": "/app/loginUserInfo"
    };
    var api = globalConfig[key] || "/unknown/"
    return api + "?r=" + Math.random();
}

function globalError(code) {
    var errorlConfig = {
        "100001": "appKey错误",
        "100002": "token为空",
        "100003": "token错误",
        "100004": "token解密错误",
        "100005": "appKey为空",
        "100006": "appKey和url的domain不匹配",
        "100007": "请求参数不正确",
        "100008": "用户名或密码错误",
        "100009": "token失效",
        "500000": "系统错误"
    };
    return errorlConfig[code] || "服务内部异常";
}