package org.bordylek.web.security;

import org.bordylek.service.event.EventQueue;
import org.bordylek.service.event.NewUserEvent;
import org.bordylek.service.model.User;
import org.bordylek.service.model.UserStatus;
import org.bordylek.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.google.api.Google;
import org.springframework.web.context.request.NativeWebRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

public class SimpleSignInAdapter implements SignInAdapter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventQueue eventQueue;

    private final RequestCache requestCache;

    private static Logger LOG = LoggerFactory.getLogger(SimpleSignInAdapter.class);

    @Inject
    public SimpleSignInAdapter(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    @Override
    public String signIn(String userId, Connection<?> connection, NativeWebRequest request) {
        User user = userRepository.findByRegId(userId);
        if (user == null) {
            user = new User();
            user.setRegId(userId);
            user.setCreateDate(new Date());
            user.setStatus(UserStatus.NEW);
        }

        Connection<Google> googleConnection = (Connection<Google>) connection;
        user.setName(connection.getDisplayName());
        user.setEmail(googleConnection.fetchUserProfile().getEmail());

        boolean newUser = user.getId() == null;
        userRepository.save(user);
        if (newUser) {
            LOG.info("New user created: " + user.getName());
            eventQueue.send(new NewUserEvent(user));
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, "", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return extractOriginalUrl(request);
    }

    private String extractOriginalUrl(NativeWebRequest request) {
        HttpServletRequest nativeReq = request.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse nativeRes = request.getNativeResponse(HttpServletResponse.class);
        SavedRequest saved = requestCache.getRequest(nativeReq, nativeRes);
        if (saved == null) {
            return null;
        }
        requestCache.removeRequest(nativeReq, nativeRes);
        removeAutheticationAttributes(nativeReq.getSession(false));
        return saved.getRedirectUrl();
    }

    private void removeAutheticationAttributes(HttpSession session) {
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

}