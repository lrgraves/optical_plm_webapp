package com.eleoptics.spark.eventbus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;

import java.util.function.Consumer;
@Slf4j
public class EventBus_depracated {
    public static void broadcastEvent(SparkEvent event) {
        var handlers = eventHandlers.get(event.getClass());
        if (handlers != null) {
            handlers.forEach(handler -> {
                handler.handler.accept(event);
            });
        } else {
            log.info("No handler found of type {}", event.getClass());
        }

    }

    private static LinkedMultiValueMap<Class<? extends SparkEvent>, SparkEventHandler_depracated> eventHandlers =
            new LinkedMultiValueMap<>();

    public static SparkEventHandler_depracated addEventHandler(Class<? extends SparkEvent> eventType, Consumer<SparkEvent> handler){
        SparkEventHandler_depracated sparkEventHandler = new SparkEventHandler_depracated(eventType, handler);
        eventHandlers.add(eventType, sparkEventHandler);
        return sparkEventHandler;
    }

    public static void removeEventHandler(SparkEventHandler_depracated sparkEventHandler){
        eventHandlers.remove(sparkEventHandler.eventType, sparkEventHandler);
    }

}
