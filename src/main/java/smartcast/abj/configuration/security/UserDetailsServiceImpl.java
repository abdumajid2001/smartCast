package smartcast.abj.configuration.security;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import smartcast.abj.exception.NotFoundException;
import smartcast.abj.projection.UserDetailsProjection;
import smartcast.abj.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public MyUserDetails loadUserByUsername(String username) {
        Optional<UserDetailsProjection> optionalUserDetails = repository.findByUsername(username);

        if (optionalUserDetails.isEmpty()) {
            logger.error("Username not found: {}", username);
            throw new NotFoundException("could not found user..!!");
        }
        UserDetailsProjection userDetails = optionalUserDetails.get();

        return new MyUserDetails(userDetails.getId(), userDetails.getUsername(), userDetails.getPassword(), userDetails.getPermissions());
    }

}
