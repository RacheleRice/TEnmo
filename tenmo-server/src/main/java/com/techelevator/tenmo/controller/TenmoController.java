package com.techelevator.tenmo.controller;
import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(path = "/tenmo")
public class TenmoController {

    private UserDao userDao;
    private AccountDao accountDao;
    private TransferDao transferDao;


    public TenmoController(UserDao userDao, AccountDao accountDao, TransferDao transferDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;
        this.transferDao = transferDao;

    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal) {
        return accountDao.getBalance(principal.getName());
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<User> getAllUsers() {
        return userDao.findAll();
    }


    @RequestMapping(path = "/account/users/{id}", method = RequestMethod.GET)
    public Account getAccountByUserId(@PathVariable long id) {
        return accountDao.getAccountWithUserId(id);
    }

    @RequestMapping(path = "/account/{id}", method = RequestMethod.GET)
    public Account getAccountByAccountId(@PathVariable long id) {

        return accountDao.getAccountWithAccountId(id);
    }

    @RequestMapping(path = "/users/{id}", method = RequestMethod.GET)
    public User getUserByUserId(@PathVariable long id) {
        return userDao.findByUserID(id);
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public void transfer(@RequestBody Transfer transfer) {
        transferDao.transfer(transfer);
    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.GET)
    public Transfer viewTransferByID(@PathVariable("id") Long id){
        return transferDao.getTransferByID(id);
    }

    @RequestMapping(path = "/transfers/history/{id}", method = RequestMethod.GET)
    public List<Transfer> viewTransferHistory(@PathVariable("id") Long accountId){
        return transferDao.getTransferByAccount(accountId);
    }
    @RequestMapping(path = "/pending/{id}", method = RequestMethod.GET)
    public List<Transfer> getPendingTransfers(@PathVariable("id") Long accountId){
        return transferDao.getPendingRequestByAccount(accountId);
    }
    @RequestMapping(path = "/request", method = RequestMethod.POST)
    public void request(@RequestBody Transfer transfer) {
        transferDao.request(transfer);
    }
    @RequestMapping(path = "/request/approve", method = RequestMethod.PUT)
    public void pendingTransfer(@RequestBody Transfer transfer){
        transferDao.pendingTransfer(transfer);
    }
    @RequestMapping(path = "/request/reject", method = RequestMethod.PUT)
    public void pendingReject(@RequestBody Transfer transfer){
        transferDao.pendingReject(transfer);
    }


}
