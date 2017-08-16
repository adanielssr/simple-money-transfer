## simple-money-transfer

This application exposes a simple RESTful service to make transfer between accounts.
Another services to create and retrieve accounts are available.

[Project lombok](https://projectlombok.org/) is used on this project.
This application makes use of [Vertx](http://vertx.io/) (version 3.0.0) framework (Vertx core and Vertx web).

As tests frameworks it's being used:
- Junit (version 4.12)
- Mockito (version 1.10.19)
- Vertex unit (version 3.0.0)

# build and test
To test and build this application just run the following command:

``mvn clean package``

# run
To run this application just run the following command (this will use port 8080 by default):

``java -jar api/target/simple-money-transfer-api-dist.jar``

To override the default port you can use the following command substituting <HTTP_PORT> for an available port in the system:

``echo '{"http.port" : <HTTP_PORT>}' > conf.json && java -jar api/target/simple-money-tranfer-api-1.0.0-SNAPSHOT-dist.jar -conf conf.json``