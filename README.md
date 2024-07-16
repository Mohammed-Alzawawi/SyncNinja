# SyncNinja

> **Note:** SyncNinja currently supports Windows users only. 

## Overview
Checkout the Medium article for more details: [SyncNinja](https://medium.com/@talaqudah86/from-concept-to-code-how-to-develop-your-own-version-control-system-10c2a8e15e55)

This Maven project uses Neo4j for graph database operations. The project aims to create a snapshot of the Java code using the `mvn clean install` command and then create a batch file to execute the CLI using SyncNinja.

## Prerequisites

1. **Java**: Ensure you have JDK 8 or higher installed.
2. **Maven**: Make sure Apache Maven is installed and configured in your PATH.
3. **Neo4j**: Have a Neo4j database instance running and accessible.
4. **SyncNinja**: Download and install SyncNinja if it’s not included in the project.

## Setup Instructions

1. **Clone the Repository**
    ```sh
    git clone <repository-url>
    cd <repository-directory>
    ```

2. **Configure Neo4j**
    Update the `config.properties` file with your Neo4j database connection details.
    ```properties
    spring.neo4j.uri=bolt://localhost:7687
    spring.neo4j.authentication.username=neo4j
    spring.neo4j.authentication.password=password
    ```

## Building the Project

1. **Clean and Install**
    Run the following command to clean the project and install dependencies:
    ```sh
    mvn clean install
    ```

    This command will compile the project, and package it into a JAR file located in the `target` directory.

2. **create Batch File**
    Create the following batch file (`SyncNinja.bat`) and add it to the C:\Windows path: 
      ```sh
      @echo off
      java -jar "{$projectJarFile}" %*
    ```
    Replace the {$projectJarFile} with your project jar file generated in `target` directory.
    
## Running the Project

1. **Start Neo4j Database**
    Ensure your Neo4j database is running.

2. **Execute Batch File**
    Run the created batch file to execute the CLI, for example:
    ```sh
      syncninja init
    ```

  This will execute the CLI using SyncNinja with the necessary arguments configured in the batch file.


