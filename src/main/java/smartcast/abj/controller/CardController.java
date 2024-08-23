package smartcast.abj.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import smartcast.abj.dto.card.CardDto;
import smartcast.abj.dto.card.CreateCardDto;
import smartcast.abj.service.CardService;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREATE')")
    public ResponseEntity<CardDto> create(
            @RequestHeader(value = "Idempotency-Key", defaultValue = "") String idempotencyKey,
            @RequestBody CreateCardDto dto) {
        return service.create(idempotencyKey, dto);
    }

    @GetMapping("{cardId}")
    @PreAuthorize("hasAnyAuthority('GET')")
    public ResponseEntity<CardDto> get(@PathVariable("cardId") String cardId) {
        return service.get(cardId);
    }

    @PostMapping("{cardId}/block")
    @PreAuthorize("hasAnyAuthority('BLOCK')")
    public ResponseEntity<Void> block(@RequestHeader("If-Match") String eTag,
                                      @PathVariable("cardId") String cardId) {
        return service.block(eTag, cardId);
    }

    @PostMapping("{cardId}/unblock")
    @PreAuthorize("hasAnyAuthority('UNBLOCK')")
    public ResponseEntity<Void> unblock(@RequestHeader("If-Match") String eTag,
                                        @PathVariable("cardId") String cardId) {
        return service.unblock(eTag, cardId);
    }


}
