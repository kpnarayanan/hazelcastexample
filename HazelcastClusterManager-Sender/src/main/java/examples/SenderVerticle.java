package examples;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class SenderVerticle extends AbstractVerticle{

        @Override
        public void start() throws Exception {
                Logger logger = LoggerFactory.getLogger("SenderVerticle");

                vertx.setPeriodic(10000, v ->
                        vertx.eventBus().publish("receiver-address", "Message to verticle 2"));
                        //vertx.eventBus().send("receiver-address", "Message to receiver..Welcome to Vertx Event Bus programming", acknowledge -> {
                        //      if (acknowledge.succeeded()){
                        //              logger.info("Received reply from receiver" + acknowledge.result().body());
                        //      }else{
                        //              logger.info("No reply");
                //              }
                //      });
                //});
        }

}