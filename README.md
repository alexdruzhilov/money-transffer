# Money transfer

Design and implement a RESTful API (including data model and the backing implementation) for money transfers between accounts.

Build

```bash
./gradlew build
```

Run application (by default starts on 8080 port)

```bash
./gradlew run
```

Run tests

```bash
./gradlew test
```

### Technologies

* Java 11
* Gradle
* Micronaut
* H2 database
* Mybatis

### Limitations

The only one currency available: USD

## API

### GET /account/{id}

Get account data

Response:
```json
{
  "id": "4cb107f4-f053-4bfe-8e57-9695b773fcce",
  "name": "Alice USD account",
  "currency": "USD",
  "balance": 1000
}
```

### POST /account

Create new account

Request data:
```json
{
   "name": "Alice USD account",
   "currency": "USD" 
}
```

Response:
```json
{
  "id": "4cb107f4-f053-4bfe-8e57-9695b773fcce",
  "name": "Alice USD account",
  "currency": "USD"
}
```

### POST /account/{id}/deposit

Deposit money on account balance
Amount should be of type Long

Request:
```json
{
  "amount": 100,
  "currency": "USD" 
}
```

### POST /account/{id}/withdraw

Withdraw money from account balance
Amount should be of type Long

Sample request:
```json
{
  "amount": 100,
  "currency": "USD" 
}
```

### POST /account/{sourceAccountId}/transfer/{targetAccountId}

Transfer money from source to target account
Amount should be of type Long

Sample request:
```json
{
  "amount": 100,
  "currency": "USD" 
}
```
