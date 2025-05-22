package com.example;

import java.math.BigDecimal;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class AccountService {

    @Inject
    AuditService auditService;

    @Transactional
    public Account createAccount(@Valid Account account) {
        account.persist();
        return account;
    }

    public Account getAccount(Long id) {
        return Account.findById(id);
    }

    public List<Account> getAllAccounts() {
        return Account.listAll();
    }

    @Transactional
    public Account deposit(Long id, BigDecimal amount) {
        Account account = Account.findById(id);
        if (account == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid deposit.");
        }
        account.balance = account.balance.add(amount);
        return account;
    }

    @Transactional
    public Account withdraw(Long id, BigDecimal amount) {
        Account account = Account.findById(id);
        if (account == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid withdrawal.");
        }
        if (account.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds.");
        }
        account.balance = account.balance.subtract(amount);
        return account;
    }

    @Transactional
    public void transferFunds(Long fromId, Long toId, BigDecimal amount) {
        auditService.logAuditEventInSameTransaction("Transfer attempt from " + fromId + " to " + toId);
        auditService.logAuditEvent("TRANSFER_OPERATION_STARTED");

        Account from = Account.findById(fromId);
        Account to = Account.findById(toId);

        if (from == null || to == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid transfer.");
        }

        if (from.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds.");
        }

        from.balance = from.balance.subtract(amount);

        // Simulate failure
        // throw new RuntimeException("Simulated failure!");

        to.balance = to.balance.add(amount);
    }
}
