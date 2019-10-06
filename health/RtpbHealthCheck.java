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
public class RtpbHealthCheck implements HealthIndicator {
  @Value("${rtpb.url}")
  private String rtpbUrl;

  @Autowired
  @Qualifier("internalSyncClient")
  private CloseableHttpClient httpClient;

  @Override
  public Health health() {
    HttpHead getWsdl = new HttpHead(String.format("%s.wsdl", rtpbUrl));

    try (CloseableHttpResponse response = httpClient.execute(getWsdl)) {
      int httpStatusCode = response.getStatusLine().getStatusCode();

      // Quick and dirty
      // Useful for two things:
      //    1. Does the service respond
      //    2. When onboarding to PCF, is the firewall open
      if (httpStatusCode == 200) {
        return Health.up()
          .withDetail("url", rtpbUrl)
          .build();
      }

      String errorMessage = EntityUtils.toString(response.getEntity());

      return Health.down()
        .withDetail("rtpbError",
          String.format("Error code: %s. Error Message: %s", httpStatusCode, errorMessage))
        .withDetail("url", rtpbUrl)
        .build();
    } catch (IOException e) {
      return Health.down()
        .withDetail("rtpbCannotConnect", "RTPB not listening")
        .withDetail("Error Message: ", e.getMessage())
        .withDetail("url", rtpbUrl)
        .build();
    }
  }
}
