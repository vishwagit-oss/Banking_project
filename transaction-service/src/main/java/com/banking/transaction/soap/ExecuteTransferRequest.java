package com.banking.transaction.soap;

import jakarta.xml.bind.annotation.*;

import java.math.BigDecimal;

@XmlRootElement(name = "ExecuteTransferRequest", namespace = "http://banking.com/transaction/soap")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://banking.com/transaction/soap")
public class ExecuteTransferRequest {
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String reference;

    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }
    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
