"use strict";

(function() {
    var bisectDate = d3.bisector(function(d) { return d.timestamp; }).left;

    var splitCoins = function(coins) {
        coins = Math.floor(coins);
        var copper = coins % 100;
        coins = Math.floor(coins / 100.0);
        var silver = coins % 100;
        coins = Math.floor(coins / 100.0);
        var gold = coins;

        return {
            gold: gold,
            silver: silver,
            copper: copper
        };
    };

    var chart = function(element, yAxisLabelFormatter) {
        var margin = {
            top: 20,
            right: 150,
            bottom: 30,
            left: 0
        },
        width = element[0][0].clientWidth - margin.left - margin.right,
        height = 400 - margin.top - margin.bottom,
        timeTickFormat = d3.time.format.multi([
                ["%H:%M", function(d) { return d.getHours(); }],
                ["%e %b", function(d) { return true; }]
            ]),
        self = this;

        var svg = element.append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        this.x = d3.time.scale().range([0, width]);
        this.xAxis = d3.svg.axis().scale(this.x).orient("bottom").tickFormat(timeTickFormat).ticks(d3.time.hours, 8);
        this.xFn = function(d) { return self.x(d.timestamp); };

        this.y = d3.scale.linear().range([height, 0]);
        // First the grid, then the axis. Otherwise the grid will lay over the axis.
        this.yGrid = d3.svg.axis().scale(this.y).orient("left").ticks(7).tickSize(-width, 0, 0).tickFormat("");
        svg.append("g").attr("class", "grid").call(this.yGrid);

        this.yAxis = d3.svg.axis().scale(this.y).orient("right").ticks(7);
        if (yAxisLabelFormatter) {
            this.yAxis.tickFormat(yAxisLabelFormatter);
        }

        svg.append("g")
            .attr("class", "gw2-charts-x gw2-charts-axis")
            .attr("transform", "translate(0," + height + ")")
            .call(this.xAxis);
        svg.append("g")
            .attr("class", "gw2-charts-y gw2-charts-axis")
            .call(this.yAxis);

        svg.append("rect")
            .attr("class", "gw2-charts-overlay")
            .attr("width", width)
            .attr("height", height)
            .on("mouseover", function() {
                self.lines.forEach(function(line) {
                    line.focus.style("display", null);
                });
                if (self.tooltip) {
                    d3.select(self.tooltip.element).style('display', null);
                }
            })
            .on("mouseout", function() {
                self.lines.forEach(function(line) {
                    line.focus.style("display", "none");
                });
                if (self.tooltip) {
                    d3.select(self.tooltip.element).style('display', 'none');
                }
            })
            .on("mousemove", function() {
                if (!self.data || self.data.length < 2) {
                    return;
                }
                var x0 = self.x.invert(d3.mouse(this)[0]),
                    i = Math.min(self.data.length - 1, bisectDate(self.data, x0, 1)),
                    d0 = self.data[i - 1],
                    d1 = self.data[i],
                    d = x0 - d0.timestamp > d1.timestamp - x0 ? d1 : d0,
                    x = self.x(d.timestamp);

                self.lines.forEach(function(line) {
                    line.focus.attr("transform", "translate(" + x + "," + line.yFn(d, self.y) + ")");
                });
                if (self.tooltip) {
                    self.tooltip.updateFn(self.tooltip.element, d);
                    self.tooltip.element.style.left = (d3.event.pageX - self.tooltip.element.clientWidth - 30) + "px";
                    self.tooltip.element.style.top = (d3.event.pageY - (self.tooltip.element.clientHeight / 2)) + "px";
                }
            });

        this.svg = svg;
        this.lines = [];
    };
    chart.prototype.setupTooltip = function(element, updateFn) {
        this.tooltip = {
            element: element,
            updateFn: updateFn
        };
    };
    chart.prototype.add = function(conf) {
        var x = this.x,
            y = this.y,
            yFnWrapper = function(d) {
                return conf.yFn(d, y);
            },
            line = d3.svg.line().x(this.xFn).y(yFnWrapper),
            path = this.svg.append("path").attr("class", conf.cls),
            focusCircle = this.svg.append("g").attr("class", conf.focusCls).style("display", "none"),
            lineLabel = this.svg.append("text").attr("dy", ".35em").attr('class', conf.cls).attr("text-anchor", "start").text(conf.label);

        focusCircle.append("circle").attr("r", 3);

        this.lines.push({
            id: conf.id,
            line: line,
            path: path,
            focus: focusCircle,
            lineLabel, lineLabel,
            yFn: conf.yFn
        });
    };
    chart.prototype.lineVisibility = function(lineId, showLine) {
        var line;
        for (var i = 0; i < this.lines.length && !line; i++) {
            if (this.lines[i].id === lineId) {
                line = this.lines[i];
            }
        }

        if (line) {
            if (showLine) {
                line.lineLabel.style('visibility', 'visible');
                line.focus.style('visibility', 'visible');
                line.path.style('visibility', 'visible');
            } else {
                line.lineLabel.style('visibility', 'hidden');
                line.focus.style('visibility', 'hidden');
                line.path.style('visibility', 'hidden');
            }
        }
    };
    chart.prototype.update = function(data, xRange, yRange) {
        this.data = data;

        var timeDelta = xRange[1].getTime() - xRange[0].getTime(),
            // an approx. delta of days is sufficient here
            daysDelta = timeDelta / (24 * 60 * 60 * 1000);

        if (daysDelta > 30) {
            this.xAxis.ticks(d3.time.days, 7);
        } else if (daysDelta > 10) {
            this.xAxis.ticks(d3.time.days, 1);
        } else if (daysDelta > 2) {
            this.xAxis.ticks(d3.time.hours, 8);
        } else {
            this.xAxis.ticks(d3.time.hours, 1);
        }
        this.x.domain(xRange);
        this.y.domain(yRange);

        var svg = this.svg.transition(),
            lastDataPoint = this.data[this.data.length - 1],
            self = this;

        svg.select(".gw2-charts-x").call(this.xAxis);
        svg.select(".gw2-charts-y").transition().call(this.yAxis).call(function(selection) {
            selection.selectAll('text').attr('transform', 'translate(0, -8)');
        });
        svg.select('.grid').call(this.yGrid);

        this.lines.forEach(function(line) {
            line.path.attr('d', line.line(data));
            if (lastDataPoint) {
                line.lineLabel.attr("transform", "translate(" + (self.x(lastDataPoint.timestamp) + 10) + "," + line.yFn(lastDataPoint, self.y) + ")");
            }
        });
    };

    window.gw2charts = {
        Chart: chart,
        splitCoins: splitCoins
    };
})();

(function() {
    var renderCoins = function(coins) {
        var coinsObj = gw2charts.splitCoins(coins);
        var html = '';
        if (coinsObj.gold > 0) {
            html += '<span class="currency-gold">' + coinsObj.gold + '</span>';
        }
        if (coinsObj.silver > 0) {
            html += '<span class="currency-silver">' + coinsObj.silver + '</span>';
        }
        html += '<span class="currency-copper">' + coinsObj.copper + '</span>';

        return html;
    },
    renderDate = function(date) {
        var isoStr = date.toISOString();
        // Cut off the milliseconds and timezone.
        return isoStr.substring(0, isoStr.length - 5);
    },
    yScalePriceHistoryFn = function(data) {
        var maxPrice = d3.max(data, function(d) {
            return Math.max(
                Math.max(d.buyStatistics.maxPrice, d.sellStatistics.minPrice),
                Math.max(d.buyStatistics.average, d.sellStatistics.average)
                );
            });

        return [ 0, maxPrice * 1.1 ];
    },
    yScaleSupplyDemandFn = function(data) {
        var totalAmount = d3.max(data, function(d) {
            return Math.max(
                    d.buyStatistics.totalAmount,
                    d.sellStatistics.totalAmount
            );
        });

        return [ 0, totalAmount ];
    },
    priceTickFormat = function(d) {
        var coins = gw2charts.splitCoins(d),
            str = '';

        if (coins.gold > 0) {
            str += coins.gold + 'g ';
        }
        if (coins.silver > 0) {
            str += coins.silver + 's ';
        }
        str += coins.copper + 'c';

        return str;
    },
    updatePriceHistoryTooltip = function(element, data) {
        element.querySelector('.highest-bidder-value').innerHTML = renderCoins(data.buyStatistics.maxPrice);
        element.querySelector('.avg-bidder-value').innerHTML = renderCoins(data.buyStatistics.average);
        element.querySelector('.lowest-seller-value').innerHTML = renderCoins(data.sellStatistics.minPrice);
        element.querySelector('.avg-seller-value').innerHTML = renderCoins(data.sellStatistics.average);
    },
    createPriceHistoryChart = function() {
        var chart = new gw2charts.Chart(d3.select("#priceHistory"), priceTickFormat);
        chart.add({
            id: 'min_sellers',
            yFn: function(d, y) { return y(d.sellStatistics.minPrice); },
            label: 'Lowest sellers',
            cls: "gw2-history-sellers",
            focusCls: "gw2-history-sellers-focus"
        });
        chart.add({
            id: 'avg_sellers',
            yFn: function(d, y) { return y(d.sellStatistics.average); },
            label: 'Avg. sellers',
            cls: "gw2-history-sellers-avg",
            focusCls: "gw2-history-sellers-focus"
        });
        chart.add({
            id: 'max_buyers',
            yFn: function(d, y) { return y(d.buyStatistics.maxPrice); },
            label: 'Highest buyers',
            cls: "gw2-history-buyers",
            focusCls: "gw2-history-buyers-focus"
        });
        chart.add({
            id: 'avg_buyers',
            yFn: function(d, y) { return y(d.buyStatistics.average); },
            label: 'Avg. buyers',
            cls: "gw2-history-buyers-avg",
            focusCls: "gw2-history-buyers-focus"
        });
        chart.setupTooltip(document.getElementById('priceHistoryTooltip'), updatePriceHistoryTooltip);

        chart.lineVisibility('min_sellers', false);
        chart.lineVisibility('avg_sellers', false);
        chart.lineVisibility('max_buyers', false);
        chart.lineVisibility('avg_buyers', false);

        return chart;
    },
    createSupplyDemandChart = function() {
        var chart = new gw2charts.Chart(d3.select("#supplyDemand"));
        chart.add({
            yFn: function(d, y) { return y(d.sellStatistics.totalAmount); },
            label: 'Sellers',
            cls: 'gw2-history-sellers',
            focusCls: 'gw2-history-sellers-focus'
        });
        chart.add({
            yFn: function(d, y) { return y(d.buyStatistics.totalAmount); },
            label: 'Buyers',
            cls: 'gw2-history-buyers',
            focusCls: 'gw2-history-buyers-focus'
        });

        return chart;
    };

    var priceHistory = createPriceHistoryChart(),
        supplyDemand = createSupplyDemandChart();

    var onTimeselectorClick = function(btn) {
        var offset = btn.getAttribute('data-offset').split(/([0-9]*)/g),
            to = new Date(),
            from = new Date();

        // offset should look like this now: [ "", "1", "d" ] or [ "", "13", "m", "37", "d" ]
        if (offset.length > 1 && offset.length % 2 == 1) {
            for (var i = 1; i < offset.length; i += 2) {
                var amount = parseInt(offset[i], 10);
                switch (offset[i + 1]) {
                    case 'm':
                        from.setMonth(from.getMonth() - amount);
                        break;
                    case 'd':
                        from.setDate(from.getDate() - amount);
                        break;
                }
            }
        }

        var fromStr = renderDate(from),
            toStr = renderDate(to);

        d3.json('/api/history/' + gw2scope.itemId + '?from=' + fromStr + '&to=' + toStr, function(err, data) {
            var yScale = yScalePriceHistoryFn(data);
            priceHistory.update(data, [ from, to ], yScale);

            yScale = yScaleSupplyDemandFn(data);
            supplyDemand.update(data, [ from, to ], yScale);
        });

        var activeButtons = document.getElementById("chart-timeframe-selectors").querySelectorAll('button.active');
        for (var i = 0; i < activeButtons.length; i++) {
            activeButtons[i].className = activeButtons[i].className.replace(/active/, '');
        }
        btn.className += ' active';
    };
    var onLineSelectorClick = function(btn) {
        var showLine = btn.className.indexOf('active') < 0;
        if (showLine) {
            btn.className += ' active';
        } else {
            btn.className = btn.className.replace(/active/, '');
        }

        priceHistory.lineVisibility(btn.getAttribute('data-line'), showLine);
    };

    var timeframeSelectors = document.getElementById("chart-timeframe-selectors").querySelectorAll('button[data-offset]'),
        initialTimeframeSelector;
    for (var i = 0; i < timeframeSelectors.length; i++) {
        if (timeframeSelectors[i].getAttribute('data-initial') === 'true') {
            initialTimeframeSelector = timeframeSelectors[i];
        }
        timeframeSelectors[i].addEventListener('click', function() {
            onTimeselectorClick(this);
        });
    };

    var lineSelectors = document.getElementById('chart-line-selectors').querySelectorAll('button[data-line]'),
        initialLineSelectors = [];
    for (var i = 0; i < lineSelectors.length; i++) {
        if (lineSelectors[i].getAttribute('data-initial') === 'true') {
            initialLineSelectors.push(lineSelectors[i]);
        }

        lineSelectors[i].addEventListener('click', function() {
            onLineSelectorClick(this);
        });
    }

    if (initialTimeframeSelector) {
        onTimeselectorClick(initialTimeframeSelector);
    }
    if (initialLineSelectors) {
        for (var i = 0; i < initialLineSelectors.length; i++) {
            onLineSelectorClick(initialLineSelectors[i]);
        }
    }
})();
