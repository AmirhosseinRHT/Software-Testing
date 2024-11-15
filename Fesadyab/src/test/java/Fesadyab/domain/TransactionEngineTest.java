package Fesadyab.domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TransactionEngineTest {

    TransactionEngine transactionEngine;
    Transaction transaction1 , transaction2 , transaction3 , transaction4;

    public Transaction createTransaction(int accountId, int transactionId,
                                         int amount, boolean debit) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);
        transaction.setDebit(debit);
        return transaction;
    }

    @BeforeEach
    void setup() {
        transactionEngine = new TransactionEngine();

        transaction1 = createTransaction(1, 101, 1, true);
        transaction2 = createTransaction(1, 102, 1, false);
        transaction3 = createTransaction(2, 201, 4, true);
        transaction4 = createTransaction(3, 202, 120, false);

        transactionEngine.addTransactionAndDetectFraud(transaction1);
        transactionEngine.addTransactionAndDetectFraud(transaction2);
        transactionEngine.addTransactionAndDetectFraud(transaction3);
        transactionEngine.addTransactionAndDetectFraud(transaction4);
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

    @Test
    void testAddRedundantTransaction() {
        Transaction t1 = createTransaction(20, 101, 200, true);
        Transaction t2 = createTransaction(10, 101, 100, false);
        transactionEngine.addTransactionAndDetectFraud(t1);
        int result = transactionEngine.addTransactionAndDetectFraud(t2);
        assertEquals(0, result);
    }

    @Test
    void testGetAverageTransactionAmountByAccountAsExpected(){
        Transaction transactionWithSameAccountId = createTransaction(transaction1.getAccountId(),
            500, 10000, false);
        int result = transactionEngine.addTransactionAndDetectFraud(transactionWithSameAccountId);
        assertEquals(0 , result);
    }

    @Test
    void testAddNewTransactionWithNewAccountId() {
        Transaction transactionWithNewAccountId = createTransaction(20, 101, 200, true);
        int result = transactionEngine.addTransactionAndDetectFraud(transactionWithNewAccountId);
        assertEquals(0,result);
    }


}
