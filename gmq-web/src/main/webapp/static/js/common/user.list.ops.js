/**
 * Created by tianyuliang on 2016/11/7.
 */
$(function () {
    $('#table_user_list').bootstrapTable({
        url: globalApi("api.user.list"),
        dataField: "data",
        cache: false,
        striped: true,
        pagination: true,
        pageSize: 10,
        pageNumber: 1,
        pageList: [10, 20, 50],
        search: false,
        showRefresh: false,
        clickToSelect: true,
        toolbar: "#toolbar_user_list",
        sidePagination: "server",
        queryParamsType: "limit",
        responseHandler: function (res) {
            return res.data;
        },
        queryParams: function getParams(params) {
            params.userName = $.trim($("#search_user_name").val());
            return params;
        },
        columns: [{
            field: "id",
            title: "ID",
            align: "center",
            valign: "middle"
        }, {
            field: "userName",
            title: "用户名",
            align: "center",
            valign: "middle",
            formatter: "user_name_formatter"
        }, {
            field: "realName",
            title: "真实姓名",
            align: "center",
            valign: "middle"
        }, {
            field: "type",
            title: "员工类型",
            align: "center",
            valign: "middle",
            formatter: "user_type_formatter"
        }, {
            field: "post",
            title: "用户岗位",
            align: "center",
            valign: "middle"
        }, {
            field: "company",
            title: "所属公司",
            align: "center",
            valign: "middle"
        }, {
            field: "createName",
            title: "创建者",
            align: "center",
            valign: "middle"
        }, {
            field: "createDate",
            title: "创建时间",
            align: "center",
            valign: "middle"
        }, {
            field: "status",
            title: "操作",
            align: "center",
            valign: "middle",
            formatter: "user_status_formatter"
        }],
        formatNoMatches: function () {
            return "无符合条件的记录";
        }
    });


    // 重绘、渲染页面
    $(window).resize(function () {
        $("#table_user_list").bootstrapTable("resetView");
    });

    $("#search_user_name").keydown(function (event) {
        if (event.keyCode == 13) {
            search_user_name();
            event.preventDefault();
            return true;
        }
    });
})


function search_user_name() {
    $("#table_user_list").bootstrapTable("refresh");
}

function user_type_formatter(value, row, index) {
    return row.type === 1 ? "外包人员" : "正式员工";
}

function user_name_formatter(value, row, index) {
    return '<a href="user.update.html?id=' + row.id + '" title="" >' + row.userName + '</a>';
}

function user_status_formatter(value, row, index) {
    var btnHtml =
        '<button type="button" class="btn btn-default btn-sm btn-info" onclick="handler_user_list_click()" name="delete-user" '
        + ' userId="' + row.id + '" userName="' + row.userName + '" realName="' + row.realName+'" >删除'
        + '</button>';
    btnHtml += "&nbsp;&nbsp;&nbsp;&nbsp;";
    btnHtml += '<button type="button" class="btn btn-default btn-sm btn-info" onclick="handler_user_list_click()" name="reset-pwd" '
        + ' userId="' + row.id + '" userName="' + row.userName + '" realName="' + row.realName+'" >重置密码'
        + '</button>';
    return btnHtml;
}




