package smartcast.abj.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.Transactional;
import smartcast.abj.dto.error.ErrorResponse;
import smartcast.abj.dto.auth.AuthenticationRequest;
import smartcast.abj.dto.auth.AuthenticationResponse;

import java.io.IOException;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    public AuthenticationFilter(ObjectMapper mapper, JwtService jwtService, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.mapper = mapper;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        super.setFilterProcessesUrl("/api/v1/auth/authenticate");
    }

    @SneakyThrows
    @Override
    @Transactional
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            AuthenticationRequest authenticationRequest = mapper.readValue(request.getReader(), AuthenticationRequest.class);
            UsernamePasswordAuthenticationToken authenticateToken = new UsernamePasswordAuthenticationToken(authenticationRequest.username(), authenticationRequest.password());
            return authenticationManager.authenticate(authenticateToken);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException();
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        MyUserDetails user = (MyUserDetails) authResult.getPrincipal();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        tokenService.revokeAllUserTokens(user.getId());
        tokenService.saveUserToken(user.getId(), jwtToken);

        AuthenticationResponse authenticationResponse = new AuthenticationResponse(jwtToken, refreshToken);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), authenticationResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.error("Error logging in: {}", failed.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ErrorResponse errorResponse =
                new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Invalid username or password."
                );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }

}
