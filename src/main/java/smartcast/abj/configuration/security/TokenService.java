package smartcast.abj.configuration.security;


import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import smartcast.abj.entity.Token;
import smartcast.abj.repository.TokenRepository;
import smartcast.abj.service.AuthenticationService;

import java.util.List;
import java.util.UUID;

@Service
public class TokenService {

    private final TokenRepository repository;
    private final AuthenticationService userService;

    public TokenService(TokenRepository repository,@Lazy AuthenticationService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public synchronized void revokeAllUserTokens(Long userId) {
        String random = UUID.randomUUID().toString();
        List<Token> tokens = repository.findAllValidTokenByUser(userId);
        tokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
            token.setToken(token.getToken() + random);
        });
        repository.saveAll(tokens);
    }

    public void saveUserToken(Long userId, String accessToken) {
        Token token = new Token(accessToken, userService.findById(userId));

        repository.save(token);
    }

}
