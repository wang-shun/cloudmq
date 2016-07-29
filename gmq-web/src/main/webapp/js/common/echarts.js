function buildEchart(containerEl, titleText, subTitleText, legendArray, xArray, yName, colorArray, dataArray, brokerAddr) {
    console.log(legendArray);
    console.log(dataArray);


    var option = {
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

    var option111 = {
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
    var myChart = echarts.init(containerEl); // main

    //显示图形
    myChart.setOption(option111);
}