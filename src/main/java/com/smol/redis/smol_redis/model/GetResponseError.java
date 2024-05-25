package com.smol.redis.smol_redis.model;

import io.vertx.core.json.JsonObject;
import lombok.Builder;

@Builder
public record GetResponseError(Error error) {
  public JsonObject mapToJson() {
    return JsonObject.mapFrom(this);
  }
}
