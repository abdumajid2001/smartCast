package smartcast.abj.service;

import org.springframework.http.ResponseEntity;
import smartcast.abj.dto.transactional.CreditDto;
import smartcast.abj.dto.transactional.DebitDto;
import smartcast.abj.dto.transactional.TransactionDto;
import smartcast.abj.dto.transactional.TransactionsDto;
import smartcast.abj.enums.Currency;
import smartcast.abj.enums.TransactionType;

public interface TransactionService {

    ResponseEntity<TransactionDto> debit(String cardId, String idempotencyKey, DebitDto dto);

    ResponseEntity<TransactionDto> credit(String cardId, String idempotencyKey, CreditDto dto);

    ResponseEntity<TransactionsDto> getTransactions(String cardId, TransactionType type, String transactionId, String externalId, Currency currency, int page, int size);

}
