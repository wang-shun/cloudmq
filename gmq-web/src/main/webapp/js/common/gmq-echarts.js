/**
 * 绘制饼状图
 * @param containerEl
 * @param titleText
 * @param subTitleText
 * @param legendArray
 * @param xArray
 * @param yName
 * @param colorArray
 * @param dataArray
 * @param brokerAddr
 */
function buildPieEchart(containerEl, titleText, subTitleText, legendArray, xArray, yName, colorArray, dataArray, brokerAddr) {

    var tmpOption = {
        backgroundColor: "#F1FAFA",
        title: {
            text: titleText,
            subtext: subTitleText,
            x: "center"
        },
        tooltip: {
            trigger: "item",
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        legend: {
            orient: "vertical",
            x: "left",
            data: legendArray
        },
        yAxis: [{type: "value", name: yName}],
        calculable: true,
        series: {
            name: brokerAddr,
            type: "pie",
            radius: "55%",
            center: ["50%", "60%"],
            data: dataArray
        }
    };


    //饼状图option配置
    var option = {
        backgroundColor: "#F1FAFA",
        // 标题
        title: {
            text: titleText,
            subtext: subTitleText,
            x: 'center'
        },
        //提示
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        // 左上角选项描述
        legend: {
            orient: 'vertical',
            x: 'left',
            data: legendArray
        },

        calculable: true,
        series: [{
            name: brokerAddr,
            type: 'pie',
            radius: '55%',
            center: ['50%', '60%'],
            data: dataArray
        }]
    };


    //初始化显示图形的Dom元素
    var myChart = echarts.init(containerEl);

    //显示图形
    myChart.setOption(option);
}

/**
 * 绘制动态数据-折线图
 * @param containerEl
 * @param titleText
 * @param subTitleText
 * @param legendArray
 * @param xArray
 * @param yName
 * @param colorArray
 * @param dataArray
 * @param brokerAddr
 */
function buildLineEchart(containerEl, titleText, dataArray) {

    // 设置折线图的option
    var option111 = {
        title: {
            text: titleText
        },
        tooltip: {
            trigger: 'axis'
        },
        dataZoom : {
            show : false,
            start : 0,
            end : 100
        },
        xAxis: {
            type: 'time',
            splitLine: {
                show: false
            }
        },
        yAxis: {
            type: 'value',
            boundaryGap: [0, '100%'],
            splitLine: {
                show: false
            }
        },
        calculable : true,
        series: [{
            name: '消息发送TPS',
            type: 'line',
            showSymbol: false,
            hoverAnimation: false,
            data: dataArray
        }]
    };


    var option = {
        title : {
            text: titleText,
        },
        tooltip : {
            trigger: 'axis'
        },
        legend: {
            data:['最新成交价', '预购队列']
        },
        dataZoom : {
            show : false,
            start : 0,
            end : 100
        },
        xAxis : [
            {
                type : 'category',
                boundaryGap : true,
                data : (function (){
                    var now = new Date();
                    var res = [];
                    var len = 10;
                    while (len--) {
                        res.unshift(now.toLocaleTimeString().replace(/^\D*/,''));
                        now = new Date(now - 2000);
                    }
                    return res;
                })()
            },
            {
                type : 'category',
                boundaryGap : true,
                data : (function (){
                    var res = [];
                    var len = 10;
                    while (len--) {
                        res.push(len + 1);
                    }
                    return res;
                })()
            }
        ],
        yAxis : [
            {
                type : 'value',
                scale: true,
                name : '价格',
                boundaryGap: [0.2, 0.2]
            },
            {
                type : 'value',
                scale: true,
                name : '预购量',
                boundaryGap: [0.2, 0.2]
            }
        ],
        series : [
            {
                name:'预购队列',
                type:'bar',
                xAxisIndex: 1,
                yAxisIndex: 1,
                data:(function (){
                    var res = [];
                    var len = 10;
                    while (len--) {
                        res.push(Math.round(Math.random() * 1000));
                    }
                    return res;
                })()
            },
            {
                name:'最新成交价',
                type:'line',
                data:(function (){
                    var res = [];
                    var len = 10;
                    while (len--) {
                        res.push((Math.random()*10 + 5).toFixed(1) - 0);
                    }
                    return res;
                })()
            }
        ]
    };
    var lastData = 11;
    var axisData = null;
    var timeTicket = setInterval(function (){
        lastData += Math.random() * ((Math.round(Math.random() * 10) % 2) == 0 ? 1 : -1);
        lastData = lastData.toFixed(1) - 0;
        axisData = (new Date()).toLocaleTimeString().replace(/^\D*/,'');

        // 动态数据接口 addData
        myChart.addData([
            [
                0,        // 系列索引
                Math.round(Math.random() * 1000), // 新增数据
                true,     // 新增数据是否从队列头部插入
                false     // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
            ],
            [
                1,        // 系列索引
                lastData, // 新增数据
                false,    // 新增数据是否从队列头部插入
                false,    // 是否增加队列长度，false则自定删除原有数据，队头插入删队尾，队尾插入删队头
                axisData  // 坐标轴标签
            ]
        ]);
    }, 2100);

    clearInterval(timeTicket);

    //初始化显示图形的Dom元素
    var myChart = echarts.init(containerEl);

    //显示图形
    myChart.setOption(option);

}