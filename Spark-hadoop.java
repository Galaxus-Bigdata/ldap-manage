import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkOnYarnExample {
    public static void main(String[] args) {
        // Create SparkSession with Hadoop and other configurations
        SparkSession spark = SparkSession.builder()
                .appName("SparkOnYarnExample")
                .master("yarn")
                .config("spark.hadoop.fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
                .config("spark.hadoop.hadoop.security.authentication", "kerberos")
                .config("spark.hadoop.hadoop.rpc.protection", "privacy")
                .config("spark.hadoop.dfs.replication", "3")  // Example HDFS replication factor
                .getOrCreate();

        // Set Kerberos principal and keytab
        String principal = "your_principal@REALM";
        String keytabPath = "/path/to/your/keytab";

        // Login user from keytab
        org.apache.hadoop.conf.Configuration conf = spark.sparkContext().hadoopConfiguration();
        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);
        try {
            UserGroupInformation.loginUserFromKeytab(principal, keytabPath);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Your Spark application code goes here
        // Example: Read a file into a DataFrame
        Dataset<Row> df = spark.read().text("/path/to/your/input");
        df.show();

        // Stop SparkSession when done
        spark.stop();
    }
}
