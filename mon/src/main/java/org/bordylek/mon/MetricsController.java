package org.bordylek.mon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;

@Controller
public class MetricsController {

	@Value("${app.metrics.url}")
	private String url;

	private RestTemplate template = new RestTemplate();

	private static final Logger LOG = LoggerFactory.getLogger(MetricsController.class);

	@RequestMapping(value = "/metrics", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String read() {
		return template.getForEntity(url, String.class).getBody();
	}

}
