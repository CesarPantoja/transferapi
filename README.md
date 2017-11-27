#Transfer API

##Introduction
This is a RESTful API for account management and transferring money between accounts.

Build is managed using Maven. Some common goals:

`mvn test` to run tests

`mvn exec:java` to execute the application without packaging

`mvn package` to compile, run tests, and generate self-self contained jar. The generated jar contains all the dependencies
    and is thus portable. Just copy/move the jar.

##Implementation details
For the HTTP server, grizzly2 is being used, which is an embedded container.
It will run by default on the `localhost:8080/transferapi/`

###Controller

The main controller class is `com.example.transferapi.controller.BankController`. This class follows the singleton pattern
to keep only one instance in memory. The responsibilities of this class are of data storage (in this case in-memory
object) and business logic.

###model
The model classes are:

`com.example.transferapi.model.Account:` An account with a unique identifier, a name, a balance, and a status.

`com.example.transferapi.model.Transaction:` Any movement that changes the balance of accounts

`com.example.transferapi.model.Bank:` A collection of accounts and transactions

###REST resources
The RESTful end-points are implemented using the Jersey framework. The resource end-points are specified in the
`com.example.transferapi.resources` package. There are two paths: **/account** for operations with the accounts, and
**/transaction** for operations with the transactions.

All endpoints return json objects. The json representation of the models are:

Account:
```
{
    "balance": <double>,
    "name": <string>,
    "status": "ACTIVE" or "INACTIVE",
    "uuid": <integer>
}
```

e.g.:
```
{
    "uuid": 59,
    "name": "Elliot Alderson",
    "balance": 550,
    "status": "ACTIVE"
}
```

Transaction:
```
{
    "uuid": <integer>,
    "sourceAccount": <integer>,
    "targetAccount": <integer>,
    "amount": <double>,
    "message": <string>,
    "sourceAccountStartBalance": <double>,
    "sourceAccountEndBalance": <double>,
    "targetAccountStartBalance": <double>,;
    "targetAccountEndBalance": <double>
}
```

e.g.:
```
{
    "uuid": 2234,
    "sourceAccount": 553,
    "targetAccount": 154,
    "amount": 50,
    "message": "Night out",
    "sourceAccountStartBalance": 500,
    "sourceAccountEndBalance": 450,
    "targetAccountStartBalance": 934,
    "targetAccountEndBalance": 984
}
```

####/account endpoints

#####POST /account<br>
######Form params
- name (string - required)
- startBalance
######Description
This endpoint creates an account, and creates a transaction with the starting balance of the account.
######Responses
- **400 - Bad request** if the start balance is less than 0
- **201 - Created** if the account was created successfully. The payload will contain the created account and the
    headers will contain Location with the url of the created account

#####GET /account
######Description
Retrieves all the accounts in the system<br>
######Responses
- **200 - Ok** The payload will contain a list of accounts or empty if there are none.

#####GET /account/{id}
######Description
Retrieves the account with id {id}
######Responses
- **404 - Not found** if the account doesn't exist.
- **200 - Ok** and the body with the account if it exists.

#####PUT /account/{id}
######Form params
- name (string - required)
######Description
Updates the name of the account
######Responses
- **404 - Not found** if the account doesn't exist
- **410 - Gone** if the account is inactive
- **304 - Not modified** if the new name is the same as the old one
- **200 - Ok**, and the body will contain the account, if the operation completes

#####DELETE /account/{id}
######Description
Deactivates an account
######Responses
- **404 - Not found** if the account doesn't exist
- **410 - Gone** if the account is already inactive
- **200 - Ok** and the body with the account if the operation completes

####/transaction endpoints

#####GET /transaction
######Description
Gets a list of all the transactions
######Responses
- **200 - Ok**, and the body contains the list of transactions or empty if there are none.

#####GET /transaction/{id}
######Description
Retrieves the transaction with id {id}
######Responses
- **404 - Not found** if the transaction doesn't exists
- **200 - Ok** and the body contains the transaction if it exists

#####POST /transaction
######Form params
- sourceAccountId: (integer - required)
- targetAccountId: (integer - required)
- amount (integer - required)
- message (string - required)
######Description
Transfers money between accounts
######Responses
- **400 - Bad request** if the amount is not a positive amount or the source account doesn't have enough balance to
        complete the operation
- **404 - Not found** if any of the accounts does't exist
- **410 - Gone** if any of the accounts is inactive
- **201 - Created** if the transaction completed successfully. The body will contain the transfer details and the
    headers will contain Location with the url of the transaction

#####POST /transaction/deposit
######Form params
- targetAccountId: (integer - required)
- amount: (double - required)
######Description
Deposits money in an account
######Responses
- **400 - Bad request** if the amount is not a positive amount
- **404 - Not found** if the account does't exist
- **410 - Gone** if the account is inactive
- **201 - Created** if the deposit completed successfully. The body will contain the deposit details and the
    headers will contain Location with the url of the transaction. The transfer details will not have
    source account details

#####POST /transaction/withdraw
######Form params
- sourceAccountId: (integer - required)
- amount: (double - required)
######Description
Withdraws money from an account
######Responses
- **400 - Bad request** if the amount is not a positive amount or the source account doesn't have enough balance to
    complete the operation
- **404 - Not found** if the account does't exist
- **410 - Gone** if the account is inactive
- **201 - Created** if the with completed successfully. The body will contain the withdrawal details and the
    headers will contain Location with the url of the transaction. The transfer details will not have
    target account details

##Tests:
JUnit is used as the test framework. Jersey's tests wrapper is used to facilitate the tests.
There are two test classes, AccountTest, and TransactionTest, one for each REST resource exposed.
The tests will check for different expected outputs and results for all the implemented endpoints

The tests can be run independently running the `test` maven goal.