package smartcast.abj.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum TransactionType {
    DEBIT, CREDIT
}
