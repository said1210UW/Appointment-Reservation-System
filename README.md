#  Appointment Reservation System
This project implements an Appointment Reservation System for scheduling vaccination appointments, where patients can book time slots and caregivers manage vaccine stock and appointment availability. The system connects to a database hosted on Microsoft Azure and allows users to interactively schedule appointments through a command-line interface.

### The project is divided into two parts:

* Part 1: Database design, Patient information storage, and vaccine appointment scheduling (including the creation of tables and E/R diagram).
* Part 2: Continued development of the system, including full implementation of the application logic for scheduling, interacting with caregivers, and managing vaccine inventory.
The application uses JDBC (Java Database Connectivity) to interact with the database, enabling dynamic insertion, update, and retrieval of patient and appointment information.

## Key Components
* Database Schema written in the "ER_Diagram" PDF

* Patient Class: Stores information about patients, such as their name, contact information, and appointment details.

* Appointment Class: Manages the scheduling of appointments, including patient ID, date/time of the appointment, and vaccine stock.

* Caregiver Class: Responsible for managing the vaccine inventory and available appointment slots.

* JDBC Code: Includes classes for interacting with the database to fetch and update appointment data, patient records, and vaccine inventory.

* Command-Line Interface (CLI): A text-based user interface that allows patients to view available time slots, schedule appointments, and view their appointment status.

## How to Run the Project
1. Install Dependencies: 
    * Java 8 or later.
    * JDBC driver for Azure SQL (e.g., Microsoft JDBC Driver for SQL Server).
    * Any required dependencies as specified in the project's build file (e.g., Maven or Gradle).

2. Database Setup:
    * Follow the instructions to set up the Microsoft Azure database and create the necessary tables from the schema design.

3. Configure Database Connection:
    * Update the connection settings in your Java code to point to your Azure SQL database (e.g., server name, username, password).

4. Run the Application:
    * Compile and run the Java application.
    * Interact with the command-line interface to schedule appointments and manage patient data.
