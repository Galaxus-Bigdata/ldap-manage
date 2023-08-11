package org.apache.ambari.solr.metrics.maths;
import java.util.Collections;
import java.util.List;
import org.apache.ambari.solr.metrics.metrics.SolrJmxDataCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MedianCalculator {

    private static final Logger logger = LogManager.getLogger(SolrJmxDataCollector.class);
    public static double calculateMedian(List<Double> numbers) {
        try {
            if (numbers.isEmpty()) {
                throw new IllegalArgumentException("List is empty, cannot compute median.");
            }

            // Sort the list
            Collections.sort(numbers);

            int size = numbers.size();
            if (size % 2 == 0) {
                int middleIndex1 = (size - 1) / 2;
                int middleIndex2 = size / 2;
                double middleValue1 = numbers.get(middleIndex1);
                double middleValue2 = numbers.get(middleIndex2);
                return (middleValue1 + middleValue2) / 2.0;
            } else {
                int middleIndex = size / 2;
                return numbers.get(middleIndex);
            }
        }catch (Exception e){

            logger.error("{}", e.getMessage());
            return 0.0;
        }
    }
}
