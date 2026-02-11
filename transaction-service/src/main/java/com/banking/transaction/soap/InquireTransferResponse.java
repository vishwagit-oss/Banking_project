package com.banking.transaction.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "InquireTransferResponse", namespace = "http://banking.com/transaction/soap")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://banking.com/transaction/soap")
public class InquireTransferResponse {
    private boolean valid;
    private String message;

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
