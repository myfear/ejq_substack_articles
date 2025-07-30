package com.example;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.account.Account;
import com.example.customer.Customer;
import com.example.customer.TransactionLog;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/bank")
public class BankResource {

    @Inject
    TransferService transferService;

    @POST
    @Path("/setup")
    @Transactional
    public Response setupData() {
        Customer alice = new Customer();
        alice.name = "Alice";
        alice.email = "alice@example.com";
        alice.phone = "555-0101";
        alice.persist();

        Customer bob = new Customer();
        bob.name = "Bob";
        bob.email = "bob@example.com";
        bob.phone = "555-0102";
        bob.persist();

        Account acc1 = new Account();
        acc1.customerId = alice.id;
        acc1.balance = new BigDecimal("1000.00");
        acc1.accountType = "CHECKING";
        acc1.persist();

        Account acc2 = new Account();
        acc2.customerId = bob.id;
        acc2.balance = new BigDecimal("500.00");
        acc2.accountType = "SAVINGS";
        acc2.persist();

        return Response.ok("Initial data created with enhanced fields!").build();
    }

    @POST
    @Path("/transfer")
    public Response transfer(@QueryParam("from") Long from, @QueryParam("to") Long to,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("fail") @DefaultValue("false") boolean simulateFailure) {
        try {
            transferService.transfer(from, to, amount, simulateFailure);
            return Response.ok("Transfer completed successfully!").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/status")
    public Response getStatus() {
        List<Account> accounts = Account.listAll();
        List<Customer> customers = Customer.listAll();
        List<TransactionLog> logs = TransactionLog.listAll();

        return Response.ok(Map.of(
                "accounts", accounts,
                "customers", customers,
                "transaction_logs", logs)).build();
    }

    @GET
    @Path("/accounts")
    public List<Account> listAccounts() {
        return Account.listAll();
    }

    @GET
    @Path("/customers")
    public List<Customer> listCustomers() {
        return Customer.listAll();
    }

    @GET
    @Path("/demo/success")
    public Response successfulTransfer() {
        try {
            transferService.transfer(1L, 2L, new BigDecimal("50.00"), false);
            return Response.ok("Successful transfer - both databases updated").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Transfer failed: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/demo/failure")
    public Response failedTransfer() {
        try {
            transferService.transfer(1L, 2L, new BigDecimal("50.00"), true);
            return Response.status(500).entity("This should not happen - transfer should have failed").build();
        } catch (Exception e) {
            return Response.ok("Failed transfer - both databases rolled back: " + e.getMessage()).build();
        }
    }

    // ===========================
    // SCENARIO 1: LOYALTY POINTS + ACCOUNT TRANSFER
    // ===========================
    @POST
    @Path("/transfer-loyalty")
    public Response transferWithLoyalty(@QueryParam("from") Long from, @QueryParam("to") Long to,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("fail") @DefaultValue("false") boolean simulateFailure) {
        try {
            transferService.transferWithLoyalty(from, to, amount, simulateFailure);
            return Response.ok("Transfer with loyalty points completed successfully!").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Transfer failed: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/demo/loyalty-success")
    public Response loyaltyTransferSuccess() {
        try {
            transferService.transferWithLoyalty(1L, 2L, new BigDecimal("100.00"), false);
            return Response.ok("Loyalty transfer success - check accounts, customers, and logs!").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Transfer failed: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/demo/loyalty-failure")
    public Response loyaltyTransferFailure() {
        try {
            transferService.transferWithLoyalty(1L, 2L, new BigDecimal("50.00"), true);
            return Response.status(500).entity("This should not happen").build();
        } catch (Exception e) {
            return Response.ok("Loyalty transfer failed - all changes rolled back: " + e.getMessage()).build();
        }
    }

    // ===========================
    // SCENARIO 2: ACCOUNT LIMITS + CUSTOMER STATUS
    // ===========================
    @POST
    @Path("/withdraw-limits")
    public Response withdrawWithLimits(@QueryParam("account") Long accountId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("fail") @DefaultValue("false") boolean simulateFailure) {
        try {
            transferService.transferWithLimits(accountId, amount, simulateFailure);
            return Response.ok("Withdrawal with limit check completed successfully!").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Withdrawal failed: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/demo/limits-success")
    public Response limitsWithdrawalSuccess() {
        try {
            transferService.transferWithLimits(1L, new BigDecimal("200.00"), false);
            return Response.ok("Limits withdrawal success - check account and customer status!").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Withdrawal failed: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/demo/limits-failure")
    public Response limitsWithdrawalFailure() {
        try {
            transferService.transferWithLimits(1L, new BigDecimal("100.00"), true);
            return Response.status(500).entity("This should not happen").build();
        } catch (Exception e) {
            return Response.ok("Limits withdrawal failed - all changes rolled back: " + e.getMessage()).build();
        }
    }

    // ===========================
    // SCENARIO 3: ACCOUNT FREEZE + CUSTOMER NOTIFICATION
    // ===========================
    @POST
    @Path("/freeze-account")
    public Response freezeAccount(@QueryParam("account") Long accountId,
            @QueryParam("reason") @DefaultValue("Suspicious activity") String reason,
            @QueryParam("fail") @DefaultValue("false") boolean simulateFailure) {
        try {
            transferService.freezeAccountWithNotification(accountId, reason, simulateFailure);
            return Response.ok("Account frozen and customer notified successfully!").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Freeze operation failed: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/demo/freeze-success")
    public Response freezeAccountSuccess() {
        try {
            transferService.freezeAccountWithNotification(2L, "Security review", false);
            return Response.ok("Account freeze success - check account/customer status and logs!").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Freeze failed: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/demo/freeze-failure")
    public Response freezeAccountFailure() {
        try {
            transferService.freezeAccountWithNotification(1L, "Test freeze", true);
            return Response.status(500).entity("This should not happen").build();
        } catch (Exception e) {
            return Response.ok("Account freeze failed - all changes rolled back: " + e.getMessage()).build();
        }
    }

    // ===========================
    // ADDITIONAL ENDPOINTS
    // ===========================
    @GET
    @Path("/logs")
    public List<TransactionLog> listTransactionLogs() {
        return TransactionLog.listAll();
    }

    @GET
    @Path("/reset-limits")
    @Transactional
    public Response resetDailyLimits() {
        List<Account> accounts = Account.listAll();
        for (Account account : accounts) {
            account.availableLimit = account.dailyLimit;
            account.persist();
        }
        return Response.ok("Daily limits reset for all accounts").build();
    }
}