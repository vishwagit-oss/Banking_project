package com.banking.account.config;

import com.banking.account.entity.BankUser;
import com.banking.account.repository.BankUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultAdminLoader {

    @Bean
    public ApplicationRunner createDefaultAdmin(BankUserRepository repo, PasswordEncoder encoder) {
        return args -> {
            var adminOpt = repo.findByUsername("admin");
            String hash = encoder.encode("admin123");
            if (adminOpt.isEmpty()) {
                BankUser admin = BankUser.builder()
                        .username("admin")
                        .passwordHash(hash)
                        .customerId(null)
                        .role(BankUser.Role.ADMIN)
                        .build();
                repo.save(admin);
            } else {
                // Ensure admin password is always correct (e.g. after manual DB edit or wrong hash)
                BankUser admin = adminOpt.get();
                admin.setPasswordHash(hash);
                repo.save(admin);
            }
        };
    }
}
