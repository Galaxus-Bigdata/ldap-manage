import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.Krb5HttpClientBuilder;
import org.apache.solr.client.solrj.impl.SolrHttpClientBuilder;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.security.HttpClientConfigurer;

import java.io.IOException;

public class SolrClientSetup {

    private CloudSolrClient solrClient;

    protected void setup() throws IOException {
        try {
            // Obtain the delegation token
            String delegationToken = getDelegationToken();

            // Configure the Solr client to use the delegation token
            Krb5HttpClientBuilder krbBuild = new Krb5HttpClientBuilder();
            SolrHttpClientBuilder kb = krbBuild.getBuilder();

            HttpClientUtil.setHttpClientBuilder(kb);
            HttpClientUtil.addConfigurer(new HttpClientConfigurer() {
                @Override
                public void configure(org.apache.http.impl.client.HttpClientBuilder builder, ModifiableSolrParams params) {
                    params.set("solr.auth.delegationToken", delegationToken);
                }

                @Override
                public void configure(org.apache.http.impl.client.CloseableHttpClient client, ModifiableSolrParams params) {
                    // No additional configuration needed
                }
            });

            String zkQuorum = "gbrdsr000013861:2181/solr";
            solrClient = new CloudSolrClient.Builder()
                    .withZkHost(zkQuorum)
                    .build();
            solrClient.setDefaultCollection("Test_noor");
            solrClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
            if (solrClient != null) {
                solrClient.close();
            }
        }
    }

    // Method to obtain the delegation token
    private String getDelegationToken() throws IOException {
        Configuration conf = new Configuration();
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("your_kerberos_principal", "your_keytab_file_path");

        UserGroupInformation ugi = UserGroupInformation.getLoginUser();
        Token<DelegationTokenIdentifier> token = ugi.getDelegationToken("solr");

        if (token == null) {
            throw new IOException("Failed to obtain delegation token");
        }

        return token.encodeToUrlString();
    }

    public static void main(String[] args) {
        SolrClientSetup setup = new SolrClientSetup();
        try {
            setup.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
