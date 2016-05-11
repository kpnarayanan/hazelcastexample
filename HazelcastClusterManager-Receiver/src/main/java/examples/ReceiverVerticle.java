package examples;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ReceiverVerticle extends AbstractVerticle{

        @Override
        public void start() throws Exception {
                Logger logger = LoggerFactory.getLogger("ReceiverVerticle");
                System.out.println("Inside start method - Receiver verticle");
                vertx.eventBus().consumer("receiver-address", message ->
                                        logger.info("Received message..."+message.body()));

                                        // Replying to the sender
                                        //message.reply("Got message from Sender....");
                                //});
        }

}