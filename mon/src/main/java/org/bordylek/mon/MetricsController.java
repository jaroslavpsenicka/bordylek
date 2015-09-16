package org.bordylek.mon;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.model.*;
import org.bordylek.service.model.Timer;
import org.bordylek.service.repository.MetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

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

	@RequestMapping(value = "/metrics/{type}", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, List<Metrics>> latestMetricsOfType(@PathVariable("type") String type) {
		Metrics latestMetric = metricsRepository.findTopByOrderByTimestampDesc();
		if (latestMetric != null && latestMetric.getTimestamp() != null) {
			for (Map.Entry<Class, String> entry : METRIC_TYPES.entrySet()) {
				if (entry.getValue().equals(type)) {
					String name = entry.getKey().getName();
					Date timestamp = latestMetric.getTimestamp();
					final List<Metrics> metricsList = metricsRepository.findAllOfTypeAndTimestamp(name, timestamp);
					return new HashMap<String, List<Metrics>>() {{
						put("data", metricsList);
					}};
				}
			}

			throw new IllegalArgumentException(type);
		}

		throw new NotFoundException("no data found");
	}

	@RequestMapping(value = "/metrics/{type}/{name:.+}", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Map<String, List<Metrics>> metricsOfType(@PathVariable("type") String type,
		@PathVariable("name") String name, @RequestParam(value = "period", defaultValue = "21600000") Integer period) {
		for (Map.Entry<Class, String> entry : METRIC_TYPES.entrySet()) {
			if (entry.getValue().equals(type)) {
				String className = entry.getKey().getName();
				Date date = new Date(System.currentTimeMillis() - period); // default 6 hrs in ms
				final List<Metrics> metricsList = metricsRepository.findAllOfTypeAndNameNewerThan(className, name, date);
				return new HashMap<String, List<Metrics>>() {{
					put("data", metricsList);
				}};
			}
		}

		throw new IllegalArgumentException(type);
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

	@ExceptionHandler(IllegalArgumentException.class)
	public void handleIllegalArgumentException(IllegalArgumentException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.BAD_REQUEST.value());
	}

}
