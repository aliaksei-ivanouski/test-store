# Test Store

## Description

Welcome to Test Store, a pancake shop software solution for ordering pancakes.

### Use case
1. Disciple can create an Order and specify the building and the room number (org.aivanouski.store.portal.DisciplePortalService.createOrder).
2. Disciple has access to the available ingredients (org.aivanouski.store.portal.DisciplePortalService.getIngredients).
3. After that to receive later the most delicious pancake the Disciple can add ingredients to the created Order from the menu in p.2 (org.aivanouski.store.portal.DisciplePortalService.addPancakes).
4. Disciple can choose to complete or cancel the Order, if cancelled the Order is removed from the database (org.aivanouski.store.portal.DisciplePortalService.completeOrder - pass status CANCELLED).
5. Disciple can complete the Order and the Order will be passed to Chef to prepare the pancakes (org.aivanouski.store.portal.DisciplePortalService.completeOrder - pass status COMPLETED).
6. Order usually preparing for 30 seconds (yeah, that's fast, because we have highly experienced Chef) (org.aivanouski.store.order.OrderDeliveryJob.checkOrdersAndDeliver).
7. Our delivery guys checking every 10 second whether any Orders ready to delivery and report if any (org.aivanouski.store.order.OrderDeliveryJob.checkOrdersAndDeliver).
8. After the Order is sent for delivery it is removed from the database (org.aivanouski.store.order.OrderDeliveryJob.checkOrdersAndDeliver).

### Limitations
Because we are in growth stage now, we have some limitations:
- Our delivery area is not big, so we have only 50 buildings with 120 rooms in each, and it can be changed anytime in the configuration file (application.yml).
- We have only 4 ingredients for pancakes: 'dark chocolate', 'milk chocolate', 'whipped cream', and 'hazelnuts'. But we can add more ingredients in the future to our database.
- We can guarantee 30 seconds to prepare your pancakes, and it can be changed in the configuration file (application.yml) when we improve our process.
- Since we have limited number of delivery guys, we check the orders every 10 seconds, and it can be changed in the configuration file (application.yml) when we hire more delivery guys.

So, accept our apologies for any inconvenience and enjoy our pancakes, which we are happy to offer.

### Thank you
If you have any questions, please contact us at aliaksei.ivanouski@gmail.com, and we will be happy to discuss the solution or share some additional insights.
The feedback is much appreciated.

We hope you enjoy our pancakes and have a great day!

Kind regards,\
Test Store Team




# Technical details

## Tech Stack
Test Store is a simple application that allows users to create orders and add products to them.
The application is based on a monolith architecture and uses the following technologies:
- Java 11 as a programming language
- gRPC as a communication framework
- PostgreSQL as a relation database
- Flyway for database migrations
- Redis as a cache database
- JUnit 5 for testing
- Testcontainers for integration testing with databases
- Docker for containerization
- docker-compose to manage the application's containers

## Building application

To package your application:
```
./mvnw clean package
```

To launch tests:
```
./mvnw test
```

## Run application
- start full application (all containers including postgres and redis)
```shell
docker-compose -f docker-compose.local.yml up -d app
```

- start postgres
```shell
docker-compose -f docker-compose.local.yml up -d postgres
```

- start redis
```shell
docker-compose -f docker-compose.local.yml up -d redis
```

## Additional tools (if required)

### Install docker
```shell
brew install docker
```
### Install docker-compose
```shell
brew install docker-compose
```
### Install protobuf
```shell
brew install protobuf
```
