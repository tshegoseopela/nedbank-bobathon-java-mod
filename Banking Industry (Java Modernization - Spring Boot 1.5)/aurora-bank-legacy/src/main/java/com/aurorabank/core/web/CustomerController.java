package com.aurorabank.core.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.aurorabank.core.domain.Customer;
import com.aurorabank.core.repo.CustomerRepository;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @RequestMapping(method = RequestMethod.GET)
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Customer getById(@PathVariable Long id) {
        return customerRepository.findOne(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Customer create(@RequestBody Customer customer) {
        customer.setStatus("ACTIVE");
        return customerRepository.save(customer);
    }
}
