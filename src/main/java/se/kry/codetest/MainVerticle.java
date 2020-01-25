package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.domain.ServiceInstance;
import se.kry.codetest.migrate.DBMigration;
import se.kry.codetest.service.DBService;
import se.kry.codetest.util.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private HashMap<String, String> services = new HashMap<>();
    //TODO use this
    private DBConnector connector;
    private BackgroundPoller poller = new BackgroundPoller();
    private DBMigration migration = new DBMigration();
    private DBService dbService = null;

    @Override
    public void start(Future<Void> startFuture) {
        connector = new DBConnector(vertx);
        dbService = new DBService(connector);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        initilizeDB(connector);
        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(dbService));
        setRoutes(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });

    }

    private void initilizeDB(DBConnector connector) {

        migration.migrate(connector);


    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());

        router.get("/service").handler(req -> {
            String serviceName = req.queryParam("service").get(0);
            dbService.getByName(serviceName).setHandler(futureResponse -> {

                if (futureResponse.succeeded()) {
                    if (futureResponse.result() != null) {
                        JsonObject jsonService = new JsonObject()
                                .put("name", futureResponse.result().getName())
                                .put("status", futureResponse.result().getStatus());
                        req.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(200)
                                .end(jsonService.encode());
                    } else {
                        JsonObject jsonObject = new JsonObject().put("error", "No service found for " + serviceName);
                        req.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(404)
                                .end(jsonObject.encode());
                    }
                }
            });
        });


        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            ServiceInstance serviceInstance = new ServiceInstance(jsonBody.getString("url"),
                    jsonBody.getString("name"));
            dbService.insert(serviceInstance).setHandler(done -> {
                if (done.succeeded()) {
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end("OK");
                } else {
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .setStatusCode(400)
                            .end();
                }
            });

        });


        router.delete("/service/:serviceName").handler(req->{
            String serviceName=req.pathParam("serviceName");

            serviceExists(serviceName).setHandler(handler->{
                if(handler.succeeded()){
                    if(handler.result()){
                        dbService.deleteByName(serviceName).setHandler(done->{
                            if(done.succeeded()){
                                req.response().setStatusCode(204).end();
                            }else{
                                req.response().setStatusCode(500)
                                        .end(new JsonObject().put("message","Internal Error Occured").encode());
                            }
                        });
                    }else{
                        JsonObject jsonObject = new JsonObject().put("message","No service found with name "+serviceName);
                        req.response()
                                .setStatusCode(404)
                                .putHeader("content-type","application/json")
                                .end(jsonObject.encode());
                    }
                }
            });
    });

    }


    private Future<Boolean> serviceExists(String serviceName) {
        Future<Boolean> serviceInstanceFuture = Future.future();
        dbService.getByName(serviceName).setHandler(done->{
            if(done.succeeded()){
                if(done.result()!=null){
                    serviceInstanceFuture.complete(true);
                }else{
                    serviceInstanceFuture.complete(false);
                }
            }else{
                serviceInstanceFuture.fail("Unable fetch service");
            }
        });
        return serviceInstanceFuture;
    }
}



