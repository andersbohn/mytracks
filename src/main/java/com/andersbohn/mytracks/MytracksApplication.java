package com.andersbohn.mytracks;

import com.andersbohn.mytracks.config.AppProperties;
import com.andersbohn.mytracks.security.UserAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({UserAuthProperties.class, AppProperties.class})
public class MytracksApplication {

  public static void main(String[] args) {
    SpringApplication.run(MytracksApplication.class, args);
  }
}
