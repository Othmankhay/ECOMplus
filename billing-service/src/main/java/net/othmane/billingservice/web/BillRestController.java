package net.othmane.billingservice.web;

import net.othmane.billingservice.entities.Bill;
import net.othmane.billingservice.feign.CustomerRestClient;
import net.othmane.billingservice.feign.ProductRestClient;
import net.othmane.billingservice.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BillRestController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private CustomerRestClient customerRestClient;

    @Autowired
    private ProductRestClient productRestClient;

    // 1. GET /bills - Liste TOUTES les factures
    @GetMapping("/bills")
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    // 2. GET /bills/{id} - Récupère une facture par ID
    @GetMapping("/bills/{id}")
    public Bill getBill(@PathVariable Long id) {
        Bill bill = billRepository.findById(id).get();

        // Récupère le client
        bill.setCustomer(customerRestClient.getCustomerById(bill.getCustomerId()));

        // Récupère les produits
        bill.getProductItems().forEach(item -> {
            item.setProduct(productRestClient.getProductById(item.getProductId()));
        });

        return bill;
    }

    // 3. POST /bills - Crée une nouvelle facture
    @PostMapping("/bills")
    public Bill createBill(@RequestBody Bill bill) {
        if (bill.getBillingDate() == null) {
            bill.setBillingDate(new java.util.Date());
        }
        return billRepository.save(bill);
    }

    // 4. DELETE /bills/{id} - Supprime une facture
    @DeleteMapping("/bills/{id}")
    public void deleteBill(@PathVariable Long id) {
        billRepository.deleteById(id);
    }
}