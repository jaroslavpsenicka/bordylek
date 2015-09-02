package org.bordylek.web;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import org.bordylek.service.NotFoundException;
import org.bordylek.service.event.EventGateway;
import org.bordylek.service.model.*;
import org.bordylek.service.repository.CommunityRepository;
import org.bordylek.service.repository.UserRepository;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
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
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;

@Controller
public class UserController {

	@Autowired
	private UserRepository repository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
	private EventGateway eventGateway;

	@Value("${user.pageSize:25}")
	private int pageSize;

    @Value("${comm.defaultDistance:20}")
    private int defaultDistance;

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = "/user/me", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
    @Timed
    @ExceptionMetered
	public MeResponse findMe(@RequestParam(value = "dist", required = false) Integer distance,
        @RequestParam(value = "include-all", defaultValue = "false") boolean includeAll) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedUserException("user not known");
        }

        String principal = (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User)
            ? ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername()
            : authentication.getPrincipal().toString();
        User user = this.repository.findByRegId(principal);
        if (user.getLocation() != null) {
            int distValue = (distance != null) ? distance : this.defaultDistance;
            Pageable page = new PageRequest(0, 5);
            List<Community> communities = communityRepository.findByLocationNear(user.getLocation().getLat(),
                    user.getLocation().getLng(), distValue, page).getContent();
            List<Community> nearby = includeAll ? communities : exclude(communities, user.getCommunities());
            return new MeResponse(user, toRefs(nearby));
        }

        return new MeResponse(user, null);
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    @Timed
    @ExceptionMetered
    public User findOne(@PathVariable("id") String id) {
        User user = this.repository.findOne(id);
        if (user == null) throw new NotFoundException(id);
        return user;
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    @Timed
    @ExceptionMetered
    public User update(@PathVariable("id") String id, @RequestBody @Valid UserUpdateReq req) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedUserException("user not known");
        }
        String principal = (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User)
            ? ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername()
            : authentication.getPrincipal().toString();
        User updatingUser = this.repository.findByRegId(principal);
        if (updatingUser == null || !updatingUser.getId().equals(id)) throw new IllegalAccessException();

        User dbUser = this.repository.findOne(id);
        if (dbUser == null) throw new NotFoundException(id);
        dbUser.setName(req.getName());
        dbUser.setLocation(req.getLocation());
        dbUser.setStatus(UserStatus.VERIFIED);
        return this.repository.save(dbUser);
    }

	@ExceptionHandler(NotFoundException.class)
	public void handleNotFoundException(NotFoundException ex, HttpServletResponse response) {
		response.setStatus(HttpStatus.NOT_FOUND.value());
	}

    @ExceptionHandler(IllegalAccessException.class)
    public void handleIllegalAccessException(IllegalAccessException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler(UnauthorizedUserException.class)
    public void unauthorizedUserException(UnauthorizedUserException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
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

    private List<CommunityRef> toRefs(List<Community> communities) {
        List<CommunityRef> refs = new ArrayList<>();
        for (Community community : communities) {
            refs.add(new CommunityRef(community.getId(), community.getTitle()));
        }

        return refs;
    }

    public static class MeResponse {

        private User user;
        private List<CommunityRef> nearby;

        public MeResponse(User user, List<CommunityRef> nearby) {
            this.user = user;
            this.nearby = nearby;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public List<CommunityRef> getNearby() {
            return nearby;
        }

        public void setNearby(List<CommunityRef> nearby) {
            this.nearby = nearby;
        }

    }

    public static class UserUpdateReq {

        @NotEmpty(message = "name may not be null")
        @Length(min = 3, max = 255, message = "name should be 3-255 characters long")
        private String name;

        @Valid @NotNull(message = "location may not be null")
        private Location location;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

}
