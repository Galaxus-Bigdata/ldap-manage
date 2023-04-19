import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HivePerformanceTest {

    private static final String HIVE_JDBC_URL = "jdbc:hive2://<your_hive_host>:<your_hive_port>/default";
    private static final String HIVE_USER = "<your_hive_username>";
    private static final String HIVE_PASSWORD = "<your_hive_password>";

    public static void main(String[] args) {
        int numRuns = 10; // Set the number of times you want to run the group by query

        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try (Connection connection = DriverManager.getConnection(HIVE_JDBC_URL, HIVE_USER, HIVE_PASSWORD)) {
            for (int i = 0; i < numRuns; i++) {
                long startTime = System.currentTimeMillis();
                executeGroupByQuery(connection);
                long endTime = System.currentTimeMillis();
                System.out.println("Run " + (i + 1) + ": Query execution time: " + (endTime - startTime) + " ms");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void executeGroupByQuery(Connection connection) throws SQLException {
        String groupByQuery = "SELECT product_id, SUM(quantity) as total_quantity FROM sales GROUP BY product_id";

        try (PreparedStatement preparedStatement = connection.prepareStatement(groupByQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                int totalQuantity = resultSet.getInt("total_quantity");
                // You can do something with the results here or just ignore them
            }
        }
    }
}
