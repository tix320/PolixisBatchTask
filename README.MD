> Required java version: 17

# Build
```
./mvnw clean package
```

# Run
```
java -jar target/BatchTask.jar
```

# Fast test with docker

### Build
```
./mvnw clean package
```

### Run docker compose
```
docker-compose up
```

### Open DB adminer in the browser 
> http://localhost:8080/

### Fill form: 
```
System: PostgreSQL
Username: root
Password: root
Database: postgres
```
### Enjoy
