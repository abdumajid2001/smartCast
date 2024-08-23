package smartcast.abj.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import smartcast.abj.dto.card.CardDto;
import smartcast.abj.dto.card.CreateCardDto;
import smartcast.abj.entity.Card;
import smartcast.abj.entity.IdempotencyKey;
import smartcast.abj.entity.User;
import smartcast.abj.enums.CardStatus;
import smartcast.abj.exception.*;
import smartcast.abj.repository.CardRepository;
import smartcast.abj.repository.IdempotencyKeyRepository;
import smartcast.abj.service.AuthenticationService;
import smartcast.abj.service.CardService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository repository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<CardDto> create(String idempotencyKey, CreateCardDto dto) {
        validCreate(idempotencyKey, dto);
        Optional<IdempotencyKey> idempotencyKeyOptional = idempotencyKeyRepository.findById(idempotencyKey);

        if (idempotencyKeyOptional.isPresent()) {
            Card card = idempotencyKeyOptional.get().getCard();

            if (Objects.isNull(card)) {
                throw new InvalidDataException("idempotencyKey is invalid");
            }

            return new ResponseEntity<>(toDto(card), HttpStatus.OK);
        }
        Card card = toEntity(dto);
        Card savedCard = repository.save(card);

        IdempotencyKey idempotency = new IdempotencyKey(idempotencyKey, savedCard);
        idempotencyKeyRepository.save(idempotency);

        return new ResponseEntity<>(toDto(savedCard), HttpStatus.CREATED);
    }

    private void validCreate(String idempotencyKey, CreateCardDto dto) {
        List<String> missingFields = new ArrayList<>();

        if (Objects.isNull(idempotencyKey) || idempotencyKey.isEmpty()) {
            missingFields.add("idempotencyKey");
        }
        if (Objects.isNull(dto.userId())) {
            missingFields.add("user_id");
        }
        if (!missingFields.isEmpty()) {
            throw new MissingFieldException("Missing required field(s): " + String.join(", ", missingFields));
        }

        if (dto.initialAmount() > 10000) {
            throw new InvalidDataException("Initial amount cannot exceed 10000 unit.");
        }
        Long existCardCount = repository.getExistCardCount(dto.userId());

        if (existCardCount >= 3) {
            throw new LimitExceededException("Your card amount is exceeded");
        }
    }

    @Override
    public ResponseEntity<CardDto> get(String cardId) {
        Card card = getById(cardId);

        return ResponseEntity
                .ok()
                .header("ETag", card.generateETag())
                .body(toDto(card));
    }


    @Override
    public ResponseEntity<Void> block(String eTag, String cardId) {
        Card card = getById(cardId);

        if (isEtagValid(card, eTag)) {
            throw new ETagMismatchException();
        }
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card is not in ACTIVE status");
        }
        card.setStatus(CardStatus.BLOCKED);
        repository.save(card);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> unblock(String eTag, String cardId) {
        Card card = getById(cardId);

        if (isEtagValid(card, eTag)) {
            throw new ETagMismatchException();
        }
        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card is not in BLOCKED status");
        }
        card.setStatus(CardStatus.ACTIVE);
        repository.save(card);

        return ResponseEntity.noContent().build();
    }

    public boolean isEtagValid(Card card, String eTag) {
        String currentETag = card.generateETag();
        return !currentETag.equals(eTag);
    }

    public Card getById(String id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Not found card cardId: " + id));
    }

    public CardDto toDto(Card card) {
        return new CardDto(card.getId(), card.getStatus(), card.getBalance(), card.getCurrency(), card.getUser().getId());
    }

    public Card toEntity(CreateCardDto dto) {
        String id = UUID.randomUUID().toString();
        User user = authenticationService.findById(dto.userId());

        return new Card(id, dto.initialAmount(), dto.currency(), user, dto.status());
    }

    @Override
    public void saveCard(Card card) {
        repository.save(card);
    }

}
