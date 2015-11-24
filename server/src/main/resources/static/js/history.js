// TODO: load data per xhr

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

        conf.svg.append("text")
        		.attr("transform", "translate(" + conf.labelX + "," + conf.labelY + ")")
        		.attr("dy", ".35em")
        		.attr('class', conf.cls)
        		.attr("text-anchor", "start")
        		.text(conf.label);
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
            this.element.querySelector('.highest-bidder-value').innerHTML = renderCoins(dataPoint.buyStatistics.maxPrice);
            this.element.querySelector('.avg-bidder-value').innerHTML = renderCoins(dataPoint.buyStatistics.average);
            this.element.querySelector('.lowest-seller-value').innerHTML = renderCoins(dataPoint.sellStatistics.minPrice);
            this.element.querySelector('.avg-seller-value').innerHTML = renderCoins(dataPoint.sellStatistics.average);
            this.element.style.left = (x  - this.element.clientWidth - 30) + "px";
            this.element.style.top = (y  - (this.element.clientHeight / 2)) + "px";
        } else {
        this.element.style.display = '';
        }
    };
    tooltip.prototype.hide = function() {
        this.element.style.display = 'none';
    };

    var createTimeAxis = function(width, fromTimestamp, toTimestamp) {
        var scale = d3.time.scale()
            .range([0, width])
            .domain([ fromTimestamp, toTimestamp ]);

        var tickFormat = d3.time.format.multi([
            ["%H:%M", function(d) { return d.getHours(); }],
            ["%e %b", function(d) { return true; }]
        ]);

        var axis = d3.svg.axis()
            .scale(scale)
            .orient("bottom")
            .tickFormat(tickFormat)
            .ticks(d3.time.hours, 8);

        return {
            scale: scale,
            axis: axis
        };
    };

    var bisectDate = d3.bisector(function(d) { return d.timestamp; }).left;
    var selectDataPoint = function(data, eventHandler, scale) {
        var x0 = scale.invert(d3.mouse(eventHandler)[0]),
            i = bisectDate(data, x0, 1),
            d0 = data[i - 1],
            d1 = data[i],
            d = x0 - d0.timestamp > d1.timestamp - x0 ? d1 : d0;

        return d;
    };

    var renderCoins = function(coins) {
        coins = Math.floor(coins);
        var copper = coins % 100;
        coins = Math.floor(coins / 100.0);
        var silver = coins % 100;
        coins = Math.floor(coins / 100.0);
        var gold = coins;

        var html = '';
        if (gold > 0) {
            html += '<span class="currency-gold">' + gold + '</span>';
        }
        if (silver > 0) {
            html += '<span class="currency-silver">' + silver + '</span>';
        }
        html += '<span class="currency-copper">' + copper + '</span>';

        return html;
    };

    window.gw2charts = {
        Line: line,
        Tooltip: tooltip,
        createTimeAxis: createTimeAxis,
        selectDataPoint: selectDataPoint
    };
})();

(function() {
    var margin = {
        top: 20,
        right: 150,
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

    var timeAxis = gw2charts.createTimeAxis(width,
            // TODO: scala depending on the selected time frame
            // new Date(data[data.length - 1].timestamp - (7 * 24 * 60 * 60 * 1000)),
            new Date(data[data.length - 1].timestamp - (2 * 60 * 60 * 1000)),
            new Date(data[data.length - 1].timestamp)
        );

    var x = timeAxis.scale;

    var y = d3.scale.linear()
        .range([height, 0])
        .domain([ 0, maxPrice * 1.1 ]);

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
        labelX: width + 4,
        labelY: y(data[data.length - 1].sellStatistics.minPrice),
        label: 'Lowest sellers',
        cls: "gw2-history-sellers",
        focusCls: "gw2-history-sellers-focus"
    });
    var sellersAvgPrice = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.sellStatistics.average); },
        labelX: width + 4,
        labelY: y(data[data.length - 1].sellStatistics.average),
        label: 'Avg. sellers',
        cls: "gw2-history-sellers-avg",
        focusCls: "gw2-history-sellers-focus"
    });
    var buyersMaxPrice = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.buyStatistics.maxPrice); },
        labelX: width + 4,
        labelY: y(data[data.length - 1].buyStatistics.maxPrice),
        label: 'Highest buyers',
        cls: "gw2-history-buyers",
        focusCls: "gw2-history-buyers-focus"
    });
    var buyersAvgPrice = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.buyStatistics.average); },
        labelX: width + 4,
        labelY: y(data[data.length - 1].buyStatistics.average),
        label: 'Avg. buyers',
        cls: "gw2-history-buyers-avg",
        focusCls: "gw2-history-buyers-focus"
    });

    svg.append("g")
        .attr("class", "gw2-charts-x gw2-charts-axis")
        .attr("transform", "translate(0," + height + ")")
        .call(timeAxis.axis);
    svg.append("g")
        .attr("class", "gw2-charts-y gw2-charts-axis")
        .call(yAxis);

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
            var d = gw2charts.selectDataPoint(data, this, timeAxis.scale);

            buyersMaxPrice.highlight(x(d.timestamp), y(d.buyStatistics.maxPrice));
            buyersAvgPrice.highlight(x(d.timestamp), y(d.buyStatistics.average));
            sellersMinPrice.highlight(x(d.timestamp), y(d.sellStatistics.minPrice));
            sellersAvgPrice.highlight(x(d.timestamp), y(d.sellStatistics.average));

            tooltip.show(d3.event.pageX, d3.event.pageY, d);
        });
})();

(function() {
    var margin = {
        top: 20,
        right: 150,
        bottom: 30,
        left: 50
    },
    width = d3.select("#supplyHistory")[0][0].clientWidth - margin.left - margin.right,
    height = 400 - margin.top - margin.bottom,
    data = gw2historyData,
    totalAmount = d3.max(data, function(d) {
        return Math.max(
                d.buyStatistics.totalAmount,
                d.sellStatistics.totalAmount
        );
    }),
    bisectDate = d3.bisector(function(d) { return d.timestamp; }).left;

    var timeAxis = gw2charts.createTimeAxis(width,
            // TODO: scala depending on the selected time frame
            new Date(data[data.length - 1].timestamp - (7 * 24 * 60 * 60 * 1000)),
            new Date(data[data.length - 1].timestamp)
        );
    var x = timeAxis.scale;

    var y = d3.scale.linear()
        .range([height, 0])
        .domain([ 0, totalAmount * 1.1 ]);

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left");

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
        labelX: width + 4,
        labelY: y(data[data.length - 1].sellStatistics.totalAmount),
        label: 'Selles',
        cls: 'gw2-history-sellers',
        focusCls: 'gw2-history-sellers-focus'
    });
    var buyers = new gw2charts.Line({
        svg: svg,
        data: data,
        xFn: function(d) { return x(d.timestamp); },
        yFn: function(d) { return y(d.buyStatistics.totalAmount); },
        labelX: width + 4,
        labelY: y(data[data.length - 1].buyStatistics.totalAmount),
        label: 'Buyers',
        cls: 'gw2-history-buyers',
        focusCls: 'gw2-history-buyers-focus'
    });

    svg.append("g")
        .attr("class", "gw2-charts-x gw2-charts-axis")
        .attr("transform", "translate(0," + height + ")")
        .call(timeAxis.axis);

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
            var d = gw2charts.selectDataPoint(data, this, timeAxis.scale);
            sellers.highlight(x(d.timestamp), y(d.sellStatistics.totalAmount));
            buyers.highlight(x(d.timestamp), y(d.buyStatistics.totalAmount));
        });
})();