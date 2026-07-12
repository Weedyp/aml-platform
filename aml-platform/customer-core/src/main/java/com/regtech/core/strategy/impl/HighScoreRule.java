package com.regtech.core.strategy.impl;

import com.regtech.core.strategy.KycResult;
import com.regtech.core.strategy.KycRuleStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(3)
public class HighScoreRule implements KycRuleStrategy {

    @Override
    public Optional<KycResult> evaluate(String countryCode, int currentScore) {
        if (currentScore > 75) {
            return Optional.of(new KycResult("MANUAL_REVIEW", currentScore, "High Initial Risk Score provided by bank"));
        }
        return Optional.empty();
    }
}