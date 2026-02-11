package com.banking.transaction.soap;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "ExecuteTransferResponse", namespace = "http://banking.com/transaction/soap")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://banking.com/transaction/soap")
public class ExecuteTransferResponse {
    private String transactionId;
    private String status;

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
