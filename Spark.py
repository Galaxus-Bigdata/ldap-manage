import org.apache.spark.{SparkConf, SparkContext}

val sparkConf = new SparkConf().setAppName("HDFSReader")
val sparkContext = new SparkContext(sparkConf)

// Replace "hdfs://your-hdfs-path" with the actual HDFS path to your file
val hdfsFilePath = "hdfs://your-hdfs-path"

val hdfsFileRDD = sparkContext.textFile(hdfsFilePath)
hdfsFileRDD.foreach(println)

sparkContext.stop()
