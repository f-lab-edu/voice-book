package com.sj.voicebook.global;

public enum RedisPrefix {
    EMAIL_AUTH_PREFIX("email:auth:"),
    EMAIL_RATE_LIMIT_PREFIX("email:rate:"),
    EMAIL_ATTEMPT_PREFIX("email:attempt:"),
    EMAIL_VERIFIED_PREFIX("email:verified:");

    public final String prefix;
    RedisPrefix(String prefix) {
        this.prefix = prefix;
    }
}
