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
public class MorningstarHealthCheck implements HealthIndicator {
  @Value("${morningstar.api.url}")
  private String morningStarUrl;

  @Autowired
  @Qualifier("externalSyncClient")
  private CloseableHttpClient httpClient;

  @Override
  public Health health() {
    HttpHead head = new HttpHead(morningStarUrl);

    try (CloseableHttpResponse response = httpClient.execute(head)) {
      int httpStatusCode = response.getStatusLine().getStatusCode();

      // Quick and dirty
      // Useful for two things:
      //    1. Does the service respond
      //    2. When onboarding to PCF, is the firewall open
      if (httpStatusCode == 200) {
        return Health.up()
          .withDetail("url", morningStarUrl)
          .build();
      }

      String errorMessage = EntityUtils.toString(response.getEntity());

      return Health.down()
          .withDetail("MorningstarError",
            String.format("Error code: %s. Error Message: %s", httpStatusCode, errorMessage))
          .withDetail("url", morningStarUrl)
          .build();
    } catch (IOException e) {
      return Health.down()
        .withDetail("MorningstarError", "Morningstar dependency not available")
        .withDetail("Error Message: ", e.getMessage())
        .withDetail("url", morningStarUrl)
        .build();
    }
  }
}
