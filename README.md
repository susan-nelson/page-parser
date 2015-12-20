HTML page text parser
=====================

This project automatically runs the PageWordLoader from the main() method of PageParserApplication after startup of the application.

PageWordLoader parses one web page defined by a configured URL (defaults to www.yahoo.com).
    You can change the URL in resources/application.properties and then restart the application.

Third party libraries:
    JSoup for parsing the web page text.
    Guava for splitting into words and doing regex matches.

Scope:
    supports only English and some unicode.
    groups and stores the words as lower case.
    used this definition of a 'word' - requirements may vary:
        "a single distinct meaningful element of speech or writing, used with others (or sometimes alone) to form a
        sentence and typically shown with a space on either side when written or printed".
    includes numbers as words (1, 01, 0.01, 1% and $0.01 are considered different words).
    # and @ are considered to be part of a word.

Tradeoffs:
    Used third party libraries to assure robust, performant processing. However, need further work in understanding all
        intricacies of their use and decide if custom code would work better.
        For example: defined CharMatcher vats as local vars because google does not guarantee thread safety of CharMatcher
    Used embedded H2 database and tomcat so that I can control running of the app but this does cause bloat and of
        course can only be used for prototying or development.
        Batching database inserts may improve performance.
    Limited time was available so much more could be done to perfect the processing of words and improve performance.
        TODO: allow parsing and persisting of multiple pages.
        TODO: more robust error handling and logging

Known bugs:
    performance profiling needed - process is slow (possibly due to Guava).
    single character words - should allow only certain characters (example: 'a' is a word but 's' is not)
    abbreviations should have period at end (example: b.c., p.m.)
    some unwanted special characters are embedded in a few words but should only allow some (TM, degrees, etc).

DATABASE SCHEMA:
===============
Using two tables (PAGE and PAGE_WORD) just to show FK relationahip and JOIN. Probably overkill for this exercise.

This project runs with an embedded H2 database that is built by Hibernate at startup based on @Entity beans.
      (see com.susannelson.domain package)

DDL to manually build tables is also available - see /src/main/resources/create_tables.sql

HOW TO BUILD and RUN:
====================
This project depends on an installation of the following:
    for build, maven 3
        see maven.apache.org
    to run, java 8
        see oracle.com/technetwork/java/javase/downloads/index.html

To build and startup with embedded H2 database and tomcat:
    cd  to root of project
    mvn clean spring-boot:run

To access the H2 database console after startup:
    http://localhost:8080/console
    (no password needed)

    run the following sql to see words with highest word counts first:

    select page.url, word.word, word.count
    from page
    join page_word word
    on word.page_id = page.id
    order by page.url, word.count desc, word.word;

To exit:
    ctrl-c

To create an executable jar:
    cd  to root of project
    mvn clean package
    (creates page-parser-0.0.1-SNAPSHOT.jar)

To execute jar from the command line at root of project:
    java -jar target/page-parser-0.0.1-SNAPSHOT.jar


