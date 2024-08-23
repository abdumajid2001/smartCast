package smartcast.abj.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import smartcast.abj.enums.UserPermission;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
@SQLRestriction("is_deleted = false")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private List<UserPermission> permissions;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.permissions = new ArrayList<>() {{
            add(UserPermission.CREATE);
            add(UserPermission.GET);
            add(UserPermission.BLOCK);
            add(UserPermission.UNBLOCK);
            add(UserPermission.DEBIT);
            add(UserPermission.CREDIT);
            add(UserPermission.HISTORY);
        }};
    }

}

