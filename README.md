# Doors NG Model Development Kit
This client extends the Eclipse Lyo Client to connect to a Doors NG instance. Allows for creating, reading, updating (TODO), and deleting Requirements, Requirement Collections, and folders. This client also supports custom fields for Requirements.

## Installation
- Requires Java 10+
- (Optional) Install Eclipse Lyo (`git clone https://git.eclipse.org/r/lyo/org.eclipse.lyo.core` and `git clone https://git.eclipse.org/r/lyo/org.eclipse.lyo.client`, then `mvn install` in `org.eclipse.lyo.oslc4j.core` and `org.eclipse.lyo.client.java`)
- (Optional) Copy log4j.properties.example to log4j.properties and edit
- Run `mvn package` or `mvn install`

## Execute CLI
- If you need it, the main CLI class is `gov.nasa.jpl.mbee.doorsng.DoorsStandalone`. So, you can execute either:
```
java -cp doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -action "SOMEACTION" -consumer "CONSUMERKEY" -secret "CONSUMERSECRET" -user "SOMEUSER" -pass "SOMEPASSWORD" -url "DOORSSERVER/rm/" -project "SOMEPROJECT"
```

## Full tests
```
java -cp target/doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -action "create" -consumer "CONSUMERKEY" -secret "CONSUMERSECRET" -user "SOMEUSER" -pass "SOMEPASSWORD" -url "https://cae-doors-test.jpl.nasa.gov:9443/rm/" -project "OpenMBEE Test 2" -requirement "{\"title\":\"CLI TEST\", \"description\":\"Some description\", \"parent\":\"Some Test Folder 6\"}"

java -cp target/doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -action "read" -consumer "CONSUMERKEY" -secret "CONSUMERSECRET" -user "SOMEUSER" -pass "SOMEPASSWORD" -url "https://cae-doors-test.jpl.nasa.gov:9443/rm/" -project "OpenMBEE Test 2"

java -cp target/doorsng-jar-with-dependencies.jar gov.nasa.jpl.mbee.doorsng.DoorsStandalone -action "delete" -consumer "CONSUMERKEY" -secret "CONSUMERSECRET" -user "SOMEUSER" -pass "SOMEPASSWORD" -url "https://cae-doors-test.jpl.nasa.gov:9443/rm/" -project "OpenMBEE Test 2"
```

## New DNG Export to JSON
```
./mvnw package && java -jar target/doorsng.jar -user "SOMEUSER" -pass "SOMEPASSWORD" -url "DOORSSERVER/rm/" -project "SOMEPROJECT"
```
