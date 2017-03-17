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