import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import javax.security.auth.login.Configuration;
import java.io.IOException;

public class SolrIndexer {

    public static class IndexMapper extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // Assuming input format: id,name
            String[] fields = value.toString().split(",");
            if (fields.length == 2) {
                context.write(new Text(fields[0]), new Text(fields[1]));
            }
        }
    }

    public static class IndexReducer extends Reducer<Text, Text, Text, Text> {
        private SolrClient solrClient;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            String solrUrl = conf.get("solr.url");
            solrClient = new HttpSolrClient.Builder(solrUrl).build();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text val : values) {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", key.toString());
                doc.addField("name", val.toString());
                solrClient.add(doc);
            }
            solrClient.commit();
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            solrClient.close();
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.security.auth.login.config", "path_to_your_jaas.conf");

        Configuration conf = new Configuration();
        conf.set("solr.url", "http://your_solr_host:8983/solr/your_collection");

        Job job = Job.getInstance(conf, "Solr Indexer");
        job.setJarByClass(SolrIndexer.class);
        job.setMapperClass(IndexMapper.class);
        job.setReducerClass(IndexReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
