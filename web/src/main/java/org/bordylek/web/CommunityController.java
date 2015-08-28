package org.bordylek.web;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.event.EventGateway;
import org.bordylek.service.event.NewCommunityEvent;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.Location;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.CommunityRepository;
import org.bordylek.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
	private EventGateway eventGateway;

	@Value("${comm.pageSize:25}")
	private int pageSize;

	@Value("${comm.defaultDistance:20}")
	private int defaultDistance;

	private static final Logger LOG = LoggerFactory.getLogger(CommunityController.class);

	public void setEventQueue(EventGateway eventGateway) {
		this.eventGateway = eventGateway;
	}

	@RequestMapping(value = "/comm", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasRole('USER')")
	public List<Community> find(@RequestParam(value = "page", defaultValue = "0") int pageNumber,
	  	@RequestParam(value = "dist", required = false) Integer distance) {
		Location loc = getUser().getLocation();
		Pageable request = new PageRequest(pageNumber, pageSize);
		if (loc != null) {
			int distValue = (distance != null) ? distance : this.defaultDistance;
			return communityRepository.findByLocationNear(loc.getLat(), loc.getLng(), distValue, request).getContent();
		}

		return communityRepository.findAll(request).getContent();
	}

	@RequestMapping(value = "/comm/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasRole('USER')")
	public Community find(@PathVariable("id") String id) {
		Community community = this.communityRepository.findOne(id);
		if (community == null) throw new NotFoundException(id);
		return community;
	}

	@RequestMapping(value = "/comm", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@PreAuthorize("hasRole('USER')")
	public Community create(@Valid @RequestBody Community comm) {
		comm.setCreateDate(new Date());
		Community community = this.communityRepository.save(comm);
		LOG.info("New community created: "+community.getTitle());
        eventGateway.send(new NewCommunityEvent(community));
		return community;
	}

//	@RequestMapping(value = "/comm/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
//	@ResponseStatus(HttpStatus.OK)
//	@ResponseBody
//	@PreAuthorize("hasRole('ADMIN')")
//	public Community update(@PathVariable("id") String id, @Valid @RequestBody Community comm) {
//		comm.setId(id);
//		return (Community) this.communityRepository.save(comm);
//	}

	@ExceptionHandler(ValidationException.class)
	public void handleConstraintViolationException(ValidationException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.BAD_REQUEST.value());
	}

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

	private User getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String principal = (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User)
			? ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername()
			: auth.getPrincipal().toString();
		return this.userRepository.findByRegId(principal);
	}
}
