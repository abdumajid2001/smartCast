package smartcast.abj.dto.transactional;

import com.fasterxml.jackson.annotation.JsonProperty;
import smartcast.abj.enums.Currency;
import smartcast.abj.enums.Purpose;
import smartcast.abj.enums.TransactionType;

public record TransactionDto(
        @JsonProperty("transaction_id") String transactionId,
        @JsonProperty("external_id") String externalId,
        @JsonProperty("card_id") String cardId,
        Long amount,
        @JsonProperty("after_balance") Long afterBalance,
        Currency currency,
        TransactionType type,
        Purpose purpose,
        @JsonProperty("exchange_rate") Long exchangeRate // Nullable
) {
}
