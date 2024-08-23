package smartcast.abj.dto.transactional;

import com.fasterxml.jackson.annotation.JsonProperty;
import smartcast.abj.enums.Currency;

public record CreditDto(
        @JsonProperty("external_id") String externalId,
        Long amount,
        Currency currency
) {

    public CreditDto {
        if (currency == null) {
            currency = Currency.UZS;
        }
    }

}
