package HospitalManagementsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class HospitalManagementSystem {

    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/hospital");
    private static final String USERNAME = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static void main(String[] args) {
        if (PASSWORD == null) {
            System.out.println("Set the DB_PASSWORD environment variable before running. See README.md.");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver not found. Add the MySQL Connector/J jar to the project. See README.md.");
            return;
        }

        try (Scanner scanner = new Scanner(System.in);
             Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {

            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            while (true) {
                System.out.println("HOSPITAL MANAGEMENT SYSTEM");
                System.out.println("1. Add Patient");
                System.out.println("2. View Patient");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. Exit");
                System.out.print("Enter Your Choice: ");

                try {
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            patient.addPatient();
                            System.out.println();
                            break;
                        case 2:
                            patient.viewPatients();
                            System.out.println();
                            break;
                        case 3:
                            doctor.viewDoctors();
                            System.out.println();
                            break;
                        case 4:
                            bookAppointment(patient, doctor, connection, scanner);
                            System.out.println();
                            break;
                        case 5:
                            System.out.println("Thank You For Using Hospital Management System");
                            return;
                        default:
                            System.out.println("Enter Valid choice!!!");
                            break;
                    }
                } catch (InputMismatchException e) {
                    scanner.nextLine();
                    System.out.println("Invalid input. Please enter a number where a number is expected.");
                    System.out.println();
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Input was closed. Exiting.");
        } catch (SQLException e) {
            System.out.println("Couldn't connect to the database. Check that MySQL is running and that DB_URL, DB_USER and DB_PASSWORD are correct.");
            System.err.println("SQL error [state=" + e.getSQLState() + ", code=" + e.getErrorCode() + "]: " + e.getMessage());
        }
    }

    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner) {
        System.out.print("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        System.out.print("Enter Doctor Id: ");
        int doctorId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        LocalDate appointmentDate;
        try {
            appointmentDate = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Use YYYY-MM-DD, for example 2026-11-05.");
            return;
        }
        if (appointmentDate.isBefore(LocalDate.now())) {
            System.out.println("Appointment date can't be in the past.");
            return;
        }

        if (!patient.getPatientById(patientId) || !doctor.getDoctorById(doctorId)) {
            System.out.println("Either doctor or patient doesn't exist!!!");
            return;
        }

        if (!checkDoctorAvailability(doctorId, appointmentDate.toString(), connection)) {
            System.out.println("Doctor Not Available on this date!!");
            return;
        }

        String appointmentQuery = "INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(appointmentQuery)) {
            preparedStatement.setInt(1, patientId);
            preparedStatement.setInt(2, doctorId);
            preparedStatement.setString(3, appointmentDate.toString());
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Appointment Booked!");
            } else {
                System.out.println("Failed to Book Appointment!");
            }
        } catch (SQLException e) {
            System.out.println("Couldn't book the appointment because of a database problem. Try again.");
            System.err.println("SQL error [state=" + e.getSQLState() + ", code=" + e.getErrorCode() + "]: " + e.getMessage());
        }
    }

    public static boolean checkDoctorAvailability(int doctorId, String appointmentDate, Connection connection) {
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Couldn't check doctor availability because of a database problem.");
            System.err.println("SQL error [state=" + e.getSQLState() + ", code=" + e.getErrorCode() + "]: " + e.getMessage());
        }
        return false;
    }
}
