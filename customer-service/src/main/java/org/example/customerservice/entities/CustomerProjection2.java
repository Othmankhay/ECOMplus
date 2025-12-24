package org.example.customerservice.entities;

import org.springframework.data.rest.core.config.Projection;

@Projection(name="Email",types = Customer.class)
public interface CustomerProjection2 {
    String getEmail();
}
