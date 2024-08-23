package smartcast.abj.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.DigestUtils;
import smartcast.abj.enums.CardStatus;
import smartcast.abj.enums.Currency;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "cards")
@SQLRestriction("is_deleted = false")
public class Card extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    private long balance;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    public String generateETag() {
        String data = String.join("-", id, String.valueOf(balance), status.name(), this.getUpdatedAt().toString());

        return DigestUtils.md5DigestAsHex(data.getBytes());
    }

}
