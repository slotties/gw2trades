(function() {
    var margin = {
        top: 20,
        right: 50,
        bottom: 30,
        left: 50
    },
    width = d3.select("#priceHistory")[0][0].clientWidth - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom,
    data = gw2historyData,
    maxPrice = d3.max(data, function(d) {
        return Math.max(
            Math.max(d.buyStatistics.maxPrice, d.sellStatistics.minPrice),
            Math.max(d.buyStatistics.average, d.sellStatistics.average)
        );
    }),
    bisectDate = d3.bisector(function(d) { return d.timestamp; }).left;

    var x = d3.time.scale()
        .range([0, width])
        .domain([
            // TODO: scala depending on the selected time frame
            // new Date(data[data.length - 1].timestamp - (7 * 24 * 60 * 60 * 1000)),
            new Date(data[data.length - 1].timestamp - (2 * 60 * 60 * 1000)),
            new Date(data[data.length - 1].timestamp)
        ]);

    var y = d3.scale.linear()
        .range([height, 0])
        .domain([ 0, maxPrice * 1.1 ]);

    var xTickFormat = d3.time.format.multi([
        ["%I:%M", function(d) { return d.getMinutes(); }],
        ["%H:%M", function(d) { return d.getHours(); }],
        ["%e %b", function(d) { return true; }]
    ]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .tickFormat(xTickFormat)
        .ticks(d3.time.hours, 8);

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    var sellers = d3.svg.line()
        .x(function(d) { return x(d.timestamp); })
        .y(function(d) { return y(d.sellStatistics.minPrice); });
    var sellersAvg = d3.svg.line()
        .x(function(d) { return x(d.timestamp); })
        .y(function(d) { return y(d.sellStatistics.average); });

    var buyers  = d3.svg.line()
        .x(function(d) { return x(d.timestamp); })
        .y(function(d) { return y(d.buyStatistics.maxPrice); });
    var buyersAvg  = d3.svg.line()
        .x(function(d) { return x(d.timestamp); })
        .y(function(d) { return y(d.buyStatistics.average); });

    var svg = d3.select("#priceHistory").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("g")
        .attr("class", "gw2-charts-x gw2-charts-axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "gw2-charts-y gw2-charts-axis")
        .call(yAxis);

    svg.append("path")
        .datum(data)
        .attr("class", "gw2-history-sellers")
        .attr("d", sellers);
    svg.append("path")
        .datum(data)
        .attr("class", "gw2-history-buyers")
        .attr("d", buyers);
    svg.append("path")
        .datum(data)
        .attr("class", "gw2-history-sellers-avg")
        .attr("d", sellersAvg);
    svg.append("path")
        .datum(data)
        .attr("class", "gw2-history-buyers-avg")
        .attr("d", buyersAvg);

    var verticalLine = svg.append("div")
        .attr("class", "remove")
        .style("position", "absolute")
        .style("z-index", "19")
        .style("width", "1px")
        .style("height", "380px")
        .style("top", "10px")
        .style("bottom", "30px")
        .style("left", "0px")
        .style("background", "#fff");

    var buyersMaxPriceCircle = svg.append("g")
        .attr("class", "gw2-history-buyers-focus")
        .style("display", "none");
    buyersMaxPriceCircle.append("circle").attr("r", 3);

    var buyersAvgPriceCircle = svg.append("g")
        .attr("class", "gw2-history-buyers-focus")
        .style("display", "none");
    buyersAvgPriceCircle.append("circle").attr("r", 3);

    var sellersMinPriceCircle = svg.append("g")
        .attr("class", "gw2-history-sellers-focus")
        .style("display", "none");
    sellersMinPriceCircle.append("circle").attr("r", 3);

    var sellersAvgPriceCircle = svg.append("g")
        .attr("class", "gw2-history-sellers-focus")
        .style("display", "none");
    sellersAvgPriceCircle.append("circle").attr("r", 3);

    var tooltip = d3.select('body').append('div')
        .attr("class", "gw2-history-tooltip");

    svg.append("rect")
        .attr("class", "gw2-charts-overlay")
        .attr("width", width)
        .attr("height", height)
        .on("mouseover", function() {
            buyersMaxPriceCircle.style("display", null);
            buyersAvgPriceCircle.style("display", null);
            sellersMinPriceCircle.style("display", null);
            sellersAvgPriceCircle.style("display", null);
            tooltip.style('display', null);
        })
        .on("mouseout", function() {
            buyersMaxPriceCircle.style("display", "none");
            buyersAvgPriceCircle.style("display", "none");
            sellersMinPriceCircle.style("display", "none");
            sellersAvgPriceCircle.style("display", "none");
            tooltip.style('display', 'none');
        })
        .on("mousemove", function() {
            var x0 = x.invert(d3.mouse(this)[0]),
                    i = bisectDate(data, x0, 1),
                    d0 = data[i - 1],
                    d1 = data[i],
                    d = x0 - d0.timestamp > d1.timestamp - x0 ? d1 : d0;

            buyersMaxPriceCircle.attr("transform", "translate(" + x(d.timestamp) + "," + y(d.buyStatistics.maxPrice) + ")");
            buyersAvgPriceCircle.attr("transform", "translate(" + x(d.timestamp) + "," + y(d.buyStatistics.average) + ")");
            sellersMinPriceCircle.attr("transform", "translate(" + x(d.timestamp) + "," + y(d.sellStatistics.minPrice) + ")");
            sellersAvgPriceCircle.attr("transform", "translate(" + x(d.timestamp) + "," + y(d.sellStatistics.average) + ")");

            // TODO: use fancy price presentation
            var html =
                'Highest bidder: ' + d.buyStatistics.maxPrice + '<br>' +
                'Average bidder: ' + d.buyStatistics.average + '<br>' +
                'Lowest seller: ' + d.sellStatistics.minPrice + '<br>' +
                'Average seller: ' + d.sellStatistics.average;

            // TODO: display tooltip on the right side of the cursor in case we move too far to the left
            tooltip.html(html);
            tooltip.style("left", (d3.event.pageX - tooltip.property('clientWidth') - 30) + "px")
                    .style("top", (d3.event.pageY - (tooltip.property('clientHeight') / 2)) + "px");
        });

    // TODO: outsource basic components such as lines or focus circles
    // TODO: mouse-over
    // TODO: load data per xhr
    // TODO: legend
    // TODO: line namings
    // TODO: "mini-map"/long term timeline
    // TODO: some orientational lines
})();

(function() {
    var margin = {
        top: 20,
        right: 50,
        bottom: 30,
        left: 50
    },
    width = d3.select("#priceHistory")[0][0].clientWidth - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom,
    data = gw2historyData,
    totalAmount = d3.max(data, function(d) {
        return Math.max(
                d.buyStatistics.totalAmount,
                d.sellStatistics.totalAmount
        );
    });

    var x = d3.time.scale()
        .range([0, width])
        .domain([
            // TODO: scala depending on the selected time frame
            new Date(data[data.length - 1].timestamp - (7 * 24 * 60 * 60 * 1000)),
            new Date(data[data.length - 1].timestamp)
        ]);

    var y = d3.scale.linear()
        .range([height, 0])
        .domain([ 0, totalAmount * 1.1 ]);

    var xTickFormat = d3.time.format.multi([
        ["%I:%M", function(d) { return d.getMinutes(); }],
        ["%H:%M", function(d) { return d.getHours(); }],
        ["%e %b", function(d) { return true; }]
    ]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .tickFormat(xTickFormat)
        .ticks(d3.time.hours, 8);

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

    var sellers = d3.svg.line()
        .x(function(d) { return x(d.timestamp); })
        .y(function(d) { return y(d.sellStatistics.totalAmount); });

    var buyers  = d3.svg.line()
        .x(function(d) { return x(d.timestamp); })
        .y(function(d) { return y(d.buyStatistics.totalAmount); });

    var svg = d3.select("#supplyHistory").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("g")
        .attr("class", "gw2-charts-x gw2-charts-axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "gw2-charts-y gw2-charts-axis")
        .call(yAxis);

    svg.append("path")
        .datum(data)
        .attr("class", "gw2-history-sellers")
        .attr("d", sellers);
    svg.append("path")
        .datum(data)
        .attr("class", "gw2-history-buyers")
        .attr("d", buyers);

    svg.append("rect")
        .attr("class", "gw2-charts-overlay")
        .attr("width", width)
        .attr("height", height);

    // TODO: mouse-over
    // TODO: load data per xhr
    // TODO: legend
    // TODO: line namings
    // TODO: "mini-map"/long term timeline
    // TODO: some orientational lines
})();