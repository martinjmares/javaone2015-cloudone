(function () {

    /* D3 Bubble Chart */

    var diameter = Math.min(document.getElementById("chart").clientWidth,
            window.innerHeight - document.querySelector("header").clientHeight) - 20;

    var color = d3.scale.category20c();

    var svg = d3.select("#chart").append("svg")
        .attr("width", diameter)
        .attr("height", diameter);

    var bubble = d3.layout.pack()
        .size([diameter, diameter])
        .value(function (d) {
            return d.size;
        }) // new data is loaded to bubble layout
        .padding(3);

    function drawBubbles(data) {
        // generate data with calculated layout values
        var nodes = bubble.nodes(processData(data))
            .filter(function (d) {
                return !d.children;
            }); // filter out the outer bubble

        // assign new data to existing DOM
        var circles = svg.selectAll('circle')
            .data(nodes, function (d) {
                return d.name;
            });
        var texts = svg.selectAll('text')
            .data(nodes, function (d) {
                return d.name;
            });

        // enter data -> remove, so non-exist selections for upcoming data won't stay -> enter new data -> ...

        // To chain transitions,
        // create the transition on the updating elements before the entering elements
        // because enter.append merges entering elements into the update selection

        var duration = 500;
        var delay = 0;

        // update - this is created before enter.append. it only applies to updating nodes.
        circles.transition()
            .duration(duration)
            .delay(function (d, i) {
                delay = i * 7;
                return delay;
            })
            .attr('transform', function (d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            })
            .attr('r', function (d) {
                return d.r;
            })
            .style('opacity', 1);
        texts.transition()
            .duration(duration)
            .delay(function (d, i) {
                delay = i * 7;
                return delay;
            })
            .attr('transform', function (d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            })
            .style('opacity', 1);

        // enter - only applies to incoming elements (once emptying data)
        circles.enter().append('circle')
            .style("fill", function (d) {
                return color(d.packageName);
            })
            .attr('transform', function (d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            })
            .attr('r', function (d) {
                return 0;
            })
            .attr('class', function (d) {
                return d.className;
            })
            .transition()
            .duration(duration * 1.2)
            .attr('transform', function (d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            })
            .attr('r', function (d) {
                return d.r;
            })
            .style('opacity', 1);
        texts.enter().append("text")
            .attr("dy", ".1em")
            .style("text-anchor", "middle")
            .text(function (d) {
                return d.className;
            })
            .attr('transform', function (d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            })
            .attr('font-size', 0)
            .transition()
            .ease("cubic-out")
            .duration(duration * 1.2)
            .attr('transform', function (d) {
                return 'translate(' + d.x + ',' + d.y + ')';
            })
            .attr("dy", ".3em")
            .attr('font-size', 20);

        // exit
        circles.exit()
            .transition()
            .duration(duration)
            .attr('transform', function (d) {
                var dy = d.y - diameter / 2;
                var dx = d.x - diameter / 2;
                var theta = Math.atan2(dy, dx);
                var destX = diameter * (1 + Math.cos(theta) ) / 2;
                var destY = diameter * (1 + Math.sin(theta) ) / 2;
                return 'translate(' + destX + ',' + destY + ')';
            })
            .attr('r', function (d) {
                return 0;
            })
            .remove();
    }

    function processData(data) {
        if (!data) {
            return;
        }

        var newDataSet = [];

        for (var prop in data) {
            newDataSet.push({name: prop, packageName: prop, className: prop, size: data[prop]});
        }

        return {children: newDataSet};
    }

    function receiveMessages() {
        if (typeof(EventSource) !== "undefined") {
            // Yes! Server-sent events support!
            var source = new EventSource("space-object/events");

            source.onmessage = function (event) {
                console.log("Received event: " + event.data);
                var data = JSON.parse(event.data);

                if (Object.keys(data).length) {
                    drawBubbles(data);
                }
            };

            source.onopen = function (event) {
                console.log("Event source opened.");
            };

            source.onerror = function (event) {
                console.log("Received error event: " + event.data);
            };
        } else {
            // Sorry! No server-sent events support..
            console.log("SSE not supported by browser.");
        }
    }

    window.onload = receiveMessages;

})();
