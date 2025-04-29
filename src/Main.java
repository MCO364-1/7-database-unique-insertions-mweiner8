import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties credentials = new Properties();
        credentials.load(new FileInputStream("credentials.properties"));
        String endpoint = credentials.getProperty("db_connection");
        String database = credentials.getProperty("database");
        String username = credentials.getProperty("user");
        String password = credentials.getProperty("password");

        String connectionUrl =
                "jdbc:sqlserver://" + endpoint + ";"
                        + "database=" + database + ";"
                        + "user=" + username + ";"
                        + "password=" + password + ";"
                        + "encrypt=true;"
                        + "trustServerCertificate=true;"
                        + "loginTimeout=30;";

        Set<String> people = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement statement = connection.createStatement())
        {
            String selectSql = "SELECT * FROM People";
            ResultSet resultSet = statement.executeQuery(selectSql);

            while (resultSet.next()) {
                people.add(resultSet.getString(1).toLowerCase() + " " + resultSet.getString(2).toLowerCase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Scanner input = new Scanner(System.in);
        System.out.print("Enter a first name: ");
        String first = input.nextLine();
        System.out.print("Enter a last name: ");
        String last = input.nextLine();

        String lowercaseFirst = first.toLowerCase();
        String lowercaseLast = last.toLowerCase();
        boolean addedToSet = people.add(lowercaseFirst + " " + lowercaseLast);
        if (addedToSet){
            try (Connection connection = DriverManager.getConnection(connectionUrl)) {
                String insertSql = "INSERT INTO People (FirstName, LastName) VALUES (?, ?);";

                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, first);
                    preparedStatement.setString(2, last);
                    preparedStatement.execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Name already exists in the table");
        }

        int total = 0;
        int[] namesByLetter = new int[26];
        char firstLetter;
        for (String s : people){
            firstLetter = s.charAt(0);
            namesByLetter[((int) firstLetter) - 65]++;
            total++;
        }

        System.out.println("\nTotal records in table: " + total);
        System.out.println("Names by first letter:");
        for (int i = 0; i < 26; i++) {
            System.out.println(((char) (i + 65)) + ":" + namesByLetter[i]);
        }
    }
}