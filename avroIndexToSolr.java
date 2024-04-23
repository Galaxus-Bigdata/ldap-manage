import org.apache.avro.generic.GenericRecord;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.avro.*;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.RequestAuthenticationException;
import org.apache.solr.common.SolrInputDocument;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class IndexAvroDataToSolr {
    public static void main(String[] args) {
        // Initialize SparkSession
        SparkSession spark = SparkSession.builder()
                .appName("Index Avro Data to Solr")
                .master("local[*]")
                .getOrCreate();

        try {
            // Read JSON file to extract column names to index
            String jsonFilePath = "path/to/your/json/file";
            List<String> fieldNames = getFieldNames(jsonFilePath);

            // Load Avro data
            Dataset<Row> avroData = spark.read().format("avro").load("path/to/your/avro/file");

            // Convert DataFrame to SolrInputDocument and index into Solr
            avroData.foreachPartition(rows -> {
                try (SolrClient solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
                    rows.forEachRemaining(row -> {
                        SolrInputDocument doc = new SolrInputDocument();
                        for (int i = 0; i < fieldNames.size(); i++) {
                            doc.addField(fieldNames.get(i), row.getAs(i).toString());
                        }
                        try {
                            solrClient.add(doc);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    solrClient.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        spark.stop();
    }

    private static List<String> getFieldNames(String jsonFilePath) throws IOException {
        List<String> fieldNames = new ArrayList<>();
        try (Reader reader = new FileReader(jsonFilePath)) {
            // Parse JSON file
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            // Extract field names array
            JsonArray fieldsArray = jsonObject.getAsJsonArray("fields");
            if (fieldsArray != null) {
                for (int i = 0; i < fieldsArray.size(); i++) {
                    fieldNames.add(fieldsArray.get(i).getAsString());
                }
            }
        }
        return fieldNames;
    }
}
