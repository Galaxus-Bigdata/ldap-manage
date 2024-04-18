import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.IndexSchemaFactory;
import org.apache.solr.schema.ManagedIndexSchemaFactory;
import org.apache.solr.util.SystemIdResolver;
import org.xml.sax.InputSource;

import java.nio.file.Paths;

SolrResourceLoader loader = new SolrResourceLoader(Paths.get(mySolrHomeDir));
SolrConfig solrConfig = SolrConfig.readFromResource(loader, "solrconfig.xml");
InputSource is = new InputSource(loader.openResource("schema.xml"));
is.setSystemId(SystemIdResolver.createSystemIdFromResourceName("schema.xml"));
IndexSchemaFactory factory = new ManagedIndexSchemaFactory();
IndexSchema schema = factory.createFromInputStream(solrConfig, is);
validateSchema(schema);
return schema;
