# Doors NG Client
This client uses the Eclipse Lyo Client to connect to a Doors NG instance. Allows for creating, reading, updating, and deleting Requirements, Requirement Collections, and folders. This client also supports custom fields for Requirements.

## Installation
- Copy doors.properties.example to doors.properties and edit with correct values
- (Optional) Copy log4j.properties.example to log4j.properties and edit
- Run `mvn package` or `mvn install`

## Execute CLI
- If you need it, the main CLI class is `gov.nasa.jpl.mbee.doorsng.DoorsStandalone`.
- You can execute:
```
java -jar doorsng-jar-with-dependencies.jar -project "europa" -action "get"
```
