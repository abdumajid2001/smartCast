package smartcast.abj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smartcast.abj.entity.Transaction;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("SELECT t FROM Transaction t WHERE t.type = 'DEBIT' AND t.idempotencyKey = :idempotencyKey")
    Optional<Transaction> findByIdempotencyKeyForDebit(String idempotencyKey);

    @Query("SELECT t FROM Transaction t WHERE t.type = 'CREDIT' AND t.idempotencyKey = :idempotencyKey")
    Optional<Transaction> findByIdempotencyKeyForCredit(String idempotencyKey);

    @Query(value = """
            WITH all_transaction AS (
                SELECT t.id AS transaction_id,
                       t.external_id,
                       t.card_id,
                       t.amount,
                       t.after_balance,
                       t.currency,
                       t.type,
                       t.purpose,
                       t.exchange_rate
                FROM transactions t
                WHERE t.card_id = :cardId
                  AND (:type = '' OR t.type = :type)
                  AND (:transactionId IS NULL OR t.id = :transactionId)
                  AND (:externalId IS NULL OR t.external_id = :externalId)
                  AND (:currency = '' OR t.currency = :currency)
            ),
                 paged_transactions AS (
                     SELECT *
                     FROM all_transaction
                     LIMIT :size OFFSET :offset
                 )
            SELECT CAST(
                           json_build_object(
                                   'total_items', (SELECT COUNT(*) FROM all_transaction),
                                   'content', json_agg(json_build_object(
                                   'transaction_id', transaction_id,
                                   'external_id', external_id,
                                   'card_id', card_id,
                                   'amount', amount,
                                   'after_balance', after_balance,
                                   'currency', currency,
                                   'type', type,
                                   'purpose', purpose,
                                   'exchange_rate', exchange_rate
                                                       ))
                           ) AS json
                   )
            FROM paged_transactions""", nativeQuery = true)
    Optional<String> getTransactions(
            @Param("cardId") String cardId,
            @Param("type") String type,
            @Param("transactionId") String transactionId,
            @Param("externalId") String externalId,
            @Param("currency") String currency,
            @Param("size") int size,
            @Param("offset") int offset
    );

}
