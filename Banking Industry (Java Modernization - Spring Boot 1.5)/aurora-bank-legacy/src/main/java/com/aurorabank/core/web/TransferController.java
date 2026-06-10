package com.aurorabank.core.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.aurorabank.core.domain.Transfer;
import com.aurorabank.core.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody TransferRequest request) {
        try {
            Transfer transfer = transferService.transfer(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    request.getMemo());
            return ResponseEntity.ok(transfer);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    public static class TransferRequest {
        private Long fromAccountId;
        private Long toAccountId;
        private double amount;
        private String memo;

        public Long getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }

        public Long getToAccountId() { return toAccountId; }
        public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
    }
}
