================================================================================
                       Solr in Action Example Code
================================================================================

Thank you for purchasing Solr in Action! Here are some basic instructions on
running the example code provided in the book.

1. Directory layout

  $SIA_HOME - this is the location where you extracted the zip file.
  |
  |__src
  |   |__main
  |      |__java - contains all the Java source files from examples in the book
  |
  |__example-docs - contains example XML and JSON documents to create content
  |
  |__pom.xml - maven build file
  |
  |__README.txt - you're looking at it ;-)


2. Building the source code

You'll need Maven to build the source code. If you need some help on getting
Maven setup and running, please see: Maven in Five Minutes 
  
  http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html

Once you have Maven setup, cd into the directory where you extracted the
example source code zip file and do:

mvn clean package

This will compile the source code and build an executable JAR file in the
target directory named: sia-examples.jar


3. To make it easy for you to run the examples, we built a simple driver
application that processes command-line args and launches the example you
want to run. Basically, the driver allows you to just pass the name of
the example you want to run and it will figure it out. 

To run a specific example from the book, use the java -jar command to
launch the executable JAR you built in step 2 above.

For example, to run the ExampleSolrJClient application from Chapter 5, do:

java -jar target/sia-examples.jar ch5.ExampleSolrJClient

In most cases, you can just pass the example class name without the package
information and the driver will figure it out, i.e.
 
java -jar target/sia-examples.jar examplesolrjclient

The driver will figure out that you're trying to run example class:
sia.ch5.ExampleSolrJClient

To see a list of all available examples, simply do:

java -jar target/sia-examples.jar

To see a list of all examples for a specific chapter, pass the chapter number,
e.g. the following command will show all examples for chapter 5:

java -jar target/sia-examples.jar 5


Enjoy!
================================================================================

