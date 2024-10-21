package fpt.capstone.iContact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IContactApplication {

	public static void main(String[] args) {
		SpringApplication.run(IContactApplication.class, args);
	}

}
