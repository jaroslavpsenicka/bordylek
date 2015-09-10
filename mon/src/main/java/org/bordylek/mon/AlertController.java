package org.bordylek.mon;

import org.bordylek.mon.model.Alert;
import org.bordylek.mon.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
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
		return (showAll != null && showAll) ? alertRepository.findByOrderByTimestampDesc() :
			alertRepository.findByResolvedOrderByTimestampDesc(false);
	}

}
