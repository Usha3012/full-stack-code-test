package se.kry.codetest.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import se.kry.codetest.DBConnector;
import se.kry.codetest.domain.ServiceInstance;
import se.kry.codetest.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class DBService {
    private DBConnector connector;
    public DBService(DBConnector connector){
        this.connector = connector;
    }
    public Future<ServiceInstance> insert(ServiceInstance serviceInstance){
        Future<ServiceInstance> futureServiceInstance = Future.future();
        JsonArray params = new JsonArray().add(serviceInstance.getUrl()).add(serviceInstance.getName());
        connector.query("INSERT INTO services(url,name)values(?,?)",params).setHandler(done->{
            if(done.succeeded()){
                Logger.log("Successfully created service "+serviceInstance);
                futureServiceInstance.complete(serviceInstance);
            }else if(done.failed()){
                Logger.log("Failed to insert record caused by ",done.cause());
                futureServiceInstance.fail(done.cause());
            }
        });
        return futureServiceInstance;
    }

    public Future<ServiceInstance> getByName(String name){
        JsonArray params = new JsonArray().add(name);
        Future<ServiceInstance> serviceInstanceFuture = Future.future();
        connector.query("SELECT * FROM services where name=?",params).setHandler(handle->{
           if(handle.succeeded()){
              serviceInstanceFuture.complete(toServiceInstance(handle.result()));
           }else if(handle.failed()){
               serviceInstanceFuture.fail(handle.cause());
           }
        });
        return serviceInstanceFuture;
    }

    public Future<List<ServiceInstance>> getAllServices(){
        Future<List<ServiceInstance>> serviceInstanceFuture = Future.future();
        List<ServiceInstance> serviceInstances = new ArrayList<>();
        connector.query("SELECT * FROM services").setHandler(handle->{
            if(handle.succeeded()){
                serviceInstanceFuture.complete(toServiceInstances(handle.result()));
            }else if(handle.failed()){
                serviceInstanceFuture.fail(handle.cause());
            }
        });
        return serviceInstanceFuture;
    }

    public Future<ServiceInstance> deleteByName(String name){
        JsonArray params = new JsonArray().add(name);
        Future<ServiceInstance> serviceInstanceFuture = Future.future();
        getByName(name).setHandler(done->{
            if(done.succeeded()){
                if(done.result()!=null){
                    connector.query("DELETE FROM services where name=?",params).setHandler(deleteHandler->{
                        if(deleteHandler.succeeded()){
                            serviceInstanceFuture.complete();
                        }else{
                            serviceInstanceFuture.fail("Failed to Delete");
                        }
                    });
                }else{
                    Logger.log("No service of name "+name+" found");
                    serviceInstanceFuture.fail("No service of name \"+name+\" found");
                }
            }else{
                Logger.log("Unable");
            }
        });
        return serviceInstanceFuture;
    }

    public Future<Void> updateStatusByName(String name,String status){
        JsonArray params = new JsonArray().add(status).add(name);
        Future<Void> updateStatus = Future.future();
        connector.query("Update services set status=? where name=?",params).setHandler(handler ->{
            if(handler.succeeded()){
                Logger.log("Successfully updated status "+status+" for "+name);
                updateStatus.complete();
            }else{
                Logger.log("Status update failed for service "+name);
                updateStatus.fail("Failed to updated status "+status+" for "+name);
            }
        });
        return updateStatus;
    }

    private List<ServiceInstance> toServiceInstances(ResultSet resultSet){
        return resultSet.getResults().stream()
                .map(this::createServiceInstance)
                .collect(Collectors.toList());
    }
    private ServiceInstance toServiceInstance(ResultSet resultSet) {
        return resultSet.getResults().stream()
                .map(this::createServiceInstance)
                .findFirst()
                .orElse(null);

    }

    private ServiceInstance createServiceInstance(JsonArray row){
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setId(row.getInteger(0));
        serviceInstance.setName(row.getString(2));
        serviceInstance.setUrl(row.getString(1));
        serviceInstance.setStatus(row.getString(3));
        return serviceInstance;
    }
}
