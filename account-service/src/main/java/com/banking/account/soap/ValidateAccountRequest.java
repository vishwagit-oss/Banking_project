package com.banking.account.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ValidateAccountRequest", namespace = "http://banking.com/account/soap")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://banking.com/account/soap")
public class ValidateAccountRequest {
    private String accountNumber;

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
