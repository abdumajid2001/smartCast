package smartcast.abj.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import smartcast.abj.enums.CardStatus;
import smartcast.abj.enums.Currency;

public record CreateCardDto(
        @JsonProperty("user_id") Long userId,
        CardStatus status,
        @JsonProperty("initial_amount") Long initialAmount,
        Currency currency
) {

    public CreateCardDto {
        if (status == null) {
            status = CardStatus.ACTIVE;
        }
        if (currency == null) {
            currency = Currency.UZS;
        }
        if (initialAmount == null) {
            initialAmount = 0L;
        }
    }

}

