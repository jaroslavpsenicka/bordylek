package org.bordylek.web;

import com.codahale.metrics.annotation.Timed;
import org.bordylek.service.NotFoundException;
import org.bordylek.service.event.EventGateway;
import org.bordylek.service.event.NewCommunityEvent;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.CommunityRef;
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
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	@Timed
	public List<Community> find(@RequestParam(value = "page", defaultValue = "0") int pageNumber,
	  	@RequestParam(value = "dist", required = false) Integer distance,
		@RequestParam(value = "include-all", defaultValue = "false") boolean includeAll) {
		User user = getUser();
		Pageable request = new PageRequest(pageNumber, pageSize);
		if (user.getLocation() != null) {
			int distValue = (distance != null) ? distance : this.defaultDistance;
			List<Community> communities = communityRepository.findByLocationNear(user.getLocation().getLat(),
				user.getLocation().getLng(), distValue, request).getContent();
			return includeAll ? communities : exclude(communities, user.getCommunities());
		}

		return communityRepository.findAll(request).getContent();
	}

	@RequestMapping(value = "/comm/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("hasRole('USER')")
	@Timed
	public Community find(@PathVariable("id") String id) {
		Community community = this.communityRepository.findOne(id);
		if (community == null) throw new NotFoundException(id);
		return community;
	}

	@RequestMapping(value = "/comm", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@PreAuthorize("hasRole('USER')")
	@Timed
	public Community create(@Valid @RequestBody CommunityCreateReq request) {
		Community comm = new Community();
		comm.setTitle(request.getTitle());
		comm.setCreatedBy(getUser());
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

	private List<Community> exclude(List<Community> communities, List<CommunityRef> subscribedCommunities) {
		if (subscribedCommunities != null && subscribedCommunities.size() > 0) {
			List<Community> filtered = new ArrayList<Community>();
			Set<String> subscribedIds = new HashSet<>();
			for (CommunityRef subscribedCommunity : subscribedCommunities) {
				subscribedIds.add(subscribedCommunity.getId());
			}
			for (Community community : communities) {
				if (!subscribedIds.contains(community.getId())) {
					filtered.add(community);
				}
			}

			return filtered;
		}

		return communities;
	}

	private static class CommunityCreateReq {

		@NotNull
		private String title;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

	}

}
