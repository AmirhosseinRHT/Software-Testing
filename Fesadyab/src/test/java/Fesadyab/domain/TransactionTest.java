package Fesadyab.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TransactionTest {

    public static Transaction createTransaction(int accountId, int transactionId,
        int amount, boolean debit) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);
        transaction.setDebit(debit);
        return transaction;
    }

    @Test
    void testCompareTwoTransactionWithSameTransactionId() {
        Transaction t1 = createTransaction(1, 1234, 100, false);
        Transaction t2 = createTransaction(1, 1234, 100, false);
        boolean isEqual = t1.equals(t2);
        assertTrue(isEqual);
    }

    @Test
    void testCompareTwoTransactionWithDifferentTransactionId() {
        Transaction t1 = createTransaction(1, 1234, 100, false);
        Transaction t2 = createTransaction(1, 1235, 100, false);
        boolean isEqual = t1.equals(t2);
        assertFalse(isEqual);
    }

    @Test
    void testCompareTransactionWithAnotherObject() {
        Transaction t1 = createTransaction(1, 1234, 100, false);
        Object obj = new Object();
        boolean isEqual = t1.equals(obj);
        assertFalse(isEqual);
    }

}
