package com.banking.account.soap;

import jakarta.xml.bind.annotation.*;

import java.math.BigDecimal;

@XmlRootElement(name = "GetAccountDetailsResponse", namespace = "http://banking.com/account/soap")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://banking.com/account/soap")
public class GetAccountDetailsResponse {
    private String accountNumber;
    private BigDecimal balance;
    private String status;
    private String currency;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
