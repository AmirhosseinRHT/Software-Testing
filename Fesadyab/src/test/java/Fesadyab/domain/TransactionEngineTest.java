package Fesadyab.domain;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionEngineTest
{

    TransactionEngine transactionEngine;
    Transaction transaction1 , transaction2 , transaction3 , transaction4 , transaction5;

    public static Transaction createTransaction(int accountId, int transactionId,
        int amount, boolean debit) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setTransactionId(transactionId);
        transaction.setAmount(amount);
        transaction.setDebit(debit);
        return transaction;
    }

    @BeforeAll
    public void createTestTransactions() {
        transaction1 = createTransaction(1, 101, 1, true);
        transaction2 = createTransaction(1, 102, 1, false);
        transaction3 = createTransaction(2, 201, 4, true);
        transaction4 = createTransaction(3, 202, 120, false);
        transaction5 = createTransaction(1, 500, 100, false);
    }

    @BeforeEach
    void setup() {
        transactionEngine = new TransactionEngine();
        transactionEngine.transactionHistory.addAll(List.of(transaction1, transaction2, transaction3, transaction4 , transaction5));
    }

    @Test
    void testAddRedundantTransaction() {
        int result = transactionEngine.addTransactionAndDetectFraud(transaction4);
        assertEquals(0, result);
    }

    @Test
    void testGetAverageTransactionAmountByAccount(){
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

    @Test
    void testAddNewCorruptTransaction(){
        Transaction corruptTransaction =  createTransaction(transaction1.getAccountId(),
            transaction4.getTransactionId() + 1,
             transaction1.getAmount() + transaction2.getAmount() + 1000, true);
        int result = transactionEngine.addTransactionAndDetectFraud(corruptTransaction);
        assertEquals(934,result);
    }

    @Test
    void testAddNewNormalTransaction(){
        Transaction t = createTransaction(transaction1.getAccountId(),
            transaction4.getTransactionId() + 1, 10, true);
        int result = transactionEngine.addTransactionAndDetectFraud(t);
        assertEquals(0,result);
    }

    @Test
    void testContinuousEqualDiffsInGetTransactionPatternAboveThreshold(){
        Transaction t1 = createTransaction(transaction4.getAccountId(), 1234 , 1500, false);
        Transaction t2 = createTransaction(transaction4.getAccountId(), 1235 , 2999, false);
        Transaction t3 = createTransaction(transaction4.getAccountId(), 1236 , 4498, false);
        Transaction t4 = createTransaction(transaction4.getAccountId(), 1237 , 5997, false);
        int result1 = transactionEngine.addTransactionAndDetectFraud(t1);
        int result2 = transactionEngine.addTransactionAndDetectFraud(t2);
        int result3 = transactionEngine.addTransactionAndDetectFraud(t3);
        int result4 = transactionEngine.addTransactionAndDetectFraud(t4);
        assertEquals(0,result1);
        assertEquals(1499,result2);
        assertEquals(1499,result3);
        assertEquals(0,result4);
    }
    @Test
    void testContinuousInEqualDiffsInGetTransactionPatternAboveThreshold(){
        Transaction t1 =  createTransaction(transaction4.getAccountId(), 1234 , 1500, false);
        Transaction t2 = createTransaction(transaction4.getAccountId(), 1235 , 3200, false);
        Transaction t3 = createTransaction(transaction4.getAccountId(), 1236 , 450, false);
        int result1 = transactionEngine.addTransactionAndDetectFraud(t1);
        int result2 = transactionEngine.addTransactionAndDetectFraud(t2);
        int result3 = transactionEngine.addTransactionAndDetectFraud(t3);
        assertEquals(0,result1);
        assertEquals(1499,result2);
        assertEquals(0,result3);
    }

    @Test
    void testAddNewTransactionWhenFraudIsNotZero(){
        Transaction t1 = createTransaction(transaction4.getAccountId() , 888, 1000, false);
        Transaction t2 = createTransaction(transaction4.getAccountId() , 999, 2000, false);
        Transaction t3 = createTransaction(transaction4.getAccountId() , 777, 1000, true);
        transactionEngine.transactionHistory.add(t1);
        transactionEngine.transactionHistory.add(t2);
        int result = transactionEngine.addTransactionAndDetectFraud(t3);
        assertEquals(1999,result);
    }

    @Test
    void testAddNewTransactionWhenTransactionHistoryIsEmpty(){
        transactionEngine = new TransactionEngine();
        int result = transactionEngine.addTransactionAndDetectFraud(transaction2);
        assertEquals(0,result);
    }

    @Test
    void testAddNewTransactionWithGreatAmountWhenDiffIsNotZero(){
        Transaction t1 = createTransaction(transaction2.getAccountId() , 888, 1000, false);
        Transaction t2 = createTransaction(transaction3.getAccountId() , 999, 2500, false);
        Transaction t3 = createTransaction(transaction2.getAccountId() , 777, 5000, true);
        transactionEngine.transactionHistory.add(t1);
        transactionEngine.transactionHistory.add(t2);
        transactionEngine.transactionHistory.add(t3);
        Transaction greaterAmountTransaction =  createTransaction(transaction2.getAccountId(),
            1234 , 10000, false);
        int result = transactionEngine.addTransactionAndDetectFraud(greaterAmountTransaction);
        assertEquals(0,result);
    }

    @Test
    void testTransactionAmountIsLessThanThresholdInGetTransactionPatternAboveThreshold(){
        Transaction lessThan = createTransaction(transaction4.getAccountId(), 1234 , 1, false);
        int result = transactionEngine.addTransactionAndDetectFraud(lessThan);
        assertEquals(0,result);
    }


}