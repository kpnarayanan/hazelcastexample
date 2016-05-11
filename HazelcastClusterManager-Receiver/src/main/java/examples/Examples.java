package examples;

import java.util.function.Consumer;

import com.hazelcast.config.Config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

//public class Examples extends AbstractVerticle{
public class Examples {

        Logger logger = LoggerFactory.getLogger("Examples");

        /*@Override
        public void start() throws Exception {
                vertx.setPeriodic(10000, v -> {
                        // senderBus.publish("verticle2-address", "Message to verticle 2")});
                        vertx.eventBus().send("receiver-address", "Message to receiver..Welcome to Vertx Event Bus programming", acknowledge -> {
                                if (acknowledge.succeeded()){
                                        logger.info("Received reply from receiver" + acknowledge.result().body());
                                }else{
                                        logger.info("No reply");
                                }
                        });
                });*/

        /*Vertx.clusteredVertx(options, res -> {
                        if (res.succeeded()) {
                                Vertx vertx = res.result();
                                System.out.println(vertx.toString());
                                vertx.setPeriodic(10000, v -> {
                                        // senderBus.publish("verticle2-address", "Message to verticle 2")});
                                        vertx.eventBus().send("receiver-address", "Message to receiver..Welcome to Vertx Event Bus programming", acknowledge -> {
                                                if (acknowledge.succeeded()){
                                                        logger.info("Received reply from receiver" + acknowledge.result().body());
                                                }else{
                                                        logger.info("No reply");
                                                }
                                        });
                                });
                        } else {
                                System.out.println("Deployment failed...");
                        }
                });*/
        //}

        public static void main(String[] args) throws Exception {
                runClusteredExample();
                //Examples ex = new Examples();
                //ex.start();
        }

		 public static void runClusteredExample(){
                Config hazelcastConfig = new Config();

                ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);

                VertxOptions options = new VertxOptions().setClustered(true).setClusterManager(mgr);

                Consumer<Vertx> runner = vertx -> {
                        try {
                                vertx.deployVerticle(examples.ReceiverVerticle.class.getName());
                        } catch (Throwable t) {
                                t.printStackTrace();
                        }
                };

                Vertx.clusteredVertx(options, res -> {
                        if (res.succeeded()) {
                                Vertx vertx = res.result();
                                System.out.println(vertx.toString());
                                runner.accept(vertx);

                        } else {
                                res.cause().printStackTrace();
                                System.out.println("Deployment failed...");
                        }
                });
        }
}