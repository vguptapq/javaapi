package com.tdameritrade.microservice.xray.health;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class IdsHealthCheck implements HealthIndicator {
  @Value("${ids.url}")
  private String idsUrl;

  @Autowired
  @Qualifier("internalSyncClient")
  private CloseableHttpClient httpClient;

  @Override
  public Health health() {
    HttpHead head = new HttpHead(String.format("%s/%s", idsUrl, "instrumentdata/instruments-v1"));

    try (CloseableHttpResponse response = httpClient.execute(head)) {
      int httpStatusCode = response.getStatusLine().getStatusCode();

      // Quick and dirty. This service doesn't support HEAD requests.
      // Useful for two things:
      //    1. Does the service respond
      //    2. When onboarding to PCF, is the firewall open
      if (httpStatusCode == 405) {
        return Health.up()
          .withDetail("url", idsUrl)
          .build();
      }

      String errorMessage = EntityUtils.toString(response.getEntity());

      return Health.down()
        .withDetail("idsError",
          String.format("Error code: %s. Error Message: %s", httpStatusCode, errorMessage))
        .withDetail("url", idsUrl)
        .build();
    } catch (IOException ex) {
      return Health.down()
        .withDetail("idsCannotConnect", "ids is not listening")
        .withDetail("Error Message: ", ex.getMessage())
        .withDetail("url", idsUrl)
        .build();
    }
  }
}
