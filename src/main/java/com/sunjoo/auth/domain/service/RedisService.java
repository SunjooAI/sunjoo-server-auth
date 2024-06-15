package com.sunjoo.auth.domain.service;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public interface RedisService {
    void setValues(String key, String value);
    void setValues(String key, String value, Duration duration);
    String getValue(String key);
    void deleteValue(String key);
}
