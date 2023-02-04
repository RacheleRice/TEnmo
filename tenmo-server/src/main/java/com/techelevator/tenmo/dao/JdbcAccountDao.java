package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao{


    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getBalance(String username) {
        BigDecimal balance = null;
        String sql = "SELECT balance FROM account JOIN tenmo_user ON tenmo_user.user_id = account.user_id " +
                "WHERE tenmo_user.username = ?;";
        SqlRowSet results= jdbcTemplate.queryForRowSet(sql, username);
        if (results.next()) {
            balance = results.getBigDecimal("balance");
        }
        return balance;
    }

    @Override
    public Account getAccountWithAccountId(Long accountId) {
        Account account = new Account();
        String sql = "SELECT account_id, user_id, balance FROM account " +
                "WHERE account_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);

        if (results.next()){
            account = mapRowToAccount(results);
        }
        return account;
    }


    @Override
    public Account getAccountWithUserId(Long userId) {
        Account account = new Account();
        String sql = "SELECT * FROM account " +
                "WHERE user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);

        if (results.next()){
            account = mapRowToAccount(results);
        }
        return account;
    }

    private Account mapRowToAccount(SqlRowSet results) {

        Account account = new Account();
        account.setAccountId(results.getInt("account_id"));
        account.setUserId(results.getInt("user_id"));
        BigDecimal balance = results.getBigDecimal("balance");
        account.setBalance(balance);
        return account;
    }


}
