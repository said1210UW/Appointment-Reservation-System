package scheduler;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Locale;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) throws SQLException {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Part 2)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        //Adapted similarly to createCaregiver

        //Check if the Array has all neededInfo (at least with amount)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return; //To stop the method from going further
        }
        String username = tokens[1];
        String password = tokens[2];

        //Check if username exists
        if(usernameExistsPatient(username)) {
            System.out.println("Username taken, try again");
            return; //To Stop the program from going further
        }

        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);

        //create our Patient
        try {
            currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
            //Save this Patient in the Patient Table
            currentPatient.saveToDB();
            System.out.println(" *** Account created successfully *** ");
        } catch (SQLException e) {
            System.out.println("Create failed");
            e.printStackTrace();
        }

    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println(" *** Account created successfully *** ");
        } catch (SQLException e) {
            System.out.println("Create failed");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }


    private static void loginPatient(String[] tokens) {
        //login_patient <username> <patient>
        //implementation is mostly adapted off loginCaregiver

        //check if someone logged in, if they are we'll tell them to logout
        if(currentCaregiver != null || currentPatient != null) {
            System.out.println("Already logged-in!");
            return;
        }

        //check if we have all the needed info in the Array (at least by amount)
        if(tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            //Assign the new Patient object info from DB they wanted
           patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Error occured when loggin in");
            e.printStackTrace();
        }

        //check if login was great by telling them
        if (patient == null) {
            System.out.println("Please try again!");
        } else {
            System.out.println("Patient logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("Already logged-in!");
            return;
        }

        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when logging in");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Please try again!");
        } else {
            System.out.println("Caregiver logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        //Check1: Needed Info
        if(tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }


        /* Step 2: Query in Caregiver Table all the Availble Caregivers with Given "Available Date"
         * and Query The Amount of Vaccine Doses for Each Vaccine (eg: Phizer, 200; J&J 3000)
         */
        try {
            //Check2: See if date is valid if so Save date for easy to read code
            Date date = Date.valueOf(tokens[1]);
            queryCaregiverDates(date);
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        }
    }

    // Helper method to querry the Vaccine Inventory
    private static void queryVaccineInventory() {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String availVaccines = "SELECT * FROM Vaccines AS V WHERE V.Doses > 0";
            PreparedStatement getAvailableVaccines = con.prepareStatement(availVaccines);
            ResultSet resultAvailibleVaccines = getAvailableVaccines.executeQuery();
            System.out.println("Here are the Remaning Vaccines along with their Stock");
            while (resultAvailibleVaccines.next()) {
                System.out.print("Name: " + resultAvailibleVaccines.getString(1));
                System.out.println(" | Dose Amount: "  + resultAvailibleVaccines.getInt(2));
            }

        } catch (SQLException e) {
            System.out.println("There's Seems to be an error when searching the vaccine stock");
            e.printStackTrace();

        }  finally {
            cm.closeConnection();
        }

    }

    //helper to querry Cargivers at a Said Available Date
    private static void queryCaregiverDates(Date date) {
    // instantiating a connection manager class
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String availCargiversQuery = "SELECT Username, AvailDate FROM Availabilities WHERE AvailDate = ?";
            PreparedStatement getAvailableCaregivers = con.prepareStatement(availCargiversQuery);

            getAvailableCaregivers.setDate(1, date);
            System.out.println("Query: "+ getAvailableCaregivers);
            ResultSet resultAvailibleCaregivers = getAvailableCaregivers.executeQuery();
            if (!resultAvailibleCaregivers.next()) {
                System.out.println("There are no Caregivers Available on " + date);
                return;
            } else  {
                System.out.println("Here are the Available Caregivers");
                while (resultAvailibleCaregivers.next()) {
                    System.out.print("Caregiver: " + resultAvailibleCaregivers.getString(1));
                    System.out.println(" | Date: "  + resultAvailibleCaregivers.getDate(2));
                }
                System.out.println();
                queryVaccineInventory();
            }
        } catch (SQLException e) {
            System.out.println("There's Seems to be an Error when searching for a Caregiver of type: " + e); //Double check with TA
            e.printStackTrace();
            return;
        }  finally {
            cm.closeConnection();
        }
    }

    //helper: Which queries if a given vaccine is available the following takes in a string representing the vaccine name
    private static boolean queryDoseAvailability(String vaccineName)  {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String availDoseQuery = "SELECT COUNT(*) FROM Vaccines AS V WHERE V.Name = ?";
            PreparedStatement getAvailableDoses = con.prepareStatement(availDoseQuery);
            getAvailableDoses.setString(1, vaccineName);
            ResultSet resultAvailibleDoses = getAvailableDoses.executeQuery();

            if (resultAvailibleDoses.next() ) {
                if(resultAvailibleDoses.getInt(1) <= 0) {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("There's Seems to ben an error when looking for vaccine count");
            e.printStackTrace();
        }  finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void reserve(String[] tokens)  {
        //Check 1: All needed info is ok

        if(tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }

        // Check 2: Check if they're logged in as Patient and They're not a Caregiver
        if (currentPatient == null || currentCaregiver != null) {
            System.out.println("Please Login as a patient to Reserve a Vaccine");
            return;
        }
        String patientName = currentPatient.getUsername();
        String date = tokens[1];
        String vaccineName = tokens[2].substring(0,1).toUpperCase() + tokens[2].substring(1); //All my vaccines are capitlized so this is to dummy-proof input

        //Check 3: If the vaccine is available
         if (!queryDoseAvailability(vaccineName)) {
             System.out.println("Please Search for a different vaccine from the Available list of Vaccines Below!");
             queryVaccineInventory();
             return;
         }

        //Check 4: See if the given date provides an available doctor
        if (queryRandomCareGiver(date) == "null") {
            System.out.println("Please find a different to date; as there are no caregiver working then");
            return;
        }

        //Get RandomCaregiver
        String randCaregiver = queryRandomCareGiver(date);

        //execute appointment
        appointmentCreater(patientName, randCaregiver, vaccineName, date);

        //remove caregiver and date from Avaliblities
        updateAvailability(randCaregiver,date);

        //update vaccine stock
        updateDoseCount(vaccineName);

        System.out.println("You Have Successfully Reserved an Appointment for the " + vaccineName + " vaccine!");
        System.out.println("Caregiver: " + randCaregiver);
    }

    private static String queryRandomCareGiver(String date) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String randCargiverQuery = "SELECT TOP 1 A.Username FROM Availabilities AS A WHERE A.Availdate = ? ORDER BY NEWID()";
            PreparedStatement getRandCaregiver = con.prepareStatement(randCargiverQuery);
            getRandCaregiver.setString(1, date);
            ResultSet result = getRandCaregiver.executeQuery();
            if (result.next()) {
                String Caregiver = result.getString(1);
                return Caregiver;
            } else {
                return "null";
            }
        } catch (SQLException e) {
            System.out.println("There's Seems to be an error when looking for a caregiver");//Double check with TA
            e.printStackTrace();
            return "null";
        } finally {
            cm.closeConnection();
        }
    }

    //Helper Method for Reserve: Allows to add info to the Appointments Table
    private static void appointmentCreater(String patientName, String caregiverName, String vaccineName, String date) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String insertInfo= "INSERT INTO Appointments Values (?, ?, ?, ?)";
            PreparedStatement reservation = con.prepareStatement(insertInfo);
            reservation.setString(1, patientName); //Patient Name
            reservation.setString(2, caregiverName); //Caregiver Name
            reservation.setString(3, vaccineName); //Vaccine Name
            reservation.setString(4, date); //Date
            reservation.executeUpdate();


        } catch (SQLException e) {
            System.out.println("There's Seems to be an Error when reserveing");
            e.printStackTrace();
        }  finally {
            cm.closeConnection();
        }
    }

    //Helper Method for Reserve: Allows to update the avaiablity of caregiver
    private static void updateAvailability(String caregiverName, String date) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String insertInfo = "DELETE FROM Availabilities WHERE Username = ? AND AvailDate = ?";
            PreparedStatement updateAvail = con.prepareStatement(insertInfo);
            updateAvail.setString(1, caregiverName);
            updateAvail.setString(2, date);
            updateAvail.executeUpdate();
        } catch (SQLException e) {
            System.out.println("There's Seems to be an Error when reserveing");
            e.printStackTrace();
        }  finally {
            cm.closeConnection();
        }
    }

    //Helper Method for Reserve: Allows to decrement the vaccine stock of a given vaccine
    private static void updateDoseCount(String vaccineName) {
        //get vaccineCount
        int vaccineCount = getVaccineStock(vaccineName) - 1; //assumes an appoint uses One vaccine

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String newDoseValue = "UPDATE Vaccines SET DOSES = ? WHERE Name = ?";
            PreparedStatement updateAvail = con.prepareStatement(newDoseValue);
            updateAvail.setInt(1, vaccineCount);
            updateAvail.setString(2, vaccineName);
            updateAvail.executeUpdate();
        } catch (SQLException e) {
            System.out.println("There's Seems to be an Error when updating doses");
            e.printStackTrace();
        }  finally {
            cm.closeConnection();
        }
    }

    //Helper Method: Allowing us to query for the vaccine stock of a said vaccine
    private static int getVaccineStock(String vaccineName)  {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            String availDoseQuery = "SELECT Doses FROM Vaccines AS V WHERE V.Name = ?";
            PreparedStatement getAvailableDoses = con.prepareStatement(availDoseQuery);
            getAvailableDoses.setString(1, vaccineName);
            ResultSet resultAvailibleDoses = getAvailableDoses.executeQuery();

            if (resultAvailibleDoses.next() ) {
                return resultAvailibleDoses.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("There's Seems to ben an error when looking for vaccine count");
            e.printStackTrace();
        }  finally {
            cm.closeConnection();
        }
        return 0;
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        if (currentCaregiver != null && currentCaregiver.getUsername() != null) { //If I can query your cargiver username you're logged in as caregiver
            String patientAppointmentStatement = "SELECT PatientName, vaccineName, appointmentDate FROM Appointments WHERE CaregiverName = ?";
            try {
                PreparedStatement getAppointment = con.prepareStatement(patientAppointmentStatement);
                getAppointment.setString(1,currentCaregiver.getUsername());
                ResultSet appointments = getAppointment.executeQuery();
                if(!appointments.next()) {
                    System.out.println("It appears you dont have any appointments");
                    return;
                }
                while (appointments.next()) {
                    System.out.print ("Patient: " + appointments.getString(1) + " | ");
                    System.out.print("Vaccine: " + appointments.getString(2) + " | ");
                    System.out.println("Date: " + appointments.getString(3));
                }
            } catch (SQLException e) {
                System.out.println("There seems to be some problems when searching for you appointments");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        } else if (currentPatient != null && currentPatient.getUsername() != null) {
            String patientAppointmentStatement = "SELECT CaregiverName, vaccineName, appointmentDate FROM Appointments WHERE PatientName = ?";
            try {
                PreparedStatement getAppointment = con.prepareStatement(patientAppointmentStatement);
                getAppointment.setString(1,currentPatient.getUsername());
                ResultSet appointments = getAppointment.executeQuery();
                if(!appointments.next()) {
                    System.out.println("It appears you dont have any appointments");
                    return;
                }
                while (appointments.next()) {
                    System.out.print ("Caregiver: " + appointments.getString(1) + " | ");
                    System.out.print("Vaccine: " + appointments.getString(2) + " | ");
                    System.out.println("Date: " + appointments.getString(3));
                }
            } catch (SQLException e) {
                System.out.println("There seems to be some problems when searching for you appointments");
                e.printStackTrace();
            } finally {
                cm.closeConnection();
            }
        } else {
            System.out.println("Please Login either as a Caregiver or a Patient to view appointments");
            return;
        }

    }

    private static void logout(String[] tokens) {
        if (currentPatient == null && currentCaregiver==null) {
            System.out.println("Please Login as a Patient or Caregiver to LogOut!");
            return;
        } else if (currentCaregiver!= null & currentPatient == null) {
            System.out.println("You Have succesuflly logged Caregiver " + currentCaregiver.getUsername());
            currentCaregiver = null;
        } else if (currentPatient != null & currentCaregiver == null) {
            System.out.println("You Have succesuflly logged  Patient " + currentPatient.getUsername());
            currentPatient = null;
        }
    }
}
