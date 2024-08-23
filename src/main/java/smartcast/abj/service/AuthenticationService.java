package smartcast.abj.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import smartcast.abj.dto.auth.AuthenticationRequest;
import smartcast.abj.dto.auth.AuthenticationResponse;
import smartcast.abj.entity.User;

import java.io.IOException;

public interface AuthenticationService {

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    ResponseEntity<AuthenticationResponse> accessToken(AuthenticationRequest request);

    User findById(Long id);

    Long register(AuthenticationRequest registerDto);

}
