package smartcast.abj.configuration.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import smartcast.abj.enums.UserPermission;

import java.util.Collection;
import java.util.List;

@Getter
public class MyUserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private final Long id;

    private final String username;

    private final String password;

    private final List<UserPermission> permissions;

    public MyUserDetails(Long id, String username, String password, List<UserPermission> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
