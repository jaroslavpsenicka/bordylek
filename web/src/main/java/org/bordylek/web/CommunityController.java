package org.bordylek.web;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.event.EventQueue;
import org.bordylek.service.event.NewCommunityEvent;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.CommunityRepository;
import org.bordylek.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.Date;
import java.util.List;

@Controller
public class CommunityController {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EventQueue eventQueue;

	@Value("${comm.pageSize:25}")
	private int pageSize;

	@Value("${comm.defaultDistance:20}")
	private int defaultDistance;

	private static final Logger LOG = LoggerFactory.getLogger(CommunityController.class);

	public void setEventQueue(EventQueue eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	@RequestMapping(value = "/comm", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public Community create(@Valid @RequestBody Community comm) {
		comm.setCreateDate(new Date());
		Community community = this.communityRepository.save(comm);
		LOG.info("New community created: "+community.getTitle());
		eventQueue.send(new NewCommunityEvent(community));
		return community;
	}

	@RequestMapping(value = "/comm/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasRole('ADMIN')")
	public Community update(@PathVariable("id") String id, @Valid @RequestBody Community comm) {
		comm.setId(id);
		return (Community) this.communityRepository.save(comm);
	}

	@RequestMapping(value = "/comm/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("hasRole('ADMIN')")
	public void delete(@PathVariable("id") String id) {
		this.communityRepository.delete((Community) this.communityRepository.findOne(id));
	}

	@RequestMapping(value = "/comm", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public List<Community> findAll(@RequestParam(value = "page", defaultValue = "0") int pageNumber, 
		@RequestParam(value = "defaultDistance", required = false) Integer distance) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) auth.getPrincipal();
		Pageable request = new PageRequest(pageNumber, pageSize);
		double[] loc = user.getLocation();
		if (loc != null) {
	        Point pos = new Point(loc[0], loc[1]);
			int distValue = (distance != null) ? distance.intValue() : this.defaultDistance;
	        Distance dist = new Distance(distValue, Metrics.KILOMETERS);
			return communityRepository.findByLocationNear(pos, dist, request).getContent();
		}
		
		return communityRepository.findAll(request).getContent();
	}
	
	@ExceptionHandler(ValidationException.class)
	public void handleConstraintViolationException(ValidationException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

}
