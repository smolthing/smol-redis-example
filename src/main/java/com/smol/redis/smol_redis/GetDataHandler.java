package com.smol.redis.smol_redis;

import com.smol.redis.smol_redis.model.Data;
import com.smol.redis.smol_redis.model.Error;
import com.smol.redis.smol_redis.model.GetResponse;
import com.smol.redis.smol_redis.model.GetResponseError;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisAPI;

public class GetDataHandler implements Handler<RoutingContext> {
  private static final String CACHE_EXPIRY_IN_SECOND = "10";
  private static final String QUERY_PARAM_KEY = "key";
  private static final int INTERNAL_SERVER_ERROR_CODE = 500;
  private final RedisAPI redisAPI;
  public GetDataHandler(RedisAPI redisAPI) {
    this.redisAPI = redisAPI;
  }

  @Override
  public void handle(RoutingContext context) {
    final var queryKey = context.queryParams().get(QUERY_PARAM_KEY);

    redisAPI.get(queryKey, response -> {
      if (response.succeeded() && response.result() != null) {
        System.out.println(String.format("Cached hit %s:%s", queryKey, response.result()));

        sendResponse(context, getResponseJsonObject(
          queryKey, response.result().toString(), true));
      } else {
        fetchFromServiceA(queryKey)
          .onSuccess(value -> {
            sendResponse(context, getResponseJsonObject(queryKey, value, false));
          })
          .onFailure(error -> {
            System.err.println("Failed to fetch data from ServiceA: " + error.getMessage());
            sendErrorResponse(context, error.getMessage());
        });
      }

    });
  }

  private Future<String> fetchFromServiceA(String key) {
    // pretend to fetch from service a
    final var value = "true";
    Promise<String> promise = Promise.promise();

    redisAPI.setex(key, CACHE_EXPIRY_IN_SECOND, value, setRes -> {
      if (setRes.succeeded()) {
        System.out.println(String.format("Cached %s:%s successfully", key, value));
      } else {
        System.err.println(String.format("Failed to cache %s:%s %s ", key, value,
          setRes.cause().getMessage()));
      }
      promise.complete(value);
    });

    return promise.future();
  }

  private void sendResponse(RoutingContext context, JsonObject jsonResponse) {
    context.response()
      .putHeader("content-type", "application/json")
      .end(jsonResponse.encode());
  }

  private void sendErrorResponse(RoutingContext context, String errorMessage) {
    context.response()
      .setStatusCode(INTERNAL_SERVER_ERROR_CODE)
      .putHeader("content-type", "application/json")
      .end(getResponseErrorString(INTERNAL_SERVER_ERROR_CODE, errorMessage));
  }

  private JsonObject getResponseJsonObject(String key, String value, boolean isCached) {
    return GetResponse.builder()
      .data(Data.builder()
        .key(key)
        .value(Boolean.parseBoolean(value))
        .build())
      .isCached(isCached)
      .build()
      .mapToJson();
  }

  private String getResponseErrorString(int code, String message) {
    return GetResponseError.builder()
      .error(Error.builder()
        .code(code)
        .message(message)
        .build())
      .build()
      .toString();
  }
}
