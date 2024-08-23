package smartcast.abj.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import smartcast.abj.configuration.security.JwtService;
import smartcast.abj.configuration.security.MyUserDetails;
import smartcast.abj.configuration.security.TokenService;
import smartcast.abj.configuration.security.UserDetailsServiceImpl;
import smartcast.abj.dto.auth.AuthenticationRequest;
import smartcast.abj.dto.auth.AuthenticationResponse;
import smartcast.abj.dto.error.ErrorResponse;
import smartcast.abj.entity.User;
import smartcast.abj.exception.AlreadyException;
import smartcast.abj.exception.NotFoundException;
import smartcast.abj.repository.UserRepository;
import smartcast.abj.service.AuthenticationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final ObjectMapper mapper;
    private final TokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    @Value("${baseUrl}")
    private String baseUrl;

    @Override
    public synchronized void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        final String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        if (Objects.nonNull(username)) {
            MyUserDetails user = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                tokenService.revokeAllUserTokens(user.getId());
                tokenService.saveUserToken(user.getId(), accessToken);

                AuthenticationResponse authenticationResponse = new AuthenticationResponse(accessToken, refreshToken);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                mapper.writeValue(response.getOutputStream(), authenticationResponse);
            }
        }
    }

    @Override
    public ResponseEntity accessToken(AuthenticationRequest request) {
        try {
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(baseUrl + "/api/v1/auth/authenticate");
            byte[] bytes = mapper.writeValueAsBytes(request);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httppost.setEntity(new InputStreamEntity(byteArrayInputStream));

            HttpResponse response = httpclient.execute(httppost);

            JsonNode jsonAuth = mapper.readTree(EntityUtils.toString(response.getEntity()));

            if (jsonAuth.has("code") && jsonAuth.has("message")) {
                int statusCode = response.getStatusLine().getStatusCode();
                return new ResponseEntity<>(mapper.readValue(jsonAuth.toString(), ErrorResponse.class), HttpStatus.resolve(statusCode));
            }

            return new ResponseEntity<>(mapper.readValue(jsonAuth.toString(), AuthenticationResponse.class), HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found userId:" + id));
    }

    @Override
    public Long register(AuthenticationRequest registerDto) {
        if (repository.existsByUsername(registerDto.username())) {
            throw new AlreadyException(registerDto.username() + "- is already registered !!!");
        }
        User user = new User(registerDto.username(), encoder.encode(registerDto.password()));

        return repository.save(user).getId();
    }

}
