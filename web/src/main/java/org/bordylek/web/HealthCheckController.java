package org.bordylek.web;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.SortedMap;

@Controller
public class HealthCheckController {

	@Autowired
	private HealthCheckRegistry healthCheckRegistry;

	private static final Logger LOG = LoggerFactory.getLogger(HealthCheckController.class);

	@RequestMapping(value = "/health/check", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public SortedMap<String, HealthCheck.Result> healthCheck() {
		return healthCheckRegistry.runHealthChecks();
	}

}
