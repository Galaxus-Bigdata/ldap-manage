/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ambari.solr.metrics.metrics;

import org.apache.ambari.solr.metrics.maths.MedianCalculator;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SolrJmxDataCollector {

  private static final Logger logger = LogManager.getLogger(SolrJmxDataCollector.class);

  @Autowired
  private MBeanServerConnection mbsc;

  @Value("${infra.solr.metrics.filter.cache.class:org.apache.solr.search.FastLRUCache}")
  private String filterCacheClass;

  @Value("${infra.solr.metrics.per_seq_filter.cache.class:org.apache.solr.search.LRUCache}")
  private String perSeqFilterCacheClass;

  @Value("${infra.solr.metrics.query_result.cache.class:org.apache.solr.search.LRUCache}")
  private String queryResultCacheCacheClass;

  @Value("${infra.solr.metrics.document.cache.class:org.apache.solr.search.LRUCache}")
  private String documentCacheClass;

  @Value("${infra.solr.metrics.field_value.cache.class:org.apache.solr.search.FastLRUCache}")
  private String fieldValueCacheClass;

  @Value("${infra.solr.metrics.solr.port:8886}")
  private int solrPort;

  public List<SolrMetricsData> collectNodeJmxData() throws Exception {
    List<SolrMetricsData> solrNodeMetricsList = new ArrayList<>();

    CompositeDataSupport heapMemoryUsage = (CompositeDataSupport) mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
    Long heapUsed = (Long) heapMemoryUsage.get("used");
    Long heapMax = (Long) heapMemoryUsage.get("max");

    Integer threadCount = (Integer) mbsc.getAttribute(new ObjectName("java.lang:type=Threading"), "ThreadCount");
    CompositeDataSupport nonHeapMemoryUsage = (CompositeDataSupport) mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "NonHeapMemoryUsage");
    Long nonHeapUsed = (Long) nonHeapMemoryUsage.get("used");
    Long nonHeapMax = (Long) nonHeapMemoryUsage.get("max");

    Long g1OldGenCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=G1 Old Generation,type=GarbageCollector", "CollectionCount");
    Long g1OldGenCollTime = (Long)getOrSkipJMXObjectAttribute("java.lang:name=G1 Old Generation,type=GarbageCollector", "CollectionTime");
    Long g1YoungGenCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=G1 Young Generation,type=GarbageCollector", "CollectionCount");
    Long g1YoungGenCollTime = (Long)getOrSkipJMXObjectAttribute("java.lang:name=G1 Young Generation,type=GarbageCollector", "CollectionTime");
    Long g1CMSCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ConcurrentMarkSweep,type=GarbageCollector", "CollectionCount");
    Long g1CMSCollTime = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ConcurrentMarkSweep,type=GarbageCollector", "CollectionTime");
    Long g1ParNewCollCount = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ParNew,type=GarbageCollector", "CollectionCount");
    Long g1ParNewCollTime = (Long) getOrSkipJMXObjectAttribute("java.lang:name=ParNew,type=GarbageCollector", "CollectionTime");

    Double cpuProcessLoad = (Double) mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "ProcessCpuLoad");
    if (cpuProcessLoad == -1.0) {
      cpuProcessLoad = Double.NaN;
    } else {
      cpuProcessLoad = Double.parseDouble(String.format("%.3f", cpuProcessLoad));
    }

    Long openFileDescriptorCount = (Long) mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "OpenFileDescriptorCount");

    addToMetricListIfNotNUll("solr.admin.info.gc.g1oldgen.count", g1OldGenCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.g1oldgen.time", g1OldGenCollTime, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.g1younggen.count", g1YoungGenCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.g1younggen.time", g1YoungGenCollTime, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.cms.count", g1CMSCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.cms.time", g1CMSCollTime, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.parnew.count", g1ParNewCollCount, solrNodeMetricsList);
    addToMetricListIfNotNUll("solr.admin.info.gc.parnew.time", g1ParNewCollTime, solrNodeMetricsList);

    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.system.processCpuLoad", cpuProcessLoad,true, "Double", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.system.openFileDescriptorCount", openFileDescriptorCount.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.memory.used", heapUsed.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.memory.max", heapMax.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.non-heap.used", nonHeapUsed.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.non-heap.max", nonHeapMax.doubleValue(),true, "Long", null));
    solrNodeMetricsList.add(new SolrMetricsData("solr.admin.info.jvm.thread.count", threadCount.doubleValue(),true, "Long", null));

    addNetworkMetrics(solrNodeMetricsList);

    return solrNodeMetricsList;
  }

  public List<SolrMetricsData> collectAggregatedCoreJmxData() throws Exception {
    List<SolrMetricsData> solrCoreMetricsData = getSolrCoreMetricsData();
    final Map<String, SolrMetricsData> metricNameAndData = new HashMap<>();
    for (SolrMetricsData solrMetricsData : solrCoreMetricsData) {
      if (metricNameAndData.containsKey(solrMetricsData.getMetricsName())) {
        SolrMetricsData actualMetric = metricNameAndData.get(solrMetricsData.getMetricsName());
        SolrMetricsData newMetric = new SolrMetricsData(
          actualMetric.getMetricsName(),actualMetric.getValue() + solrMetricsData.getValue(),
          actualMetric.isPointInTime(), actualMetric.getType(), null);
        metricNameAndData.put(actualMetric.getMetricsName(), newMetric);

      } else {
        metricNameAndData.put(solrMetricsData.getMetricsName(),
          new SolrMetricsData(solrMetricsData.getMetricsName(), solrMetricsData.getValue(),
            solrMetricsData.isPointInTime(), solrMetricsData.getType(), null));
      }
    }
    final List<SolrMetricsData> result;
    if (metricNameAndData.isEmpty()) {
      result = new ArrayList<>();
    } else {
      result = new ArrayList<>(metricNameAndData.values());
    }
    return result;
  }

  public Map<String, List<SolrMetricsData>> collectCoreJmxData() throws Exception {
    final Map<String, List<SolrMetricsData>> metricsPerCore = new HashMap<>();
    final List<SolrMetricsData> solrCoreMetricsList = getSolrCoreMetricsData();
    for (SolrMetricsData solrMetricsData : solrCoreMetricsList) {
      if (metricsPerCore.containsKey(solrMetricsData.getCore())) {
        List<SolrMetricsData> existingList = metricsPerCore.get(solrMetricsData.getCore());
        existingList.add(solrMetricsData);
      } else {
        List<SolrMetricsData> newList = new ArrayList<>();
        newList.add(solrMetricsData);
        metricsPerCore.put(solrMetricsData.getCore(), newList);
      }
    }
    return metricsPerCore;
  }

  private List<SolrMetricsData> getSolrCoreMetricsData() throws Exception {

    String[] domains = mbsc.getDomains();
    String[] solrCores = filterCores(domains);
    logger.info("All solrCores {}",solrCores);
    List<SolrMetricsData> solrCoreMetricsList = new ArrayList<>();
    logger.info("Inside getSolrCoreMetricsData");
    if (solrCores.length > 0) {
      for (String solrCore : solrCores) {
        logger.info("SolrCore {}",solrCore);
        String[] updateHandlerStrings = formatDomain(solrCore,"UPDATE","updateHandler");
        for (String updateHandlerString : updateHandlerStrings) {
          logger.debug("UpdateHandler Bean {}",updateHandlerString);
          ObjectName updateHandlerObjectName = new ObjectName(String.format("%s:%s", solrCore, updateHandlerString));
          String collectionName = String.format("%s_%s_%s",updateHandlerObjectName.getKeyProperty("dom2"),updateHandlerObjectName.getKeyProperty("dom3"),updateHandlerObjectName.getKeyProperty("dom4"));
          if (updateHandlerObjectName.getKeyProperty("name").equals("adds")){
          Long adds = (Long) mbsc.getAttribute(updateHandlerObjectName, "Value");
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.updateHandler.adds",collectionName), adds.doubleValue(), true, "Long", solrCore));}
          if (updateHandlerObjectName.getKeyProperty("name").equals("deletesById")){
          Long deletesById = (Long) mbsc.getAttribute(updateHandlerObjectName, "Value");
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.updateHandler.deletesById",collectionName), deletesById.doubleValue(), true, "Long", solrCore));}
          if (updateHandlerObjectName.getKeyProperty("name").equals("deletesByQuery")){
          Long deletesByQuery = (Long) mbsc.getAttribute(updateHandlerObjectName, "Value");
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.updateHandler.deletesByQuery",collectionName), deletesByQuery.doubleValue(), true, "Long", solrCore));}
          if (updateHandlerObjectName.getKeyProperty("name").equals("docsPending")){
          Long docsPending = (Long) mbsc.getAttribute(updateHandlerObjectName, "Value");
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.updateHandler.docsPending",collectionName), docsPending.doubleValue(), true, "Long", solrCore));}
          if (updateHandlerObjectName.getKeyProperty("name").equals("errors")){
          Long errors = (Long) mbsc.getAttribute(updateHandlerObjectName, "Value");
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.updateHandler.errors",collectionName), errors.doubleValue(), true, "Long", solrCore));}
          if (updateHandlerObjectName.getKeyProperty("name").equals("transaction_logs_total_size")){
          Long transactionLogsTotalSize = (Long) mbsc.getAttribute(updateHandlerObjectName, "Value");
            // file in bytes
            solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.updateHandler.transaction_logs_total_size",collectionName),
                    transactionLogsTotalSize.doubleValue(), true, "Long", solrCore));}
          if (updateHandlerObjectName.getKeyProperty("name").equals("transaction_logs_total_number")){
          Long transactionLogsTotalNumber = (Long) mbsc.getAttribute(updateHandlerObjectName, "Value");
            // file in bytes
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.updateHandler.transaction_logs_total_number",collectionName),
                    transactionLogsTotalNumber.doubleValue(), true, "Long", solrCore));}

          /* TODO: commits, autocommits, soft autocommits */

        }

        String[] searcherStrings = formatDomain(solrCore,"SEARCHER","searcher");
        for (String searcherString : searcherStrings) {

          ObjectName searcherObjectName = new ObjectName(String.format("%s:%s", solrCore, searcherString));
          String collectionName = String.format("%s_%s_%s",searcherObjectName.getKeyProperty("dom2"),searcherObjectName.getKeyProperty("dom3"),searcherObjectName.getKeyProperty("dom4"));
          logger.debug("Searcher Bean {}", searcherObjectName);
          if (searcherObjectName.getKeyProperty("name").equals("numDocs")) {
            Integer searcherNumDocs = (Integer) mbsc.getAttribute(searcherObjectName, "Value");
            solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.searcher.numDocs",collectionName), searcherNumDocs.doubleValue(), true, "Long", solrCore));
          }
          if (searcherObjectName.getKeyProperty("name").equals("maxDoc")) {
            Integer searcherMaxDoc = (Integer) mbsc.getAttribute(searcherObjectName, "Value");
            solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.searcher.maxDoc",collectionName), searcherMaxDoc.doubleValue(), true, "Long", solrCore));
          }
          if (searcherObjectName.getKeyProperty("name").equals("deletedDocs")) {
            Integer searcherDeletedDocs = (Integer) mbsc.getAttribute(searcherObjectName, "Value");
            solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.searcher.deletedDocs",collectionName), searcherDeletedDocs.doubleValue(), true, "Long", solrCore));
          }
          if (searcherObjectName.getKeyProperty("name").equals("warmupTime")) {
            Long searcherWarmupTime = (Long) mbsc.getAttribute(searcherObjectName, "Value");
            solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.searcher.warmupTime",collectionName), searcherWarmupTime.doubleValue(), true, "Long", solrCore));
          }


        }
          String[] indexStrings = formatDomain(solrCore,"INDEX");
        for (String indexString: indexStrings) {
          ObjectName indexObjectName = new ObjectName(String.format("%s:%s", solrCore, indexString));
          String collectionName = String.format("%s_%s_%s",indexObjectName.getKeyProperty("dom2"),indexObjectName.getKeyProperty("dom3"),indexObjectName.getKeyProperty("dom4"));
          if (indexObjectName.getKeyProperty("name").equals("sizeInBytes")) {
            Long searcherWarmupTime = (Long) mbsc.getAttribute(indexObjectName, "Value");
            solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.index.sizeInBytes",collectionName), searcherWarmupTime.doubleValue(), true, "Long", solrCore));
          }
        }


          addFilterClassMetrics(filterCacheClass, "filterCache", solrCore, solrCoreMetricsList, Long.class);
          addFilterClassMetrics(perSeqFilterCacheClass, "perSegFilter", solrCore, solrCoreMetricsList, Integer.class);
          addFilterClassMetrics(queryResultCacheCacheClass, "queryResultCache", solrCore, solrCoreMetricsList, Integer.class);
          addFilterClassMetrics(fieldValueCacheClass, "fieldValueCache", solrCore, solrCoreMetricsList, Long.class);
          addFilterClassMetrics(documentCacheClass, "documentCache", solrCore, solrCoreMetricsList, Long.class);

          addQueryMetrics("select", "QUERY", "/select", solrCore, solrCoreMetricsList);
          addQueryMetrics("update", "UPDATE", "/update", solrCore, solrCoreMetricsList);
          addQueryMetrics("query", "QUERY", "/query", solrCore, solrCoreMetricsList);
          addQueryMetrics("get", "QUERY", "/get", solrCore, solrCoreMetricsList);
          addQueryMetrics("luke", "ADMIN", "/admin/luke", solrCore, solrCoreMetricsList);
          addQueryMetrics("browse", "QUERY", "/browse", solrCore, solrCoreMetricsList);
          addQueryMetrics("export", "QUERY", "/export", solrCore, solrCoreMetricsList);


      }
    }
    return solrCoreMetricsList;
  }

  private String[] filterCores(String[] domains) {
    List<String> list = new ArrayList<>();
    if (domains != null && domains.length > 0) {
      for (String domain : domains) {
        logger.debug("Domains {}",domain);
        if (domain.startsWith("solr")) {
          list.add(domain);
        }
      }
    }
    return list.toArray(new String[0]);
  }

  private String[] formatDomain(String domain, String targetCategoryPattern) throws IOException, MalformedObjectNameException {

    ObjectName queryPattern = new ObjectName(String.format("%s:*",domain));
    String targetCorePatter = "core";


    Set<ObjectName> mbeans = mbsc.queryNames(queryPattern, null);
    List<String> list = new ArrayList<>();
    for (ObjectName mbean:mbeans) {
      String dom1 = mbean.getKeyProperty("dom1");
      String dom2 = mbean.getKeyProperty("dom2");
      String dom3 = mbean.getKeyProperty("dom3");
      String dom4 = mbean.getKeyProperty("dom4");
      String category = mbean.getKeyProperty("category");
      String name = mbean.getKeyProperty("name");

      if (targetCorePatter.equals(dom1) && targetCategoryPattern.equals(category)){
        String fqdn = String.format("dom1=%s,dom2=%s,dom3=%s,dom4=%s,category=%s,name=%s", dom1,dom2, dom3, dom4,category,name);
        logger.debug("Format Domain Name {}", fqdn);
        list.add(fqdn);
      }
    }
    return list.toArray(new String[0]);

  }
  private String[] formatDomain(String domain, String targetCategoryPattern, String targetScopePattern) throws IOException, MalformedObjectNameException {

    ObjectName queryPattern = new ObjectName(String.format("%s:*",domain));
    String targetCorePatter = "core";


    Set<ObjectName> mbeans = mbsc.queryNames(queryPattern, null);
    List<String> list = new ArrayList<>();
    for (ObjectName mbean:mbeans) {
      String dom1 = mbean.getKeyProperty("dom1");
      String dom2 = mbean.getKeyProperty("dom2");
      String dom3 = mbean.getKeyProperty("dom3");
      String dom4 = mbean.getKeyProperty("dom4");
      String category = mbean.getKeyProperty("category");
      String scope = mbean.getKeyProperty("scope");
      String name = mbean.getKeyProperty("name");

       if (targetCorePatter.equals(dom1) && targetCategoryPattern.equals(category) && targetScopePattern.equals(scope)){
         String fqdn = String.format("dom1=%s,dom2=%s,dom3=%s,dom4=%s,category=%s,scope=%s,name=%s", dom1,dom2, dom3, dom4,category,scope,name);
         logger.debug("Format Domain Name {}", fqdn);
         list.add(fqdn);
      }
      }
    return list.toArray(new String[0]);

  }

  private String formatDomain(String domain, String targetCategoryPattern, String targetScopePattern,String targetNamePattern) throws IOException, MalformedObjectNameException {

    ObjectName queryPattern = new ObjectName(String.format("%s:*",domain));
    String targetCorePatter = "core";


    Set<ObjectName> mbeans = mbsc.queryNames(queryPattern, null);
    //List<String> list = new ArrayList<>();
    for (ObjectName mbean:mbeans) {
      String dom1 = mbean.getKeyProperty("dom1");
      String dom2 = mbean.getKeyProperty("dom2");
      String dom3 = mbean.getKeyProperty("dom3");
      String dom4 = mbean.getKeyProperty("dom4");
      String category = mbean.getKeyProperty("category");
      String scope = mbean.getKeyProperty("scope");
      String name = mbean.getKeyProperty("name");

      if (targetCorePatter.equals(dom1) && targetCategoryPattern.equals(category) && targetScopePattern.equals(scope) && targetNamePattern.equals(name)){
        String fqdn = String.format("dom1=%s,dom2=%s,dom3=%s,dom4=%s,category=%s,scope=%s,name=%s", dom1,dom2, dom3, dom4,category,scope,name);
        logger.debug("Format Domain Name {}", fqdn);
        //list.add(fqdn);
        return fqdn;
      }
    }
    return null;

  }

  private void addToMetricListIfNotNUll(String metricName, Long data, List<SolrMetricsData> solrNodeMetricsList) {
    if (data != null) {
      solrNodeMetricsList.add(new SolrMetricsData(metricName, data.doubleValue(), true, "Long", null));
    }
  }

  private Object getOrSkipJMXObjectAttribute(String objectName, String attributeName) throws Exception {
    try {
      return mbsc.getAttribute(new ObjectName(objectName), attributeName);
    } catch (AttributeNotFoundException | InstanceNotFoundException e) {
      // skip
      logger.debug("Cannot load: {}", e.getMessage());
    }
    return null;
  }

  private void addValueIfNotZero(List <Double> percentileList,String percentile_name,Double percentileValue)
  {

      if (percentileValue != 0){
        percentileList.add(percentileValue);
      }
      else{
        logger.debug("Cannot add {}  to list : value {}",percentile_name,percentileValue);
      }

  }

  private <T extends Number> void addFilterClassMetrics(String filterClassName, String type, String solrCore, List<SolrMetricsData> solrCoreMetricsList, Class<T> clazz)
    throws Exception {
    try {

      String filterCacheObjectStrings = formatDomain(solrCore,"CACHE","searcher",type);
         if (filterCacheObjectStrings != null) {
           ObjectName filterCacheObjectName = new ObjectName(String.format("%s:%s", solrCore, filterCacheObjectStrings));
           String collectionName = String.format("%s_%s_%s",filterCacheObjectName.getKeyProperty("dom2"),filterCacheObjectName.getKeyProperty("dom3"),filterCacheObjectName.getKeyProperty("dom4"));
           Object attributeValue = mbsc.getAttribute(filterCacheObjectName, "Value");
           logger.debug("Add Filter Class Metrics => attributeValue {}", attributeValue);
           HashMap<?, ?> attributesMap = (HashMap<?, ?>) attributeValue;
           // Map<String, Object> attributesMap = parseAttributeValue(attributeValue);
           logger.debug("Add Filter Class Metrics => attributeMap {}", attributesMap);
           Float cacheHitRatio = (Float) attributesMap.get("hitratio");
           T cacheSize = (T) attributesMap.get("size");
           Long cacheWarmupTime = (Long) attributesMap.get("warmupTime");

           solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.cache.%s.hitratio", collectionName,type), cacheHitRatio.doubleValue(), true, "Double", solrCore));
           solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.cache.%s.size", collectionName,type), cacheSize.doubleValue(), true, "Long", solrCore));
           solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.cache.%s.warmupTime", collectionName,type), cacheWarmupTime.doubleValue(), true, "Long", solrCore));
         }
      } catch (Exception e) {
      // skip
      logger.error("{} - {}", type, e.getMessage());
    }

  }

  private static Map<String, Object> parseAttributeValue(String attributeValue) {
    Map<String, Object> attributesMap = new HashMap<>();

    Pattern pattern = Pattern.compile("(\\w+)=(\\d+)");
    Matcher matcher = pattern.matcher(attributeValue);

    while (matcher.find()) {
      String key = matcher.group(1);
      Object value = (Object) (matcher.group(2));
      attributesMap.put(key, value);
    }

    return attributesMap;
  }

  private void addQueryMetrics(String queryName, String targetCategoryPattern, String scope, String solrCore, List<SolrMetricsData> solrCoreMetricsList)
  throws Exception {

    try {
      List<Double> percentileData = new ArrayList<>();
      String[] filterCacheObjectStrings = formatDomain(solrCore, targetCategoryPattern, scope);
      for (String filterCacheObjectString : filterCacheObjectStrings) {

        ObjectName queryHandlerObjectName = new ObjectName(String.format("%s:%s", solrCore, filterCacheObjectString));
        String collectionName = String.format("%s_%s_%s",queryHandlerObjectName.getKeyProperty("dom2"),queryHandlerObjectName.getKeyProperty("dom3"),queryHandlerObjectName.getKeyProperty("dom4"));
        if (queryHandlerObjectName.getKeyProperty("name").equals("requestTimes")) {
          Double avgRequestsPerSec = (Double) mbsc.getAttribute(queryHandlerObjectName, "MeanRate");
          Long requests = (Long) mbsc.getAttribute(queryHandlerObjectName, "Count");
          Double fiftyPercentile = (Double) mbsc.getAttribute(queryHandlerObjectName, "50thPercentile");
          Double seventyFivePercentile = (Double) mbsc.getAttribute(queryHandlerObjectName, "75thPercentile");
          Double ninetyFivePercentile = (Double) mbsc.getAttribute(queryHandlerObjectName, "95thPercentile");
          Double ninetyEightPercentile = (Double) mbsc.getAttribute(queryHandlerObjectName, "98thPercentile");
          Double ninetyNinePercentile = (Double) mbsc.getAttribute(queryHandlerObjectName, "99thPercentile");
          Double nineHundredNinetyNinePercentile = (Double) mbsc.getAttribute(queryHandlerObjectName, "999thPercentile");

          Double fiveMinRateReqsPerSecond = (Double) mbsc.getAttribute(queryHandlerObjectName, "FiveMinuteRate");
          Double fifteenMinRateReqsPerSecond = (Double) mbsc.getAttribute(queryHandlerObjectName, "FifteenMinuteRate");

          addValueIfNotZero(percentileData, "50thPercentile", fiftyPercentile);
          addValueIfNotZero(percentileData, "75thPercentile", seventyFivePercentile);
          addValueIfNotZero(percentileData, "95thPercentile", ninetyFivePercentile);
          addValueIfNotZero(percentileData, "98thPercentile", ninetyEightPercentile);
          addValueIfNotZero(percentileData, "99thPercentile", ninetyNinePercentile);
          addValueIfNotZero(percentileData, "999thPercentile", nineHundredNinetyNinePercentile);

          Double medianRequestTime = MedianCalculator.calculateMedian(percentileData);

          String requestsCacheOjectString = formatDomain(solrCore, targetCategoryPattern, scope, "totalTime");
          ObjectName requestsObject = new ObjectName(String.format("%s:%s", solrCore, requestsCacheOjectString));
          Long totalTime = (Long) mbsc.getAttribute(requestsObject, "Count");
          Double avgTimePerRequest = 0.0;

          if (totalTime != 0 && requests != 0) {
            avgTimePerRequest = (double) totalTime / requests;

          }
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.avgRequestsPerSec", collectionName,queryName), avgRequestsPerSec, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.5minRateReqsPerSecond", collectionName,queryName), fiveMinRateReqsPerSecond, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.15minRateReqsPerSecond", collectionName,queryName), fifteenMinRateReqsPerSecond, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.requests", collectionName,queryName), requests.doubleValue(), true, "Long", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.medianRequestTime", collectionName,queryName), medianRequestTime, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.avgTimePerRequest", collectionName,queryName), avgTimePerRequest, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.75thPcRequestTime", collectionName,queryName), seventyFivePercentile, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.95thPcRequestTime", collectionName,queryName), ninetyFivePercentile, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.99thPcRequestTime", collectionName,queryName), ninetyNinePercentile, true, "Double", solrCore));
          solrCoreMetricsList.add(new SolrMetricsData(String.format("solr.admin.mbeans.%s.queryHandler.%s.999thPcRequestTime", collectionName,queryName), nineHundredNinetyNinePercentile, true, "Double", solrCore));
        }


      }
    } catch (Exception e){

      logger.error("{} - {}", queryName, e.getMessage());
    }
  }

  private void addNetworkMetrics(List<SolrMetricsData> solrNodeMetricsList) {
    Runtime rt = Runtime.getRuntime();
    List<String> supportedConnectionStates = supportedTcpConnectionStates();
    String command = createNetstatCommand(supportedConnectionStates);
    String[] commands = {"/bin/sh", "-c", command};
    Process proc = null;
    try {
      proc = rt.exec(commands);
      BufferedReader br = new BufferedReader(
        new InputStreamReader(proc.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        String[] splitted = line.trim().split(" ");
        if (splitted.length == 2) {
          if (supportedConnectionStates.contains(splitted[1])) {
            Integer value = Integer.parseInt(splitted[0]);
            SolrMetricsData networkData = new SolrMetricsData("solr.network.connections." + splitted[1], value.doubleValue(), true, "Long", null);
            solrNodeMetricsList.add(networkData);
          }
        }
      }
    } catch (IOException e) {
      logger.error("Error during execute command: ", StringUtils.join(commands, " "), e);
    }
  }

  private String createNetstatCommand(List<String> supportedConnectionStates) {
    return "netstat -nat | grep :" + solrPort+ " | egrep \"" +
      StringUtils.join(supportedConnectionStates, "|") + "\" | awk '{print$6}' | sort | uniq -c | sort -n";
  }

  private List<String> supportedTcpConnectionStates() {
    return Arrays.asList("ESTABLISHED", "LISTENING", "CLOSE_WAIT", "TIME_WAIT");
  }
}
