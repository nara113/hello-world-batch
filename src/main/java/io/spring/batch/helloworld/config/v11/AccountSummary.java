package io.spring.batch.helloworld.config.v11;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountSummary {
    private int id;
    private String accountNumber;
    private double currentBalance;
}
