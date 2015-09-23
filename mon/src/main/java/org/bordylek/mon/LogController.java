package org.bordylek.mon;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.model.Log;
import org.bordylek.service.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class LogController {

	@Autowired
	private LogRepository logRepository;

	private static final Logger LOG = LoggerFactory.getLogger(LogController.class);

	@RequestMapping(value = "/logs/{logId}", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public Log getLog(@PathVariable("logId") String logId) {
		Log log = logRepository.findOne(logId);
		if (log == null) throw new NotFoundException(logId);
		return log;
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
