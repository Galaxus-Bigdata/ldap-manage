import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
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
            List<String> columnsToIndex = getColumnNames(jsonFilePath);

            // Load Avro data
            Dataset<Row> avroData = spark.read().format("avro").load("path/to/your/avro/file");

            // Select required columns
            Dataset<Row> selectedData = avroData.select(columnsToIndex.stream().map(col -> functions.col(col)).toArray(Column[]::new));

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

    private static List<String> getColumnNames(String jsonFilePath) throws IOException {
        List<String> columnNames = new ArrayList<>();
        try (Reader reader = new FileReader(jsonFilePath)) {
            // Parse JSON file
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            // Extract column names array
            JsonArray columnsArray = jsonObject.getAsJsonArray("columns");
            if (columnsArray != null) {
                for (int i = 0; i < columnsArray.size(); i++) {
                    columnNames.add(columnsArray.get(i).getAsString());
                }
            }
        }
        return columnNames;
    }

    private static void indexToSolr(SolrInputDocument doc) {
        // Initialize SolrClient with HTTPS URL and Kerberos Authentication
        String solrUrl = "https://your-solr-host:8983/solr";
        Configuration conf = new Configuration();
        conf.set("hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(conf);
        try {
            UserGroupInformation.loginUserFromKeytab("your_principal@YOUR_REALM", "/path/to/your/keytab");
            SolrClient solrClient = new HttpSolrClient.Builder(solrUrl)
                    .withHttpClient(org.apache.solr.client.solrj.impl.HttpClientUtil.createClient(null))
                    .withKerberosKeytab("/path/to/your/keytab")
                    .withKerberosPrincipal("your_principal@YOUR_REALM")
                    .build();
            // Index doc into Solr
            solrClient.add(doc);
            // Commit changes to Solr
            solrClient.commit();
            solrClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
