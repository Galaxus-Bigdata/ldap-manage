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
package org.apache.ambari.solr.metrics;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.apache.ambari.solr.metrics"},
  exclude = {
    JmxAutoConfiguration.class
})
@PropertySource(value={"file:/usr/hdp/current/solr-server/metrics/conf/infra-solr-metrics.properties"}, ignoreResourceNotFound = true)
public class SolrMetricsApplication {

  public static void main(String[] args) throws Exception {
    String pidFile = System.getenv("PID_FILE") == null ? "infra-solr-metrics.pid" : System.getenv("PID_FILE");
    new SpringApplicationBuilder(SolrMetricsApplication.class)
      .bannerMode(Banner.Mode.OFF)
      .listeners(new ApplicationPidFileWriter(pidFile))
      .run(args);
  }
}

