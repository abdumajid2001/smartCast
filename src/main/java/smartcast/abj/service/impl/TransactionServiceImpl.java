package smartcast.abj.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import smartcast.abj.dto.transactional.CreditDto;
import smartcast.abj.dto.transactional.DebitDto;
import smartcast.abj.dto.transactional.TransactionDto;
import smartcast.abj.dto.transactional.TransactionsDto;
import smartcast.abj.entity.Card;
import smartcast.abj.entity.Transaction;
import smartcast.abj.enums.CardStatus;
import smartcast.abj.enums.Currency;
import smartcast.abj.enums.TransactionType;
import smartcast.abj.exception.InsufficientFundsException;
import smartcast.abj.exception.InvalidDataException;
import smartcast.abj.exception.MissingFieldException;
import smartcast.abj.repository.TransactionRepository;
import smartcast.abj.service.CardService;
import smartcast.abj.service.TransactionService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static smartcast.abj.enums.TransactionType.CREDIT;
import static smartcast.abj.enums.TransactionType.DEBIT;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final CardService cardService;
    private final ObjectMapper mapper;

    @Override
    public ResponseEntity<TransactionDto> debit(String cardId, String idempotencyKey, DebitDto dto) {
        validateRequest(idempotencyKey, dto.externalId(), dto.amount(), new ArrayList<>() {{
            if (Objects.isNull(dto.purpose())) {
                add("purpose");
            }
        }});
        Optional<Transaction> transactionOptional = repository.findByIdempotencyKeyForDebit(idempotencyKey);

        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();

            if (!transaction.getCard().getId().equals(cardId)
                    || !transaction.getIdempotencyKey().equals(idempotencyKey)
                    || !transaction.getExternalId().equals(dto.externalId())
                    || !Objects.equals(transaction.getAmount(), dto.amount())
                    || !transaction.getCurrency().equals(dto.currency())
                    || !transaction.getPurpose().equals(dto.purpose())
            ) {
                throw new InvalidDataException("data provided is not compatible");
            }

            return ResponseEntity.ok(toDto(transaction));
        }
        Long exchangeRate = null;
        Card card = cardService.getById(cardId);

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card is not in ACTIVE status");
        }
        if (card.getCurrency().equals(dto.currency())) {
            if (card.getBalance() < dto.amount()) {
                throw new InsufficientFundsException();
            }
            card.setBalance(card.getBalance() - dto.amount());
        } else {
            exchangeRate = getExchangeRate();

            if (card.getCurrency() == Currency.UZS) {
                if (card.getBalance() < dto.amount() * exchangeRate) {
                    throw new InsufficientFundsException();
                }
                card.setBalance(card.getBalance() - dto.amount() * exchangeRate);
            } else {
                if (card.getBalance() * exchangeRate < dto.amount()) {
                    throw new InsufficientFundsException();
                }
                card.setBalance(card.getBalance() - dto.amount() / exchangeRate);
            }
        }
        cardService.saveCard(card);

        String id = UUID.randomUUID().toString();
        Transaction transaction = new Transaction(id, DEBIT, dto.externalId(), dto.amount(), dto.currency(), dto.purpose(), idempotencyKey, card, card.getBalance(), exchangeRate);
        Transaction savedTransaction = repository.save(transaction);

        return ResponseEntity.ok(toDto(savedTransaction));
    }

    @Override
    public ResponseEntity<TransactionDto> credit(String cardId, String idempotencyKey, CreditDto dto) {
        Transaction transaction;
        validateRequest(idempotencyKey, dto.externalId(), dto.amount(), new ArrayList<>());

        Optional<Transaction> transactionOptional = repository.findByIdempotencyKeyForCredit(idempotencyKey);

        if (transactionOptional.isPresent()) {
            transaction = transactionOptional.get();

            if (!transaction.getCard().getId().equals(cardId)
                    || !transaction.getIdempotencyKey().equals(idempotencyKey)
                    || !transaction.getExternalId().equals(dto.externalId())
                    || !Objects.equals(transaction.getAmount(), dto.amount())
                    || !transaction.getCurrency().equals(dto.currency())
            ) {
                throw new InvalidDataException("data provided is not compatible");
            }

            return ResponseEntity.ok(toDto(transaction));
        }
        Long exchangeRate = null;
        Card card = cardService.getById(cardId);

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card is not in ACTIVE status");
        }
        Long amountToCredit = dto.amount();

        if (dto.currency() != card.getCurrency()) {
            exchangeRate = getExchangeRate();

            if (card.getCurrency() == Currency.UZS) {
                amountToCredit = dto.amount() * exchangeRate;
            } else {
                amountToCredit = dto.amount() / exchangeRate;
            }
        }
        card.setBalance(card.getBalance() + amountToCredit);
        cardService.saveCard(card);

        String id = UUID.randomUUID().toString();
        Transaction newTransaction = new Transaction(id, CREDIT, dto.externalId(), dto.amount(), dto.currency(), idempotencyKey, card, card.getBalance(), exchangeRate);
        transaction = repository.save(newTransaction);

        return ResponseEntity.ok(toDto(transaction));
    }

    private void validateRequest(String idempotencyKey, String externalId, Long amount, ArrayList<String> requiredFields) {
        if (Objects.isNull(idempotencyKey) || idempotencyKey.isEmpty()) {
            requiredFields.add("idempotencyKey");
        }
        if (Objects.isNull(externalId) || externalId.isEmpty()) {
            requiredFields.add("external_id");
        }
        if (Objects.isNull(amount)) {
            requiredFields.add("amount");
        }

        if (!requiredFields.isEmpty()) {
            throw new MissingFieldException("Missing required field(s): " + String.join(", ", requiredFields));
        }

        if (amount <= 0) {
            throw new InvalidDataException("amount must be greater than zero");
        }
    }

    public Long getExchangeRate() {
        String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String url = "https://cbu.uz/uz/arkhiv-kursov-valyut/json/USD/" + now + "/";

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(new HttpGet(url))) {

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseString = EntityUtils.toString(entity);
                JsonNode jsonAuth = mapper.readTree(responseString);

                if (jsonAuth.isArray() && !jsonAuth.isEmpty()) {
                    JsonNode firstElement = jsonAuth.get(0);
                    if (firstElement.has("Rate")) {
                        String rateString = firstElement.get("Rate").asText();
                        return Long.parseLong(rateString.split("\\.")[0]); // Return the integer part of the rate as Long
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch the exchange rate", e);
        }
        throw new RuntimeException("Failed to fetch the exchange rate");
    }

    public TransactionDto toDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getExternalId(),
                transaction.getCard().getId(),
                transaction.getAmount(),
                transaction.getAfterBalance(),
                transaction.getCurrency(),
                transaction.getType(),
                transaction.getPurpose(),
                transaction.getExchangeRate()
        );
    }

    @Override
    public ResponseEntity<TransactionsDto> getTransactions(
            String cardId, TransactionType type, String transactionId, String externalId, Currency currency, int page, int size
    ) {
        int offset = page * size;
        String stringType = Objects.isNull(type) ? "" : type.name();
        String currencyString = Objects.isNull(currency) ? "" : currency.name();
        Optional<String> result
                = repository.getTransactions(cardId, stringType, transactionId, externalId, currencyString, size, offset);
        try {
            TransactionsDto transactionsDto = new TransactionsDto();

            if (result.isPresent()) {
                transactionsDto = mapper.readValue(result.get(), new TypeReference<>() {
                });
            }
            transactionsDto.setPage(page);
            transactionsDto.setSize(size);
            transactionsDto.setTotalPages((int) Math.ceil((double) transactionsDto.getTotalItems() / transactionsDto.getSize()));

            return ResponseEntity.ok(transactionsDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch transactions", e);
        }
    }

}
