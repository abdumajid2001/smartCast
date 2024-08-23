package smartcast.abj.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smartcast.abj.dto.auth.AuthenticationRequest;
import smartcast.abj.dto.auth.AuthenticationResponse;
import smartcast.abj.service.AuthenticationService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }

    @PostMapping("accessToken")
    public ResponseEntity<AuthenticationResponse> accessToken(@RequestBody AuthenticationRequest request) {
        return service.accessToken(request);
    }

    @PostMapping("register")
    public ResponseEntity<Long> register(@RequestBody AuthenticationRequest registerDto) {
        return new ResponseEntity<>(
                service.register(registerDto),
                HttpStatus.OK
        );
    }

}
