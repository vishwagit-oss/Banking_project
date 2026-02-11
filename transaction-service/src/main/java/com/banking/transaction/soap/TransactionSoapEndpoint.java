package com.banking.transaction.soap;

import com.banking.transaction.dto.TransferRequest;
import com.banking.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class TransactionSoapEndpoint {

    private static final String NAMESPACE_URI = "http://banking.com/transaction/soap";

    private final TransactionService transactionService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ExecuteTransferRequest")
    @ResponsePayload
    public ExecuteTransferResponse executeTransfer(@RequestPayload ExecuteTransferRequest request) {
        ExecuteTransferResponse response = new ExecuteTransferResponse();
        try {
            var result = transactionService.transfer(TransferRequest.builder()
                    .fromAccountNumber(request.getFromAccountNumber())
                    .toAccountNumber(request.getToAccountNumber())
                    .amount(request.getAmount())
                    .reference(request.getReference())
                    .build());
            response.setTransactionId(result.getId());
            response.setStatus(result.getStatus().name());
        } catch (Exception e) {
            response.setTransactionId(null);
            response.setStatus("FAILED");
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "InquireTransferRequest")
    @ResponsePayload
    public InquireTransferResponse inquireTransfer(@RequestPayload InquireTransferRequest request) {
        InquireTransferResponse response = new InquireTransferResponse();
        try {
            transactionService.validateTransfer(request.getFromAccountNumber(),
                    request.getToAccountNumber(), request.getAmount());
            response.setValid(true);
        } catch (Exception e) {
            response.setValid(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
