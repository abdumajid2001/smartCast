package smartcast.abj.dto.transactional;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TransactionsDto {

    private int page;

    private int size;

    private int totalPages;

    @JsonProperty("total_items")
    private int totalItems;

    private List<TransactionDto> content;

}

