package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    BigDecimal getBalance(String username);
    Account getAccountWithUserId(Long userId);
    Account getAccountWithAccountId(Long accountId);
}
