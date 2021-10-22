# Revature Project 1

## Project Description

This project is an expense reimbursement system to help manage requests for reimbursement from expenses where occurred on company time. The project currently has options for travel, equpment and other expences.

## Technologies Used

* Java - version 1.8
* PostgreSql - version 9
* JUnit - version 4.12
* Javalin - version 4.0.1
* Log4j - version 2.14.1
* Mockito - version 3.12.4
* Gson - version 2.8.8
* HTML - version 5
* CSS - version 3

## Features

List of features ready and TODOs for future development
* Managers can create employee users
* Managers can view and approve requests for reimbursement
* Employee can submit requests for reimbursement
* Each reimbursement request has multiple expenses that can be on the request
* Employees can view all their past requests

To-do list:
* Allow custom expense types
* Stop hard coding the url for accessing the server
* Implement email for account creation, and password resets

## Getting Started

In order to run the application, you'll need a Tomcat server that runs on port 8080, and a PostgreSql server.
You'll also need maven installed, and accessible via the command line.

Before we get started with the project, you'll want to log in to your PostgreSql server and run the `setup.sql` file in order to create the tables and the admin user.

To get started your first going to want to cone the repository

`git clone https://github.com/ryanmohler17/Revature_Project1.git`

After that you'll want to enter to project directory.

`cd Revature_Project1`

Then you'll want to compile the project using maven.

`mvn package`

This will generate a war file at `PROJECT_DIR/target/Project1.war`. you'll want to rename this file to `ers.war` and copy it over to your tomcat webapps folder.
After you start tomcat it will explode the war file, once it's done you'll want to stop tomcat.

Now we need to create a properties file in order to store our database credentials and key used for verifying jwts.

Create the file at the following location.

`TOMCAT_FOLER/webapps/ers/WEB-INF/db.properties`

You're going to want to set the file contents as follows replacing anything in `<>`

```properties
db.driver_name=org.postgresql.Driver

# Set connection_url to the url for accessing the PostgreSql server 
db.url=<connection_url>

# Set username to the username to used to log into your database
db.username=<username>

# Set password to the password used to log into the database
db.password=<password>

# secret_key should be a base64 encoded random 64 bytes
user.secret=<secret_key>
```

After you create that file you can start tomcat back up.

## Usage

To get started you'll want to navigate to `http://localhost:8080/ers` and login with the following credentials.

Username: `admin`

Password: `password1234`