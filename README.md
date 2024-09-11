## Building

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


## Install protobuf
```shell
brew install protobuf
```