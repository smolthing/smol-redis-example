package com.smol.redis.smol_redis.model;

import lombok.Builder;

@Builder
public record Error(int code, String message) {

}
