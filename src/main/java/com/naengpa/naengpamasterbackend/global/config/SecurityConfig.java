package com.naengpa.naengpamasterbackend.global.config;

import com.naengpa.naengpamasterbackend.global.auth.oauth2.handler.OAuth2LoginSuccessHandler;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.handler.OAuth2LoginFailureHandler;
import com.naengpa.naengpamasterbackend.global.security.JwtAuthenticationFilter;
import com.naengpa.naengpamasterbackend.global.security.JwtTokenProvider;
import com.naengpa.naengpamasterbackend.global.security.RestAccessDeniedHandler;
import com.naengpa.naengpamasterbackend.global.security.RestAuthenticationEntryPoint;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/health",
            "/actuator/health",
            "/error",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api/v1/members/check-email",
            "/oauth2/**",
            "/login/oauth2/**"
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/api/v1/members",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/email-verifications",
            "/api/v1/auth/email-verifications/**",
            "/api/v1/auth/oauth2/email-completion",
            "/api/v1/auth/oauth2/email-verifications"
    };

    private static final String[] ADMIN_ENDPOINTS = {
            "/api/v1/admin/**"
    };

    private static final String[] AUTHENTICATED_ENDPOINTS = {
            "/api/v1/members/me",
            "/api/v1/members/me/**",
            "/api/v1/products/search",
            "/api/v1/products/search/**",
            "/api/v1/categories",
            "/api/v1/fridge-items",
            "/api/v1/fridge-items/**",
            "/api/v1/fridges",
            "/api/v1/fridges/**",
            "/api/v1/subscriptions",
            "/api/v1/subscriptions/**",
            "/api/v1/auth/logout",
            "/api/v1/recipes/**",
            "/api/v1/comments/**",
            "/api/v1/shopping-items",
            "/api/v1/shopping-items/**",
            "/api/v1/inquiries/**",
            "/api/v1/scores",
            "/api/v1/scores/**",
            "/api/v1/member-stats/**",
            "/api/v1/notifications/**"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oauth2LoginFailureHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(restAuthenticationEntryPoint)
                                .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                        .requestMatchers(RegexRequestMatcher.regexMatcher(HttpMethod.GET, "^/api/v1/recipes/[0-9]+$")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/recipes/*/comments").permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS).hasAuthority("ADMIN")
                        .requestMatchers(AUTHENTICATED_ENDPOINTS).hasAnyAuthority("USER", "ADMIN")
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(oauth2AuthorizationRequestResolver()))
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler(oauth2LoginFailureHandler))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public Filter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, restAuthenticationEntryPoint);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
        return new LinkAwareOAuth2AuthorizationRequestResolver(resolver);
    }

    private static class LinkAwareOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

        private final OAuth2AuthorizationRequestResolver delegate;

        private LinkAwareOAuth2AuthorizationRequestResolver(OAuth2AuthorizationRequestResolver delegate) {
            this.delegate = delegate;
        }

        @Override
        public org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            saveLinkAccessToken(request);
            return delegate.resolve(request);
        }

        @Override
        public org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest resolve(
                HttpServletRequest request,
                String clientRegistrationId
        ) {
            saveLinkAccessToken(request);
            return delegate.resolve(request, clientRegistrationId);
        }

        private void saveLinkAccessToken(HttpServletRequest request) {
            String linkAccessToken = request.getParameter("linkAccessToken");
            if (linkAccessToken == null || linkAccessToken.isBlank()) {
                return;
            }
            request.getSession(true).setAttribute(
                    OAuth2LoginSuccessHandler.LINK_ACCESS_TOKEN_SESSION_ATTRIBUTE,
                    linkAccessToken
            );
        }
    }
}
