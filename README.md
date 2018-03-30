[<img src="https://img.shields.io/travis/playframework/play-java-starter-example.svg"/>](https://travis-ci.org/playframework/play-java-starter-example)
[![Maintainability](https://api.codeclimate.com/v1/badges/70649b79bf4a747a69e6/maintainability)](https://codeclimate.com/github/InceptAi/neo-backend/maintainability)

# Java backend for Neo assistant

This is the java backend for Neo AI assistant, a mobile application for real-time AI-driven tech support. The app is supported by a backend that uses an inference algorithm with a crowdsourced knowledge base to compute solutions. The knowledge about a technical issue gets shared across users using our expert system which can perform root cause analysis and match it up with a possible fix in the knowledge base. Providing solution customized for user’s device and navigating complex settings to apply such a solution are two key components of our approach. 

## Running

Run this using [sbt](http://www.scala-sbt.org/). 
```
sbt run
```

And then go to http://localhost:9000 to see the running web application.

## Controllers

There are two key Controllers.

- CrawlController.java:

  Processes the UI screen data from the client device and stores it in its knowledge graph.

- ActionController.java:

  Processes client requests for help and returns the top matching actions that can resolve an issue.


## Components

- Model:

  Data model for the backend -- UI Screen, UI Elements, UI Paths, UI Graph

- View:

  Structures sent down to the client. Includes sending down navigation paths for taking automated actions.
