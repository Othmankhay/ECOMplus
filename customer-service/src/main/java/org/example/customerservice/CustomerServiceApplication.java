package org.example.customerservice;

import org.example.customerservice.entities.Customer;
import org.example.customerservice.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient  // Added for Eureka client registration
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(CustomerRepository customerRepository) {
        return args -> {
            // Fixed: Removed duplicate .name() calls and used proper field names
            customerRepository.save(
                    Customer.builder()
                            .firstName("Othman")  // Assuming firstName field
                            .lastName("Khay")       // Assuming lastName field
                            .email("othmankhay@example.com")
                            .build()
            );

            customerRepository.save(
                    Customer.builder()
                            .firstName("Ali")
                            .lastName("Badr")
                            .email("ali@example.com")
                            .build()
            );

            customerRepository.findAll().forEach(System.out::println);
        };
    }
}