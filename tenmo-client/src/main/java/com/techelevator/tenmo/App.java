package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TenmoServices;
import com.techelevator.tenmo.services.UserServices;

import java.math.BigDecimal;


public class App {

    public enum TransferType{
        Request,
        Send
    }

    public enum TransferStatus{
        Pending,
        Approved,
        Rejected
    }


    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final UserServices userService = new UserServices(API_BASE_URL);
    private final TenmoServices tenmoServices = new TenmoServices();


    private AuthenticatedUser currentUser;


    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        } else {
            userService.setAuthToken(currentUser.getToken());
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        // TODO Auto-generated method stub
        BigDecimal balance = tenmoServices.getBalance(currentUser);
        System.out.println("Your current account balance is: $" + balance);

    }

    private void viewTransferHistory() {
        // TODO Auto-generated method stub
        Account userAccount = tenmoServices.getAccountByUserId(currentUser, currentUser.getUser().getId());
        int userAccountId = userAccount.getAccountId();
        Transfer[] transfers = tenmoServices.getTransferHistory(currentUser, userAccountId);

        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.printf("%-22s%-22s%-22s\n","ID"," From/To  ","Amount");
        System.out.println("--------------------------------------------------");

        if (transfers != null) {
            for (Transfer transfer : transfers) {
                if (transfer.getAccountFrom() == userAccountId) {
                    String username = tenmoServices.getUserByUserId(currentUser,
                            tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountTo()).getUserId()).getUsername();
                    String to = "To:";
                    System.out.printf("%-22d%-22s%-22s\n",transfer.getTransferId(),to + " " +username,"$" + transfer.getAmount());


                }
                if (transfer.getAccountTo() == userAccountId) {
                    String username = tenmoServices.getUserByUserId(currentUser,
                            tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountFrom()).getUserId()).getUsername();
                    String from = "From: ";
                    System.out.printf("%-22d%-22s%-22s\n",transfer.getTransferId(),from + username,"$" + transfer.getAmount());


                }
            }

            int userInput = consoleService.promptForInt("Enter the ID of the transfer you'd like to view: (enter 0 to cancel): ");
            while (userInput != 0) {

                Transfer transfer = tenmoServices.getTransferById(currentUser, userInput);
                if (transfer != null) {
                    printTransferDetails(transfer);
                    break;
                } else {
                    userInput = consoleService.promptForInt("Invalid ID\n " +
                            "Please enter the VALID ID of the transfer you'd like to view: (enter 0 to cancel): ");
                }
            }
        } else {
            System.out.println("No Transfer History");
        }


    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub
        Account userAccount = tenmoServices.getAccountByUserId(currentUser, currentUser.getUser().getId());
        int userAccountId = userAccount.getAccountId();
        Transfer[] pendingTransfers = tenmoServices.getPendingTransfers(currentUser,userAccountId);
        int userInput = -1;
        if (pendingTransfers.length <= 0){
                System.out.println("\nNo Pending Transfer History.");
            }

        while (userInput != 0 && tenmoServices.getPendingTransfers(currentUser,userAccountId).length > 0){
            System.out.println("-------------------------------------------\n" +
                    "Pending Transfers\n" +
                    "ID          To                     Amount\n" +
                    "-------------------------------------------");
            for (Transfer pending : pendingTransfers) {

                String userName = tenmoServices.getUserByUserId(currentUser,
                        tenmoServices.getAccountByAccountId(currentUser, pending.getAccountTo()).getUserId()).getUsername();


                System.out.printf("%-22d%-22s%-22s\n",pending.getTransferId(),userName,pending.getAmount());
            }
                userInput = consoleService.promptForInt("\n---------\n" +
                        "Please enter transfer ID to approve/reject (0 to cancel): ");
                Transfer transfer = tenmoServices.getTransferById(currentUser, userInput);
                if (transfer != null) {
                    printTransferDetails(transfer);
                    System.out.println("\n1: Approve\n" +
                                         "2: Reject\n" +
                                         "0: Don't approve or reject\n" +
                                         "---------");
                    userInput = consoleService.promptForInt("Please choose an option: ");
                    while (userInput != 0) {
                        if (userInput == 1) {
                            if (tenmoServices.getBalance(currentUser).compareTo(transfer.getAmount()) >= 0) {
                                tenmoServices.pendingTransfer(currentUser, transfer);
                                System.out.println("Transfer Approved!");
                            }else{
                                System.out.println("You're balance is too low.");
                            }
                            break;

                        } else if (userInput == 2){
                            tenmoServices.pendingReject(currentUser, transfer);
                            System.out.println("Transfer Rejected!");
                            break;

                        } else {
                            System.out.println("Invalid Option. Please Choose Valid Option Below\n" +
                                    "\n1: Approve\n" +
                                    "2: Reject\n" +
                                    "0: Don't approve or reject\n" +
                                    "---------");
                            userInput = consoleService.promptForInt("Please choose an option: ");

                        }
                    }
                } else {
                    System.out.println("Invalid Transfer Id\n");
                }
            pendingTransfers = tenmoServices.getPendingTransfers(currentUser,userAccountId);
            }
    }

    private void sendBucks() {
        // TODO Auto-generated method stub

        User[] users = tenmoServices.getAllUsers(currentUser);
        printUsers(users);
        int userInput = consoleService.promptForInt("Enter the ID of the user you want to send money to (enter 0 to cancel): ");

        while (userInput != 0) {
            User user = tenmoServices.getUserByUserId(currentUser, userInput);
            if (user.getId() == null || user.getId().equals(currentUser.getUser().getId())) {
                System.out.println("\nInvalid User ID\n");
                printUsers(users);
                userInput = consoleService.promptForInt("Enter the ID of the user you want to send money to (enter 0 to cancel): ");
            } else {
                BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter amount to send: ");
                while (transferAmount.compareTo(BigDecimal.valueOf(0.01)) < 0 ||
                        transferAmount.compareTo(tenmoServices.getBalance(currentUser)) > 0) {
                    if (transferAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
                        transferAmount = consoleService.promptForBigDecimal(
                                "\nTransfer amount must be at least $0.01." +
                                        "\nEnter amount to transfer: ");
                    } else if (transferAmount.compareTo(tenmoServices.getBalance(currentUser)) > 0) {
                        transferAmount = consoleService.promptForBigDecimal(
                                "\nTransfer amount cannot be more than user balance." +
                                        "\nEnter amount to transfer: ");
                    }
                }
                Transfer transfer = createTransfer(2, 2, userInput, transferAmount);
                tenmoServices.transfer(currentUser, transfer);

                BigDecimal newBalance = tenmoServices.getBalance(currentUser);
                System.out.println("Successfully sent: $" + transferAmount);
                System.out.println("Your new balance is: $" + newBalance);
                break;

            }
        }



            }


    private void requestBucks() {
        // TODO Auto-generated method stub
        User[] users = tenmoServices.getAllUsers(currentUser);
        printUsers(users);

        int userInput = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel): ");

        while (userInput != 0) {
            User user = tenmoServices.getUserByUserId(currentUser, userInput);
            if (user.getId() == null || user.getId().equals(currentUser.getUser().getId())) {
                System.out.println("\nInvalid User ID\n");
                printUsers(users);
                userInput = consoleService.promptForInt("Enter the ID of the user you want to send money to (enter 0 to cancel): ");
            } else {
                BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter request amount: ");
                while (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    transferAmount = consoleService.promptForBigDecimal("Request amount must be greater than zero. Enter request amount: ");
                }

                Transfer transfer = createTransfer(1, 1, userInput, transferAmount);
                tenmoServices.request(currentUser, transfer);

                System.out.println("Request sent successfully");
                break;
            }
        }
    }


    private Transfer createTransfer(int status, int type, int UserId, BigDecimal transferAmount) {
        Transfer transfer = new Transfer();
        transfer.setTransferStatusId(status);
        transfer.setTransferTypeId(type);

        Account fromAccount = tenmoServices.getAccountByUserId(currentUser, UserId);
        Account toAccount = tenmoServices.getAccountByUserId(currentUser, (currentUser.getUser().getId()));

        if (type == 1) {
            fromAccount = tenmoServices.getAccountByUserId(currentUser, UserId);
            toAccount = tenmoServices.getAccountByUserId(currentUser, (currentUser.getUser().getId()));
        }

        if (type == 2) {
            fromAccount = tenmoServices.getAccountByUserId(currentUser, (currentUser.getUser().getId()));
            toAccount = tenmoServices.getAccountByUserId(currentUser, UserId);
        }
        transfer.setAccountFrom(fromAccount.getAccountId());
        transfer.setAccountTo(toAccount.getAccountId());
        transfer.setAmount(transferAmount);
        return transfer;
    }


    private void printUsers(User[] users) {
        System.out.println("-------------------------------------------\n" +
                "Users\n" +
                "ID          Name\n" +
                "-------------------------------------------");

        for (User user : users) {
            if (user.getUsername().equals(currentUser.getUser().getUsername())) continue;

            System.out.println(user.getId() + "\t\t" + user.getUsername());
        }
        System.out.println("-------------------------------------------");
    }

    private void printTransferDetails(Transfer transfer){
        System.out.println("--------------------------------------------\n" +
                "Transfer Details\n" +
                "--------------------------------------------");

        String accountToUsername = tenmoServices.getUserByUserId(currentUser,
                tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountTo()).getUserId()).getUsername();

        String accountFromUsername = tenmoServices.getUserByUserId(currentUser,
                tenmoServices.getAccountByAccountId(currentUser, transfer.getAccountFrom()).getUserId()).getUsername();

        System.out.println("Id: " + transfer.getTransferId());
        System.out.println("From: " + accountFromUsername);
        System.out.println("To: " + accountToUsername);
        System.out.println("Type: " + getTransferType(transfer.getTransferTypeId()));
        System.out.println("Status: " + getTransferStatus(transfer.getTransferStatusId()));
        System.out.println("Amount: " + transfer.getAmount());
    }


    public TransferType getTransferType(int type){
        return (type == 1)? TransferType.Request : TransferType.Send;
    }

    public TransferStatus getTransferStatus(int status){
        if(status == 1) return TransferStatus.Pending;
        else if (status ==2) return TransferStatus.Approved;
        else return TransferStatus.Rejected;
    }

}
