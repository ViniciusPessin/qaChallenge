/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 83.825, "KoPercent": 16.175};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.637375, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.8656, 500, 1500, "04 - POST /confirmation.php (Confirmar Compra)"], "isController": false}, {"data": [0.0, 500, 1500, "02 - POST /reserve.php (Selecionar Voo)"], "isController": false}, {"data": [0.842, 500, 1500, "01 - GET / (Página Inicial)"], "isController": false}, {"data": [0.8756, 500, 1500, "03 - POST /purchase.php (Dados do Passageiro)"], "isController": false}, {"data": [0.658, 500, 1500, "04 - POST /confirmation.php (Spike)"], "isController": false}, {"data": [0.598, 500, 1500, "01 - GET / (Spike)"], "isController": false}, {"data": [0.5986666666666667, 500, 1500, "02 - POST /reserve.php (Spike)"], "isController": false}, {"data": [0.6386666666666667, 500, 1500, "03 - POST /purchase.php (Spike)"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 8000, 1294, 16.175, 744.339249999998, 67, 10702, 286.0, 1977.0, 3161.8499999999995, 5552.879999999997, 75.71455612341472, 451.6846766012446, 19.180497142958547], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["04 - POST /confirmation.php (Confirmar Compra)", 1250, 11, 0.88, 546.1039999999998, 69, 10432, 250.5, 1008.9000000000001, 1654.2000000000007, 5497.240000000001, 12.57191133282375, 70.36101972030012, 5.537042979593274], "isController": false}, {"data": ["02 - POST /reserve.php (Selecionar Voo)", 1250, 1250, 100.0, 544.6255999999998, 67, 10702, 255.0, 1071.9, 1934.250000000003, 5476.530000000001, 12.760961666071154, 90.77191057118064, 3.05315977362054], "isController": false}, {"data": ["01 - GET / (Página Inicial)", 1250, 11, 0.88, 581.8311999999991, 68, 10375, 261.5, 1175.8000000000002, 2010.3000000000006, 5611.9000000000015, 12.740801141575782, 58.689933094868, 1.493062633778412], "isController": false}, {"data": ["03 - POST /purchase.php (Dados do Passageiro)", 1250, 6, 0.48, 505.88480000000015, 78, 10523, 250.0, 927.0, 1650.9500000000032, 5090.410000000003, 12.819329497789948, 83.72332137289891, 3.642993050641479], "isController": false}, {"data": ["04 - POST /confirmation.php (Spike)", 750, 2, 0.26666666666666666, 1036.8586666666663, 76, 5875, 341.0, 2965.0, 3742.1499999999996, 5690.620000000001, 34.8658825717075, 196.29619003068197, 10.861539590209661], "isController": false}, {"data": ["01 - GET / (Spike)", 750, 2, 0.26666666666666666, 1103.017333333334, 104, 5891, 561.0, 2757.2, 3493.199999999995, 5751.68, 35.04017940571856, 162.3646938072323, 4.106271024107643], "isController": false}, {"data": ["02 - POST /reserve.php (Spike)", 750, 6, 0.8, 1094.8920000000019, 69, 5811, 513.5, 3033.1, 3750.9499999999966, 5426.870000000002, 35.89375448671931, 255.32124910265614, 8.587861180904524], "isController": false}, {"data": ["03 - POST /purchase.php (Spike)", 750, 6, 0.8, 1074.1080000000004, 70, 5871, 403.5, 3191.8999999999996, 3770.5999999999995, 5634.56, 35.59563360227812, 231.75223214285714, 8.273203903654485], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Deve exibir lista de voos dispon&iacute;veis", 1240, 95.82689335394127, 15.5], "isController": false}, {"data": ["429/Too Many Requests", 54, 4.1731066460587325, 0.675], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 8000, 1294, "Deve exibir lista de voos dispon&iacute;veis", 1240, "429/Too Many Requests", 54, "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["04 - POST /confirmation.php (Confirmar Compra)", 1250, 11, "429/Too Many Requests", 11, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["02 - POST /reserve.php (Selecionar Voo)", 1250, 1250, "Deve exibir lista de voos dispon&iacute;veis", 1240, "429/Too Many Requests", 10, "", "", "", "", "", ""], "isController": false}, {"data": ["01 - GET / (Página Inicial)", 1250, 11, "429/Too Many Requests", 11, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["03 - POST /purchase.php (Dados do Passageiro)", 1250, 6, "429/Too Many Requests", 6, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["04 - POST /confirmation.php (Spike)", 750, 2, "429/Too Many Requests", 2, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["01 - GET / (Spike)", 750, 2, "429/Too Many Requests", 2, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["02 - POST /reserve.php (Spike)", 750, 6, "429/Too Many Requests", 6, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["03 - POST /purchase.php (Spike)", 750, 6, "429/Too Many Requests", 6, "", "", "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
