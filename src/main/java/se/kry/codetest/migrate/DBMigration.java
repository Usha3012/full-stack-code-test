package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public void migrate(DBConnector connector) {
     migrate1(connector);
     migrate2(connector);
  }

  private void migrate2(DBConnector connector) {
    connector.query("SELECT * FROM services WHERE name='kry-service'").setHandler(done -> {
      if(done.succeeded()){
        if(done.result().getNumRows()==0) {
          connector.query("INSERT INTO services(url,name)values('https://www.kry.se','kry-service')").setHandler(ops -> {
            if(ops.succeeded()){
              System.out.println("applied migration insert default services kry-service");
            } else {
              ops.cause().printStackTrace();
            }
          });
        }
      }else{
        done.cause().printStackTrace();
      }
    });
  }


  private void migrate1(DBConnector connector){
    connector.query("CREATE TABLE IF NOT EXISTS services(" +
            "id integer primary key autoincrement," +
            "url varchar(255) NOT NULL," +
            "name varchar(255) NOT NULL UNIQUE," +
            "status varchar(255) NOT NULL DEFAULT UNKNOWN);").setHandler(done -> {
      if(done.succeeded()){
        System.out.println("applied migration creating database");
      } else {
        done.cause().printStackTrace();
      }
    });
  }
}
