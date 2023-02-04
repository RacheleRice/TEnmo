package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {


    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transfer> getTransferByAccount(Long accountId) {
        List<Transfer> transferList = new ArrayList<>();
        String sql =
                "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                        " FROM transfer " +
                        " WHERE account_from = ? " +
                        " OR account_to = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
        while (results.next()) {
            transferList.add(mapRowToTransfer(results));
        }
        return transferList;
    }

    @Override
    public List<Transfer> getPendingRequestByAccount(Long accountId) {
        List<Transfer> pendingTransfers = new ArrayList<>();
        String sql =
                        "SELECT * " +
                        "FROM transfer " +
                        "WHERE account_from = ? " +
                        "AND transfer_status_id = 1";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
        while (results.next()) {
            pendingTransfers.add(mapRowToTransfer(results));
        }
        return pendingTransfers;
    }


    @Override
    public Transfer getTransferByID(Long transferId) {
        Transfer transfer = null;
        String sql = "SELECT * " +
                " FROM transfer " +
                " WHERE transfer_id = ?; ";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId);
        if (results.next()) {
            transfer = mapRowToTransfer(results);
        }
        return transfer;
    }

    ;

    @Override
    public void transfer(Transfer transfer) {
        boolean approved = transfer.getTransferStatusId() == 2;
        AccountDao accountDao = new JdbcAccountDao(jdbcTemplate);
        Long fromAccountId = transfer.getAccountFrom();
        Long toAccountId = transfer.getAccountTo();
        String sql;
        Account fromAccount = accountDao.getAccountWithAccountId((long) fromAccountId);
        Account toAccount = accountDao.getAccountWithAccountId((long) toAccountId);
        BigDecimal fromAccountBalance = fromAccount.getBalance();
        BigDecimal toAccountBalance = toAccount.getBalance();
        BigDecimal transferAmount = transfer.getAmount();
        if (approved) {
            fromAccountBalance = fromAccountBalance.subtract(transferAmount);
            toAccountBalance = toAccountBalance.add(transferAmount);
        }
            sql = "START TRANSACTION; " +
                    "UPDATE account " +
                    "SET balance = ? " +
                    "WHERE account_id = ?; " +
                    "UPDATE account " +
                    "SET balance = ? " +
                    "WHERE account_id = ?; " +
                    "INSERT INTO transfer (transfer_type_id, transfer_status_id,account_from, account_to, amount) " +
                    " VALUES (?, ?, ?, ?, ?);" +
                    "COMMIT;";
            jdbcTemplate.update(sql, fromAccountBalance, fromAccountId, toAccountBalance, toAccountId,
                    transfer.getTransferTypeId(), transfer.getTransferStatusId(), fromAccountId, toAccountId, transferAmount);



    }
    @Override
    public void pendingTransfer(Transfer transfer) {
        AccountDao accountDao = new JdbcAccountDao(jdbcTemplate);
        Long fromAccountId = transfer.getAccountFrom();
        Long toAccountId = transfer.getAccountTo();
        String sql;
        Account fromAccount = accountDao.getAccountWithAccountId(fromAccountId);
        Account toAccount = accountDao.getAccountWithAccountId(toAccountId);
        BigDecimal fromAccountBalance = fromAccount.getBalance();
        BigDecimal toAccountBalance = toAccount.getBalance();
        BigDecimal transferAmount = transfer.getAmount();
        fromAccountBalance = fromAccountBalance.subtract(transferAmount);
        toAccountBalance = toAccountBalance.add(transferAmount);
        sql = "START TRANSACTION; " +
                "UPDATE account " +
                "SET balance = ? " +
                "WHERE account_id = ?; " +
                "UPDATE account " +
                "SET balance = ? " +
                "WHERE account_id = ?; " +
                "UPDATE transfer " +
                "SET transfer_status_id = 2" +
                "WHERE transfer_id = ?;" +
                "COMMIT;";
        jdbcTemplate.update(sql, fromAccountBalance, fromAccountId, toAccountBalance, toAccountId,
                transfer.getTransferId());

        }

        public void pendingReject(Transfer transfer){
        String sql = "START TRANSACTION; " +
                "UPDATE transfer " +
                "SET transfer_status_id = 3" +
                "WHERE transfer_id = ?;" +
                "COMMIT;";
        jdbcTemplate.update(sql, transfer.getTransferId());
        }
        @Override
        public void request (Transfer transfer){
            Long transferTypeId = transfer.getTransferTypeId();
            Long transferStatusId = transfer.getTransferStatusId();

            AccountDao accountDao = new JdbcAccountDao(jdbcTemplate);
            Long fromAccountId = transfer.getAccountFrom();
            Long toAccountId = transfer.getAccountTo();
            Account fromAccount = accountDao.getAccountWithAccountId(fromAccountId);
            Account toAccount = accountDao.getAccountWithAccountId((toAccountId));
            fromAccountId = fromAccount.getAccountId();
            toAccountId = toAccount.getAccountId();

            BigDecimal requestAmount = transfer.getAmount();

            String sql = "START TRANSACTION; " +
                    "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    " VALUES (?, ?, ?, ?, ?);" +
                    "COMMIT;";

            jdbcTemplate.update(sql, transferTypeId, transferStatusId, fromAccountId, toAccountId, requestAmount);
        }



    private Transfer mapRowToTransfer(SqlRowSet results) {

        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getLong("transfer_id"));
        transfer.setTransferTypeId(results.getLong("transfer_type_id"));
        transfer.setTransferStatusId(results.getLong("transfer_status_id"));
        transfer.setAccountFrom(results.getLong("account_from"));
        transfer.setAccountTo(results.getLong("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        return transfer;
    }
}
