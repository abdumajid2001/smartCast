package smartcast.abj.dto.transactional;

import com.fasterxml.jackson.annotation.JsonProperty;
import smartcast.abj.enums.Currency;
import smartcast.abj.enums.Purpose;

public record DebitDto(
        @JsonProperty("external_id") String externalId,
        Long amount,
        Currency currency,
        Purpose purpose
) {

    public DebitDto {
        if (currency == null) {
            currency = Currency.UZS;
        }
    }

}

