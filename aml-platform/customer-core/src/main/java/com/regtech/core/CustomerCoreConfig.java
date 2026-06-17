package com.regtech.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.regtech.core")
public class CustomerCoreConfig {
    // This class acts as the foundational marker for the customer-core module.
    // ArchUnit will now detect this class and successfully validate our architecture rules!
}
