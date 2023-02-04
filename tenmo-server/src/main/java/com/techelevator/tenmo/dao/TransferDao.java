package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {

    void request (Transfer transfer);
    void transfer (Transfer transfer);
    Transfer getTransferByID(Long transferId);
    List<Transfer> getTransferByAccount(Long accountId);
    List<Transfer> getPendingRequestByAccount(Long accountId);
    void pendingTransfer(Transfer transfer);
    void pendingReject(Transfer transfer);



}
