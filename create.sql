CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Patients (
    Username varchar(255),
    Salt BINARY(16),
    HASH BINARY(16),
    PRIMARY KEY (Username) -- Since I beleive a person can get one vaccine given to them this is my pk method
);

CREATE TABLE Availabilities (
    AvailDate date,
    Username varchar(255) REFERENCES Caregivers,
    PRIMARY KEY (AvailDate, Username)
);

CREATE TABLE Appointments (
    PatientName varchar(255) REFERENCES Patients(Username),
    CaregiverName varchar(255) REFERENCES Caregivers(Username),
    vaccineName varchar(255),
    appointmentDate date,
);
--Adding Appointment ID as ID
CREATE UNIQUE INDEX ID
on Appointments (PatientName, CaregiverName, vaccineName, appointmentDate);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

