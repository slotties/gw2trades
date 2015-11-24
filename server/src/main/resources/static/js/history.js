(function() {
    var line = function(conf) {
        this.line = d3.svg.line().x(conf.xFn).y(conf.yFn);
        this.path = conf.svg.append("path")
            .datum(conf.data)
            .attr("class", conf.cls)
            .attr("d", this.line);

        this.focusCircle = conf.svg.append("g")
            .attr("class", conf.focusCls)
            .style("display", "none");

        this.focusCircle.append("circle").attr("r", 3);
    };
    line.prototype.hideHighlight = function() {
        this.focusCircle.style("display", "none");
    };
    line.prototype.showHighlight = function() {
        this.focusCircle.style("display", null);
    };
    line.prototype.highlight = function(x, y) {
        this.focusCircle.attr("transform", "translate(" + x + "," + y + ")");
    };

    var tooltip = function(element) {
        this.element = element;
    };
    tooltip.prototype.show = function(x, y, dataPoint) {
        if (x && y && dataPoint) {
            this.element.querySelector('.highest-bidder-value').innerHTML = dataPoint.buyStatistics.maxPrice;
            this.element.querySelector('.avg-bidder-value').innerHTML = dataPoint.buyStatistics.average;
            this.element.querySelector('.lowest-seller-value').innerHTML = dataPoint.sellStatistics.minPrice;
            this.element.querySelector('.avg-seller-value').innerHTML = dataPoint.sellStatistics.average;
            this.element.style.left = (x  - this.element.clientWidth - 30) + "px";
            this.element.style.top = (y  - (this.element.clientHeight / 2)) + "px";
        } else {
        this.element.style.display = '';
        }
    };
    tooltip.prototype.hide = function() {
        this.element.style.display = 'none';
    };

    window.gw2charts = {
        Line: line,
        Tooltip: tooltip
    };
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

    var svg = d3.select("#priceHistory").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var sellersMinPrice = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.sellStatistics.minPrice); },
        cls: "gw2-history-sellers",
        focusCls: "gw2-history-sellers-focus"
    });
    var sellersAvgPrice = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.sellStatistics.average); },
        cls: "gw2-history-sellers-avg",
        focusCls: "gw2-history-sellers-focus"
    });
    var buyersMaxPrice = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.buyStatistics.maxPrice); },
        cls: "gw2-history-buyers",
        focusCls: "gw2-history-buyers-focus"
    });
    var buyersAvgPrice = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.buyStatistics.average); },
        cls: "gw2-history-buyers-avg",
        focusCls: "gw2-history-buyers-focus"
    });

    svg.append("g")
        .attr("class", "gw2-charts-x gw2-charts-axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);
    svg.append("g")
        .attr("class", "gw2-charts-y gw2-charts-axis")
        .call(yAxis);

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

    var tooltip = new gw2charts.Tooltip(document.getElementById('priceHistoryTooltip'));

    svg.append("rect")
        .attr("class", "gw2-charts-overlay")
        .attr("width", width)
        .attr("height", height)
        .on("mouseover", function() {
            buyersMaxPrice.showHighlight();
            buyersAvgPrice.showHighlight();
            sellersMinPrice.showHighlight();
            sellersAvgPrice.showHighlight();
            tooltip.show();
        })
        .on("mouseout", function() {
            buyersMaxPrice.hideHighlight();
            buyersAvgPrice.hideHighlight();
            sellersMinPrice.hideHighlight();
            sellersAvgPrice.hideHighlight();
            tooltip.hide();
        })
        .on("mousemove", function() {
            var x0 = x.invert(d3.mouse(this)[0]),
                    i = bisectDate(data, x0, 1),
                    d0 = data[i - 1],
                    d1 = data[i],
                    d = x0 - d0.timestamp > d1.timestamp - x0 ? d1 : d0;

            buyersMaxPrice.highlight(x(d.timestamp), y(d.buyStatistics.maxPrice));
            buyersAvgPrice.highlight(x(d.timestamp), y(d.buyStatistics.average));
            sellersMinPrice.highlight(x(d.timestamp), y(d.sellStatistics.minPrice));
            sellersAvgPrice.highlight(x(d.timestamp), y(d.sellStatistics.average));

            tooltip.show(d3.event.pageX, d3.event.pageY, d);
        });

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
    }),
    bisectDate = d3.bisector(function(d) { return d.timestamp; }).left;

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

    var buyers  = d3.svg.line()
        .x(function(d) { return x(d.timestamp); })
        .y(function(d) { return y(d.buyStatistics.totalAmount); });

    var svg = d3.select("#supplyHistory").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var sellers = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.sellStatistics.totalAmount); },
        cls: 'gw2-history-sellers',
        focusCls: 'gw2-history-sellers-focus'
    });
    var buyers = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.buyStatistics.totalAmount); },
        cls: 'gw2-history-buyers',
        focusCls: 'gw2-history-buyers-focus'
    });

    svg.append("g")
        .attr("class", "gw2-charts-x gw2-charts-axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis);

    svg.append("g")
        .attr("class", "gw2-charts-y gw2-charts-axis")
        .call(yAxis);

    svg.append("rect")
        .attr("class", "gw2-charts-overlay")
        .attr("width", width)
        .attr("height", height)
        .on("mouseover", function() {
            sellers.showHighlight();
            buyers.showHighlight();
        })
        .on("mouseout", function() {
            sellers.hideHighlight();
            buyers.hideHighlight();
        })
        .on("mousemove", function() {
            var x0 = x.invert(d3.mouse(this)[0]),
                    i = bisectDate(data, x0, 1),
                    d0 = data[i - 1],
                    d1 = data[i],
                    d = x0 - d0.timestamp > d1.timestamp - x0 ? d1 : d0;

            sellers.highlight(x(d.timestamp), y(d.sellStatistics.totalAmount));
            buyers.highlight(x(d.timestamp), y(d.buyStatistics.totalAmount));
        });

    // TODO: mouse-over
    // TODO: load data per xhr
    // TODO: legend
    // TODO: line namings
    // TODO: "mini-map"/long term timeline
    // TODO: some orientational lines
})();