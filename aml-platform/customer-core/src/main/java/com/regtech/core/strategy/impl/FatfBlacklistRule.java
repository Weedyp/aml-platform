package com.regtech.core.strategy.impl;

import com.regtech.core.strategy.KycResult;
import com.regtech.core.strategy.KycRuleStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Order(1) // Highest priority
public class FatfBlacklistRule implements KycRuleStrategy {

    @Override
    public Optional<KycResult> evaluate(String countryCode, int currentScore) {
        if (List.of("KP", "IR", "SY").contains(countryCode)) {
            return Optional.of(new KycResult("BLOCKED", 100, "FATF Blacklist Match: High-Risk Jurisdiction"));
        }
        return Optional.empty();
    }
}