package com.smol.redis.smol_redis.model;

import lombok.Builder;

@Builder
public record Data(String key, boolean value) {

}
