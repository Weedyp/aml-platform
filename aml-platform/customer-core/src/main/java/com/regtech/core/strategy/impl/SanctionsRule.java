package com.regtech.core.strategy.impl;

import com.regtech.core.strategy.KycResult;
import com.regtech.core.strategy.KycRuleStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Order(2)
public class SanctionsRule implements KycRuleStrategy {

    @Override
    public Optional<KycResult> evaluate(String countryCode, int currentScore) {
        if (List.of("RU", "BY").contains(countryCode)) {
            return Optional.of(new KycResult("MANUAL_REVIEW", 85, "Sanctions Watchlist Match"));
        }
        return Optional.empty();
    }
}