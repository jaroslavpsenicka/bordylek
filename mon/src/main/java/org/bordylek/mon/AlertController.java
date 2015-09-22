package org.bordylek.mon;

import org.bordylek.mon.model.Alert;
import org.bordylek.mon.model.Severity;
import org.bordylek.mon.repository.AlertRepository;
import org.bordylek.service.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class AlertController {

	@Autowired
	private AlertRepository alertRepository;

	@Autowired
	private ConfigurationService config;

	private static final Logger LOG = LoggerFactory.getLogger(AlertController.class);

	@RequestMapping(value = "/alerts", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<Alert> getAlerts(@RequestParam(value = "all", required = false) Boolean showAll) {
		return new ArrayList<Alert>() {{
			add(new Alert("Basic.memory", new Date(), Severity.WARNING, "Something wrong happened again."));
			add(new Alert("Basic.memory", new Date(System.currentTimeMillis() - 1000*60*60*25), Severity.INFO, "Something wrong happened."));
		}};
//		return (showAll != null && showAll) ? alertRepository.findByOrderByTimestampDesc() :
//			alertRepository.findByResolvedOrderByTimestampDesc(false);
	}

	@RequestMapping(value = "/alerts/{alertId}/resolve", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void resolveAlert(@PathVariable("alertId") String alertId) {
		Alert alert = alertRepository.findOne(alertId);
		if (alert == null) throw new NotFoundException(alertId);
		alert.setResolved(true);
		alertRepository.save(alert);
	}

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

}
