package com.canvas.sync.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class UserAccessTokenService {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserAccessTokenService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public String getCurrentUserAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                registrationId, oauthToken.getName());
            if (client != null) {
                return client.getAccessToken().getTokenValue();
            }
        }
        return null; //TODO exception
    }
}