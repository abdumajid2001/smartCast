package smartcast.abj.projection;

import smartcast.abj.enums.UserPermission;

import java.util.List;

public interface UserDetailsProjection {

    Long getId();

    String getUsername();

    String getPassword();

    List<UserPermission> getPermissions();

}
