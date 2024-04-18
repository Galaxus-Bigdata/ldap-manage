import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;

import java.io.File;

public class SolrConfigReader {
    public static void main(String[] args) {
        // Specify the path to your Solr home directory
        String solrHome = "/path/to/your/solr/home";

        // Initialize SolrResourceLoader
        SolrResourceLoader loader = new SolrResourceLoader(solrHome);

        // Specify the path to solrconfig.xml
        String configFile = "solrconfig.xml";

        // Load solrconfig.xml
        File configFileObj = new File(loader.getConfigDir(), configFile);

        // Read solrconfig.xml
        SolrConfig solrConfig = new SolrConfig(loader, configFile);

        // Now you can access SolrConfig properties
        // For example:
        int maxBooleanClauses = solrConfig.getMaxBooleanClauses();
        System.out.println("Max Boolean Clauses: " + maxBooleanClauses);
    }
}
