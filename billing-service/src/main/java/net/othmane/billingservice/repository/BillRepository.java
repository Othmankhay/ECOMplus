package net.othmane.billingservice.repository;

import net.othmane.billingservice.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository  extends JpaRepository<Bill, Long> {
    List<Bill> findByCustomerId(Long customerId);
}