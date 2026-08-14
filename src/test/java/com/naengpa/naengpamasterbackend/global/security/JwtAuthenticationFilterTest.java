package com.naengpa.naengpamasterbackend.global.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtTokenProvider,
            mock(UserDetailsService.class),
            new RestAuthenticationEntryPoint()
    );

    @Test
    void publicLoginRequestSkipsJwtFilterEvenWithAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader("Authorization", "Bearer expired-token");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void publicSignupRequestSkipsJwtFilterEvenWithAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/members");
        request.addHeader("Authorization", "Bearer expired-token");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void publicEmailCheckRequestSkipsJwtFilterEvenWithAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/members/check-email");
        request.addHeader("Authorization", "Bearer expired-token");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void prometheusRequestSkipsJwtFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/prometheus");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void publicEmailVerificationRequestSkipsJwtFilterEvenWithAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/email-verifications");
        request.addHeader("Authorization", "Bearer expired-token");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void publicEmailVerificationConfirmRequestSkipsJwtFilterEvenWithAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/email-verifications/confirm");
        request.addHeader("Authorization", "Bearer expired-token");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void authenticatedRequestUsesJwtFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/members/me");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void invalidTokenRequestReturnsUnauthorizedResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/members/me");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChainSpy filterChain = new MockFilterChainSpy();

        when(jwtTokenProvider.validateToken("invalid-token"))
                .thenThrow(new BadCredentialsException("유효하지 않은 JWT입니다."));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"success\":false");
        assertThat(response.getContentAsString()).contains("\"message\":\"인증이 필요합니다.\"");
        assertThat(filterChain.called).isFalse();
    }

    @Test
    void requestWithoutTokenContinuesToAuthorizationFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/members/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChainSpy filterChain = new MockFilterChainSpy();

        filter.doFilter(request, response, filterChain);

        assertThat(filterChain.called).isTrue();
        verify(jwtTokenProvider, never()).validateToken(null);
    }

    private static class MockFilterChainSpy extends org.springframework.mock.web.MockFilterChain {

        private boolean called;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                throws IOException, ServletException {
            called = true;
        }
    }
}
