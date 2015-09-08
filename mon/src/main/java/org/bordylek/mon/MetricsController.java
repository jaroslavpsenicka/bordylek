package org.bordylek.mon;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.model.*;
import org.bordylek.service.repository.MetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MetricsController {

	@Autowired
	private MetricsRepository metricsRepository;

	private static final Map<Class, String> METRIC_TYPES = new HashMap<Class, String>() {{
		put(Counter.class, "counter");
		put(Gauge.class, "gauge");
		put(Histogram.class, "histogram");
		put(Meter.class, "meter");
		put(Timer.class, "timer");
	}};

	private static final Logger LOG = LoggerFactory.getLogger(MetricsController.class);

	@RequestMapping(value = "/metrics", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, List<Metrics>> latestMetrics() {
		Metrics latestMetric = metricsRepository.findTopByOrderByTimestampDesc();
		if (latestMetric != null && latestMetric.getTimestamp() != null) {
			Map<String, List<Metrics>> latestMetrics = createLatestMetricsMap();
			for (Metrics metric : metricsRepository.findByTimestamp(latestMetric.getTimestamp())) {
				latestMetrics.get(METRIC_TYPES.get(metric.getClass())).add(metric);
			}

			return latestMetrics;
		}

		throw new NotFoundException("no data found");
	}

	private Map<String, List<Metrics>> createLatestMetricsMap() {
		Map<String, List<Metrics>> latestMetrics = new HashMap<>();
		for (String type : METRIC_TYPES.values()) latestMetrics.put(type, new ArrayList<Metrics>());
		return latestMetrics;
	}

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

}
