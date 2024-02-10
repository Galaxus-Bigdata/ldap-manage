import org.apache.hadoop.security.UserGroupInformation
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object SparkOnYarnExample {
  def main(args: Array[String]): Unit = {
    // Create SparkSession with Hadoop and other configurations
    val spark = SparkSession.builder()
      .appName("SparkOnYarnExample")
      .master("yarn")
      .config("spark.hadoop.fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
      .config("spark.hadoop.hadoop.security.authentication", "kerberos")
      .config("spark.hadoop.hadoop.rpc.protection", "privacy")
      .config("spark.hadoop.dfs.replication", "3")  // Example HDFS replication factor
      .getOrCreate()

    // Set Kerberos principal and keytab
    val principal = "your_principal@REALM"
    val keytabPath = "/path/to/your/keytab"

    // Login user from keytab
    val conf = spark.sparkContext.hadoopConfiguration
    conf.set("hadoop.security.authentication", "kerberos")
    UserGroupInformation.setConfiguration(conf)
    try {
      UserGroupInformation.loginUserFromKeytab(principal, keytabPath)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        return
    }

    // Your Spark application code goes here
    // Example: Read a file into a DataFrame
    val df: Dataset[Row] = spark.read.text("/path/to/your/input")
    df.show()

    // Stop SparkSession when done
    spark.stop()
  }
}
