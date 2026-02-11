package com.banking.account.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ValidateAccountResponse", namespace = "http://banking.com/account/soap")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://banking.com/account/soap")
public class ValidateAccountResponse {
    private boolean valid;

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
