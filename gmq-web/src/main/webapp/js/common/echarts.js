function initEcharts(containerId, titleText, subTitleText, legendArray, xArray, yName, colorArray, dataArray, nameArray) {
    var seriesInfo = new Array();
    if (nameArray != undefined && nameArray.length > 0) {
        $.each(nameArray, function (i, o) {
            var seriesItem = {
//                name:nameArray,
                radius : ['50%', '80%'],
                center: ['48%', '55%'],
                type: 'pie',
                itemStyle: {
                    normal: {
                        label: {
                            show: false
                        },
                        labelLine: {
                            show: false
                        }
                    },
                    emphasis: {
                        label: {
                            show: true,
                            position: 'center',
                            textStyle: {
                                fontSize: '30',
                                fontWeight: 'bold'
                            }
                        }
                    }
                },
                data: dataArray
//                itemStyle: {
//                    normal: {
//                        color: colorArray
//                    }
//                }
            };
            seriesInfo.push(seriesItem);
        });
    }

    var option = {
        title: {text: titleText, x: 'left', subtext: subTitleText},
        tooltip: {trigger: 'item', formatter: "{a} <br/>{b} : {c} ({d}%)"},
//        legend: {data:legendArray},
//        yAxis : [{type : 'value',name : yName }],
        calculable: true,
        series: seriesInfo
    }

    var myChart = echarts.init(document.getElementById(containerId));// 图表初始化的地方，在页面中要有一个地方来显示图表，他的ID是main
    // option = getOptionByArray(json, reportDesign);//得到option图形
    myChart.setOption(option); //显示图形

}