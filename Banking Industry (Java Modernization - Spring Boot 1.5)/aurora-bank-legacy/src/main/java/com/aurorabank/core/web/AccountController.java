package com.aurorabank.core.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurorabank.core.domain.Account;
import com.aurorabank.core.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Account getById(@PathVariable Long id) {
        return accountService.getById(id);
    }

    @RequestMapping(value = "/customer/{customerId}", method = RequestMethod.GET)
    public List<Account> getForCustomer(@PathVariable Long customerId) {
        return accountService.getForCustomer(customerId);
    }

    // POST /api/accounts?customerId=1&type=SAVINGS&currency=EUR
    @RequestMapping(method = RequestMethod.POST)
    public Account open(@RequestParam Long customerId,
                        @RequestParam String type,
                        @RequestParam String currency) {
        return accountService.openAccount(customerId, type, currency);
    }

    // GET /api/accounts/search?name=smith
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public List<Map<String, Object>> search(@RequestParam String name) {
        return accountService.searchByCustomerName(name);
    }

    // POST /api/accounts/{id}/fee?amount=5.00
    @RequestMapping(value = "/{id}/fee", method = RequestMethod.POST)
    public void chargeFee(@PathVariable Long id, @RequestParam double amount) {
        accountService.chargeFee(id, amount);
    }
}
