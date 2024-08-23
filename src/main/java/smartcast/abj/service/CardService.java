package smartcast.abj.service;

import org.springframework.http.ResponseEntity;
import smartcast.abj.dto.card.CardDto;
import smartcast.abj.dto.card.CreateCardDto;
import smartcast.abj.entity.Card;

public interface CardService {

    ResponseEntity<CardDto> create(String idempotencyKey, CreateCardDto dto);

    ResponseEntity<CardDto> get(String cardId);

    ResponseEntity<Void> block(String eTag, String cardId);

    ResponseEntity<Void> unblock(String eTag, String cardId);

    Card getById(String id);

    void saveCard(Card card);

}
