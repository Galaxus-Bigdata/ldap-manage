import org.apache.avro.generic.GenericRecord;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.avro.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IndexAvroDataToSolr {
    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession spark = SparkSession.builder()
                .appName("Index Avro Data to Solr")
                .master("local[*]")
                .getOrCreate();

        try {
            // Read JSON file to extract column names to index
            List<String> columnsToIndex = Arrays.asList("column1", "column2", "column3"); // Replace with your logic to read from JSON

            // Load Avro data and select required columns
            Dataset<Row> avroData = spark.read().format("avro").load("path/to/your/avro/file");
            Dataset<Row> selectedData = avroData.select(columnsToIndex.get(0), columnsToIndex.subList(1, columnsToIndex.size()));

            // Convert DataFrame to SolrInputDocument and index into Solr
            selectedData.foreach(row -> {
                SolrInputDocument doc = new SolrInputDocument();
                for (String columnName : columnsToIndex) {
                    doc.addField(columnName, row.getAs(columnName));
                }
                indexToSolr(doc); // Method to index document into Solr
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        spark.stop();
    }

    private static void indexToSolr(SolrInputDocument doc) {
        // Initialize SolrClient
        try (SolrClient solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
            // Index doc into Solr
            solrClient.add(doc);
            // Commit changes to Solr
            solrClient.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
