package es2.eventLoop;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

public class VResultListener extends AbstractVerticle {
	
    public void start(Promise<Void> startPromise) {
       log("started.");
       EventBus eb = this.getVertx().eventBus();
       eb.consumer("my-topic", message -> {
           log("new message: " + message.body());
       });		
       log("Ready.");
       startPromise.complete();
   }

   private void log(String msg) {
       System.out.println("[REACTIVE AGENT #1]["+Thread.currentThread()+"] " + msg);
   }
}
