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
    });

    var x = d3.time.scale()
        .range([0, width])
        .domain([new Date(data[0].timestamp), new Date(data[data.length - 1].timestamp)]);

    var y = d3.scale.linear()
        .range([height, 0])
        .domain([ 0, maxPrice * 1.1 ]);

    var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom")
        .tickFormat(d3.time.format("%I:%M"));

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

    svg.append("rect")
        .attr("class", "gw2-charts-overlay")
        .attr("width", width)
        .attr("height", height);

    // TODO: mouse-over
    // TODO: load data per xhr
    // TODO: legend
    // TODO: line namings
    // TODO: better x-axis (per day, etc.)
    // TODO: "mini-map"/long term timeline
    // TODO: some orientational lines
})();