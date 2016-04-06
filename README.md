# Doors NG Client
This client extends the Eclipse Lyo Client to connect to a Doors NG instance. Allows for creating, reading, updating (TODO), and deleting Requirements, Requirement Collections, and folders. This client also supports custom fields for Requirements.

## Installation
- Copy doors.properties.example to doors.properties and edit with correct values
- (Optional) Copy log4j.properties.example to log4j.properties and edit
- Run `mvn package` or `mvn install`

## Execute CLI
- If you need it, the main CLI class is `gov.nasa.jpl.mbee.doorsng.DoorsStandalone`. So, you can execute either:
```
java -cp doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -user "SOMEUSER" -pass "SOMEPASSWORD" -url "https://cae-doors-test.jpl.nasa.gov:9443/rm/" -project "europa" -action "read"
```
OR if you included your doors.properties file
```
java -jar doorsng-jar-with-dependencies.jar -project "europa" -action "read"
```

## Full tests
```
java -cp target/doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -action "create" -user "SOMEUSER" -pass "SOMEPASSWORD" -url "https://cae-doors-test.jpl.nasa.gov:9443/rm/" -project "OpenMBEE Test 2" -requirement "{\"title\":\"CLI TEST\", \"description\":\"Some description\", \"parent\":\"Some Test Folder 6"}"

java -cp target/doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -action "read" -user "SOMEUSER" -pass "SOMEPASSWORD" -url "https://cae-doors-test.jpl.nasa.gov:9443/rm/" -project "OpenMBEE Test 2"

java -cp target/doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -action "delete" -user "SOMEUSER" -pass "SOMEPASSWORD" -url "https://cae-doors-test.jpl.nasa.gov:9443/rm/" -project "OpenMBEE Test 2"
```