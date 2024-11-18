package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Patient {
    //Fields will look to save are Username, Password(saved as a hash)
    private final String username;
    private final byte[] salt;
    private final byte[] hash;

    //Constructors (style adapted from caregiver)
    private Patient(PatientBuilder builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    private Patient(PatientGetter getter) {
        this.username = getter.username;
        this.salt = getter.salt;
        this.hash = getter.hash;
    }

    //Getters
    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    //Method to Insert Patient Info to DB
    //Adapted from the Caregiver Class
    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addPatient = "INSERT INTO Patients VALUES (? , ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addPatient);
            statement.setString(1, this.username);
            statement.setBytes(2, this.salt);
            statement.setBytes(3, this.hash);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }



    //inner-Class for Patient Builder (Adpated from Caregiver)
    public static class PatientBuilder {
        private final String username;
        private final byte[] salt;
        private final byte[] hash;

        public PatientBuilder(String username, byte[] salt, byte[] hash) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public  Patient build() {
            return new Patient(this);
        }
    }

    //inner-class for Patient Getter (Adapted from Caregiver)
    public static class PatientGetter {
        private final String username;
        private final String password;
        private byte[] salt;
        private byte[] hash;

        public PatientGetter(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public Patient get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getPatient = "SELECT Salt, Hash FROM Patients WHERE Username = ?";
            //query the DB for Patients Info
            try {
                PreparedStatement statement = con.prepareStatement(getPatient);
                statement.setString(1, this.username);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    //get Salt
                    byte[] salt = resultSet.getBytes("Salt");

                    //get hash, and we must trim it as there is "surronding  info in the DB"
                    byte[] hash = Util.trim(resultSet.getBytes("Hash"));

                    //hashed testPassword
                    byte[] calculatedHash = Util.generateHash(password, salt);

                    //check if we can map our Hashed Password to this Hashed out TestPassowrd
                    if(!Arrays.equals(hash, calculatedHash)) {
                        return  null;
                    } else  {
                        this.salt = salt;
                        this.hash = hash;
                        return new Patient(this);
                    }
                }
                return  null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }
    }


}
