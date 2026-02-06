package com.taskflow.config;

import com.taskflow.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    public void doFilterInternal_ShouldNotThrowException_WhenJwtExtractionFails() throws Exception {
        // Arrange
        Cookie cookie = new Cookie("token", "invalid_token");
        when(request.getCookies()).thenReturn(new Cookie[] { cookie });

        // Simulate an exception thrown by JwtService
        when(jwtService.extractUsername("invalid_token")).thenThrow(new RuntimeException("Jwt parsing failed"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Verify that the filter chain continues despite the exception
        verify(filterChain).doFilter(request, response);
        // Verify userDetailsService is NOT called because username extraction failed
        verify(userDetailsService, never()).loadUserByUsername(any());
    }
}
