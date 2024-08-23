package smartcast.abj.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import smartcast.abj.enums.Currency;
import smartcast.abj.enums.Purpose;
import smartcast.abj.enums.TransactionType;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "transactions")
@SQLRestriction("is_deleted = false")
public class Transaction extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String externalId;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private Purpose purpose;

    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    private Long afterBalance;

    private Long exchangeRate;

    public Transaction(String id,
                       TransactionType type,
                       String externalId,
                       Long amount,
                       Currency currency,
                       String idempotencyKey,
                       Card card,
                       Long afterBalance,
                       Long exchangeRate) {
        this.id = id;
        this.type = type;
        this.externalId = externalId;
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
        this.card = card;
        this.afterBalance = afterBalance;
        this.exchangeRate = exchangeRate;
    }
}
