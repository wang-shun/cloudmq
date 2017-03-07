/**
 * Created by tianyuliang on 2016/10/26.
 */


/**
 * 退出接口
 */
function handler_logout() {
    ajaxDataController(globalApi("api.logout"), {}, null, function (data) {
        if (data && data.code === 0) {
            window.location.href = "/view/web.proxy.html";
        } else {
            $("#logout").attr("data-content", "退出失败.");
            $("#logout").popover("toggle");
        }
    }, true, "get");
}

/**
 * 页面右上角，展示用户名（除了登陆页之外，其他所有页面均需要调用）
 */
function handler_query_user() {
    ajaxDataController(globalApi("api.user.query"), {}, null, function (data) {
        if (data && data.code === 0) {
            if(data.data && data.data.userName){
                $("#user-name").html(data.data.userName);
                $("#logout").attr("user-name", data.data.userName);
                $("#logout").attr("user-id", data.data.ssoUserId);
            } else {
                window.location.href = "web.proxy.html";
            }
        } else {
            read_logined_user();
        }
    }, true, "get");
}


/**
 * 从cookies中读取已登陆的用户名
 */
function read_logined_user() {
    try {
        $("#user-name").html($.cookie("web-user") || "");
        $("#logout").attr("user-name", $.cookie("web-user") || "");
    } catch (e) {
    }
}

/**
 * 后台管理登陆页-读取cookies
 */
function read_cookie_user() {
    try {
        $("#txt_user_name").val($.cookie("web-user") || "");
    } catch (e) {
    }
}

/**
 * 后台管理登陆页-设置cookies
 */
function set_cookie_login(cookieValue) {
    try {
        $.cookie("web-user", cookieValue, {path: "/"});
    } catch (e) {
    }
}

/**
 * SSO登陆
 */
function sso_login() {
    if (!validate_login_param()) {
        return;
    }
    handler_appKey_redirectUrl();
    handler_sso_login();
}

/**
 * 如果submit提交，登录成功，服务端302跳转到app应用的页面，会遭遇跨域问题、cookies无法写入问题 <br/>
 * 因此改用ajax提交参数，登陆成功后由页面自行跳转
 */
function handler_sso_login() {
    var param = {
        "userName": $.trim($("#txt_user_name").val()),
        "password": $.trim($("#txt_password").val()),
        "redirectUrl": $.trim($("#redirectUrl").val()),
        "appKey": $.trim($("#appKey").val())
    };
    ajaxDataController(globalApi("ssoApi.login"), JSON.stringify(param), null, function (data) {
        if (data && data.code === 0) {
            if (data.data && data.data.redirectUrl && data.data.redirectUrl.length > 0) {
                gotoHref(data.data.redirectUrl);
            } else {
                handler_sso_userinfo(data.data);
                $("#divLoginForm").addClass("sso-div-hidden");
                $("#divAppForm").removeClass("sso-div-hidden");
            }
        } else {
            var warning_message = "用户名或密码错误.";
            if (data && data.msg) {
                warning_message = data.msg;
            }
            errorMsg(warning_message);
        }
    }, true, "post");
}

function handler_sso_logout() {
    ajaxDataController(globalApi("ssoApi.login.signout"), null, null, function (data) {
        // 调用成功，服务端302重定向到login.html页
    }, true, "post");
}

function validate_login_param() {
    var userName = $.trim($("#txt_user_name").val());
    var password = $.trim($("#txt_password").val());
    if (userName.length == 0) {
        $("#txt_user_name").focus();
        tipsLoginMsg("请输入用户名", "#txt_user_name");
        return false;
    }
    if (password.length == 0) {
        $("#txt_password").focus();
        tipsLoginMsg("请输入密码", "#txt_password");
        return false;
    }
    return true;
}


/***
 * 兼任域名直接访问的场景，取消redirectUrl和appKey参数空值校验  2016/11/16 Add by tianyuliang
 */
function handler_appKey_redirectUrl() {
    var redirectUrl = getUrlParameter("redirectUrl");
    $("#redirectUrl").val(redirectUrl || "");
    var appKey = getUrlParameter("appKey" || "");
    $("#appKey").val(appKey);
    myConsoleLog("appKey=" + $("#appKey").val() + ",redirectUrl=" + $("#redirectUrl").val());
}
/**
 * 查询SSO登陆用户的信息
 */
function query_sso_userinfo() {
    ajaxDataController(globalApi("ssoApi.user.query"), null, null, function (data) {
        if (data && data.code === 0 && data.data) {
            handler_sso_userinfo(data.data);
            $("#divLoginForm").addClass("sso-div-hidden");
            $("#divAppForm").removeClass("sso-div-hidden");
        } else {
            $("#divLoginForm").removeClass("sso-div-hidden");
        }
    }, true, "get");
}

/**
 * 加载登陆页支持SSO的应用列表
 */
function load_sso_app_list() {
    ajaxDataController(globalApi("ssoApi.app.all"), {}, null, function (data) {
        if (data && data.code === 0 && data.data.length > 0) {
            handler_app_div(data.data);
        } else {
            errorMsg("获取应用列表异常. msg=" + data.msg);
        }
    }, true, "get");
}

/**
 * 登陆成功，设置用户名、上次登陆，职位等信息
 */
function handler_sso_userinfo(bodyData) {
    $("#user_post").html(bodyData.post || "");
    $("#last_login_time").html(bodyData.lastLoginDate);
    $("#user_name").html(bodyData.userName);
}

function handler_app_div(bodyData) {
    var divHtml = '';
    var isNewLine = false;
    var count = 0;
    $.each(bodyData, function (index, el) {
        count += 1;
        isNewLine = count % 6 == 0 ? true : false;
        divHtml += build_div_appInfo(bodyData[index].appUrl, bodyData[index].appName, isNewLine);
    });
    $("#divShowAppLink").empty();
    $("#divShowAppLink").append(divHtml);
}

/**
 * 构建SSO应用的按钮样式
 */
function build_div_appInfo(linkUrl, value, isNewLine) {
    var divAppHtml = ''
        + '<div class="col-md-2 text-left">'
        + '<a href="' + linkUrl + '" class="btn btn-sm btn-success btn-block text-center" target="_blank">' + value + '</a>'
        + '</div>';
    if (isNewLine === true) {
        divAppHtml += '<br/><br/><br/>';
    }
    return divAppHtml;
}

function validate_app_param() {
    var appName = $.trim($("#txt_app_name").val());
    var appLink = $.trim($("#txt_app_link").val());
    var appDomain = $.trim($("#txt_app_domain").val());
    //var expiredApi = $.trim($("#txt_app_token_expired").val());
    var ipList = $.trim($("#txt_app_ip_list").val());
    if (appName.length == 0) {
        $("#txt_app_name").focus();
        tipsInputParamMsg("必填项", "#txt_app_name");
        return false;
    }

    if (appLink.length == 0) {
        $("#txt_app_link").focus();
        tipsInputParamMsg("必填项", "#txt_app_link");
        return false;
    }
    if (appLink.substr(0, 4) !== "http" && appLink.substr(0, 5) !== "https") {
        $("#txt_app_link").focus();
        tipsInputParamMsg("主页地址带有Http前缀", "#txt_app_link");
        return false;
    }

    if (appDomain.length == 0) {
        $("#txt_app_domain").focus();
        tipsInputParamMsg("必填项", "#txt_app_domain");
        return false;
    }

    /*if (expiredApi.length == 0) {
        $("#txt_app_token_expired").focus();
        tipsInputParamMsg("必填项", "#txt_app_token_expired");
        return false;
    }*/


    /*if (expiredApi.substr(0, 4) === "http" || expiredApi.substr(0, 5) === "https") {
        $("#txt_app_token_expired").focus();
        tipsInputParamMsg("请配置完整的http接口地址", "#txt_app_token_expired");
        return false;
    }*/

    if (ipList.length == 0) {
        $("#txt_app_ip_list").focus();
        tipsInputParamMsg("必填项", "#txt_app_ip_list");
        return false;
    }
    // 匹配IP
    if (ipList.length > 0) {
        var ip = ipList.replace(/\s/g, ",");
        var ips = ip.split(",");
        if (ips.length == 0) {
            tipsInputParamMsg("必填项", "#txt_app_ip_list");
            return false;
        }
        var flag = true, rows = 0;
        $.each(ips, function (i, el) {
            var tmp = match_ip(el);
            myConsoleLog("el=" + el + ", tmp=" + tmp)
            if (match_ip(el) === false) {
                flag = false;
                rows = i + 1;
                return false; // ==> break
            }
        });
        if (!flag) {
            tipsInputParamMsg("第" + rows + "行IP地址无效", "#txt_app_ip_list");
            return false;
        }
    }
    return true;
}

/**
 * 匹配单个IP
 * @param ipValue
 * @returns {true:有效IP,  false:无效IP}
 */
function match_ip(ipValue) {
    var regex_ip = new RegExp("^(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})(\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[0-9]{1,2})){3}$");
    //匹配 10.128.31.103
    var result = regex_ip.test(ipValue);
    if (!result) {
        var tmp = ipValue.split("/");
        if (tmp.length != 2) {
            return false;
        }
        //匹配 10.128.31.103/25
        if (!regex_ip.test(tmp[0]) || isNaN(Number(tmp[1])) || Number(tmp[1]) <= 0 || Number(tmp[1]) > 32) {
            return false;
        }
    }
    return true;
}


function validate_user_param() {
    var userName = $.trim($("#user_name").val());
    //var password = $.trim($("#password").val());
    var realName = $.trim($("#real_name").val());
    var userPost = $.trim($("#user_post").val());
    var company = $.trim($("#company").val());

    if (userName.length == 0) {
        $("#user_name").focus();
        tipsInputParamMsg("必填项", "#user_name");
        return false;
    }

    /*if (password.length == 0) {
        $("#password").focus();
        tipsInputParamMsg("必填项", "#password");
        return false;
    }*/

    if (realName.length == 0) {
        $("#real_name").focus();
        tipsInputParamMsg("必填项", "#real_name");
        return false;
    }
    if (userPost.length == 0) {
        $("#user_post").focus();
        tipsInputParamMsg("必填项", "#user_post");
        return false;
    }

    if (company.length == 0) {
        $("#company").focus();
        tipsInputParamMsg("必填项", "#company");
        return false;
    }
    return true;
}

function handler_delete_user(target) {
    if (target.nodeName === 'BUTTON' && $(target).attr("name") == "delete-user") {
        var userName = $(target).attr("realName");
        var msg = "确定删除用户名为“" + userName + "”的用户信息吗？";
        confirmMsg(msg, function () {
            delete_user($(target).attr("userId"));
        }, function () {
            // nothing to do
        });
    }
}

function delete_user(userId) {
    var param = {"id": userId};
    ajaxDataController(globalApi("api.user.delete"), JSON.stringify(param), null, function (data) {
        if (data && data.code === 0 && data.data > 0) {
            successAndFn("删除成功", function () {
                $("#table_user_list").bootstrapTable("refresh");
            });
        } else {
            errorMsg("删除异常. " + data.msg);
        }
    }, true, "post");
}

function handler_reset_password(target) {
    if (target.nodeName === 'BUTTON' && $(target).attr("name") == "reset-pwd") {
        var msg = "确定重置“" + $(target).attr("realName") + "”的登陆密码？";
        confirmMsg(msg, function () {
            reset_password($(target).attr("userId"));
        }, function () {
            // nothing to do
        });
    }
}

function reset_password(userId) {
    promptMsg("请输入新的密码", function (new_value) {
        reset_password_call_back(userId, new_value);
    });
}

function reset_password_call_back(userId, password) {
    var param = {"id": userId, "password": password};
    ajaxDataController(globalApi("api.user.resetPwd"), JSON.stringify(param), null, function (data) {
        if (data && data.code === 0 && data.data > 0) {
            successAndFn("密码重置成功", function () {
                $("#table_user_list").bootstrapTable("refresh");
            });
        } else {
            errorMsg("密码重置异常. " + data.msg);
        }
    }, true, "post");
}

/**
 * 动态构建“启用禁用”按钮，并且该按钮的click事件关联handler_app_status()
 **/
function handler_app_status_call_back(target, new_status) {
    if (target.nodeName === 'BUTTON' && $(target).attr("name") == "handle-app") {
        var param = {
            "appId": $(target).attr("appId"),
            "domain": $(target).attr("domain"),
            "appName": $(target).attr("appName"),
            "status": new_status
        };
        ajaxDataController(globalApi("api.app.update"), JSON.stringify(param), null, function (data) {
            if (data && data.code === 0 && data.data > 0) {
                successMsgFn("操作成功");
                // 如下操作，可以防止table_app_list刷新两次
                setTimeout(function () {
                    $("#table_app_list").bootstrapTable("refresh");
                }, 100);
            } else {
                errorMsg("操作异常.原因是: " + data.msg);
            }
        }, true, "post");
    }
}