package cl.duoc.fullstack.discovery_server_m12;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerM12Application {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerM12Application.class, args);
    }

}
