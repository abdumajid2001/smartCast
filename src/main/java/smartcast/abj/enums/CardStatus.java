package smartcast.abj.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum CardStatus {
    ACTIVE, BLOCKED, CLOSED
}
