package com.sj.voicebook.global.util;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    // Key(이메일), Value(인증번호), 유효시간(분)
    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        Duration expireDuration = Duration.ofMinutes(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public String getData(String key) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void deleteData(String key) {
        stringRedisTemplate.delete(key);
    }

    public void setDataExpireSeconds(String key, String value, long seconds) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set(key, value, Duration.ofSeconds(seconds));
    }
    public void increment(String key, long expireMinutes) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        Long count = ops.increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofMinutes(expireMinutes));
        }
    }
}