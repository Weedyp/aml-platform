package com.regtech.core.strategy;

import java.util.Optional;

public interface KycRuleStrategy {

    /**
     * Evaluates the customer data against a specific AML regulation.
     * @return Optional containing the penalty if triggered, or empty if it passes.
     */
    Optional<KycResult> evaluate(String countryCode, int currentScore);
}
