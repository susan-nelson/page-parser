HTML page text parser
=====================

This project depends on an installation of the following:
    for build, maven 3
        see maven.apache.org
    to run, java 8
        see oracle.com/technetwork/java/javase/downloads/index.html

To build and startup with embedded H2 database:
    mvn clean spring-boot:run

To exit:
    ctrl-c

To access the H2 database console
    http://localhost:8080/console

Scope
    supports only English and some unicode
    includes numbers (1, 01, 0.01, 1% and $0.01 are considered different words)
    # and @ are considered to be part of a word



