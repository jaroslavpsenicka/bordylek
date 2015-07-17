/*
 * Cloud Foundry 2012.02.03 Beta
 * Copyright (c) [2009-2012] VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product includes a number of subcomponents with
 * separate copyright notices and license terms. Your use of these
 * subcomponents is subject to the terms and conditions of the
 * subcomponent's license, as noted in the LICENSE file.
 */

package org.bordylek.web.security;

import org.bordylek.service.model.User;
import org.bordylek.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * Copied from the original implementation of the <code>DefaultUserAuthenticationConverter</code> to fix a bug in the
 * <code>getAuthorities</code> method. Rest all unchanged. Class with the original bug
 * <code>org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter</code>
 */
public class DefaultUserAuthenticationConverter extends org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter {

    @Autowired
    private UserRepository userRepository;

    private String[] defaultAuthorities;

    private static final String EMAIL = "email";

    public void setDefaultAuthorities(String[] defaultAuthorities) {
        this.defaultAuthorities = defaultAuthorities;
    }

    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USERNAME) && map.containsKey(EMAIL)) {
            User user = getUser((String) map.get(USERNAME), (String) map.get(EMAIL), map);
            List<GrantedAuthority> auths = commaSeparatedStringToAuthorityList(arrayToCommaDelimitedString(user.getRoles()));
            return new UsernamePasswordAuthenticationToken(user, "N/A", auths);
        }

        return null;
    }

    private User getUser(String regId, String email, Map<String, ?> map) {
        User user = userRepository.findByRegId(regId);
        if (user == null) {
            user = new User(regId, email, defaultAuthorities);
            user.setRegId(regId);
            user.setCreateDate(new Date());
            userRepository.save(user);
        }

        return user;
    }

}
