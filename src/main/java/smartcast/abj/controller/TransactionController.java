package smartcast.abj.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartcast.abj.dto.transactional.CreditDto;
import smartcast.abj.dto.transactional.DebitDto;
import smartcast.abj.dto.transactional.TransactionDto;
import smartcast.abj.dto.transactional.TransactionsDto;
import smartcast.abj.enums.Currency;
import smartcast.abj.enums.TransactionType;
import smartcast.abj.service.TransactionService;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("{cardId}/debit")
    @PreAuthorize("hasAnyAuthority('DEBIT')")
    public ResponseEntity<TransactionDto> debit(
            @PathVariable("cardId") String cardId,
            @RequestHeader(value = "Idempotency-Key", defaultValue = "") String idempotencyKey,
            @RequestBody DebitDto dto
    ) {
        return service.debit(cardId, idempotencyKey, dto);
    }

    @PostMapping("{cardId}/credit")
    @PreAuthorize("hasAnyAuthority('CREDIT')")
    public ResponseEntity<TransactionDto> credit(
            @PathVariable("cardId") String cardId,
            @RequestHeader(value = "Idempotency-Key", defaultValue = "") String idempotencyKey,
            @RequestBody CreditDto dto
    ) {
        return service.credit(cardId, idempotencyKey, dto);
    }

    @GetMapping("{cardId}/transactions")
    @PreAuthorize("hasAnyAuthority('HISTORY')")
    public ResponseEntity<TransactionsDto> getTransactions(
            @PathVariable String cardId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) Currency currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.getTransactions(cardId, type, transactionId, externalId, currency, page, size);
    }

}
