package com.banking.account.soap;

import com.banking.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
@RequiredArgsConstructor
public class AccountSoapEndpoint {

    private static final String NAMESPACE_URI = "http://banking.com/account/soap";

    private final AccountService accountService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ValidateAccountRequest")
    @ResponsePayload
    public ValidateAccountResponse validateAccount(@RequestPayload ValidateAccountRequest request) {
        boolean valid = accountService.validateAccount(request.getAccountNumber());
        ValidateAccountResponse response = new ValidateAccountResponse();
        response.setValid(valid);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAccountDetailsRequest")
    @ResponsePayload
    public GetAccountDetailsResponse getAccountDetails(@RequestPayload GetAccountDetailsRequest request) {
        GetAccountDetailsResponse response = new GetAccountDetailsResponse();
        accountService.getAccountEntityByNumber(request.getAccountNumber()).ifPresent(account -> {
            response.setAccountNumber(account.getAccountNumber());
            response.setBalance(account.getBalance());
            response.setStatus(account.getStatus().name());
            response.setCurrency(account.getCurrency());
        });
        return response;
    }
}
