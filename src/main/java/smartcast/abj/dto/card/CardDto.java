package smartcast.abj.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import smartcast.abj.enums.CardStatus;
import smartcast.abj.enums.Currency;

public record CardDto(
        @JsonProperty("card_id") String cardId,
        CardStatus status,
        long balance,
        Currency currency,
        @JsonProperty("user_id") Long userId
) {
}
