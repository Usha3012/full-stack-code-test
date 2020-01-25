package se.kry.codetest;

import io.vertx.core.Future;
import se.kry.codetest.domain.ServiceInstance;
import se.kry.codetest.service.DBService;
import se.kry.codetest.util.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BackgroundPoller {

  public void  pollServices(DBService service) {
    Logger.log("Running poll service");
    service.getAllServices().setHandler(handler->{
      if(handler.succeeded()){
        List<ServiceInstance> serviceInstances = handler.result();
        pollServices(serviceInstances,service);
      }
    });
  }

  private void pollServices(List<ServiceInstance> serviceInstances,DBService dbService) {
    serviceInstances.forEach(serviceInstance -> {
      dbService.getByName(serviceInstance.getName()).setHandler(handler->{
        if(handler.succeeded()){
          if(handler.result()!=null){
            ServiceInstance existingInstance = handler.result();
            existingInstance.setStatus(healthCheck(existingInstance.getUrl()));
            dbService.updateStatusByName(existingInstance.getName(),existingInstance.getStatus());
          }else{
            Logger.log("Service not found");
          }
        }
      });
    });
  }

  //This is a mock to GET call
  private String healthCheck(String url) {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    int randomInt=random.nextInt(0,100);
    if(randomInt<10){
      return "UNKNOWN";
    }else if(randomInt>50){
      return "OK";
    }else{
      return "DOWN";
    }
  }
}
