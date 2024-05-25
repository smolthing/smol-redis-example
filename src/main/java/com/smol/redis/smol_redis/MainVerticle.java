package com.smol.redis.smol_redis;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;

public class MainVerticle extends AbstractVerticle {
  private static final int PORT = 8000;
  private RedisAPI redisAPI;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    RedisOptions options = new RedisOptions().addConnectionString("redis://localhost:6379");
    Redis redis = Redis.createClient(vertx, options);
    redisAPI = RedisAPI.api(redis);

    Router router = Router.router(vertx);
    GetDataHandler getDataHandler = new GetDataHandler(redisAPI);
    router.get("/data").handler(getDataHandler);

    vertx.createHttpServer().requestHandler(router).listen(PORT, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println(String.format("HTTP server started on port %s", PORT));
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
