package com.eleoptics.spark.eventbus;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.UI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class EventBusFactory {

    //hold the instance of the event bus here
    //private static EventBus eventBus = new EventBus();

    /*
    public static EventBus getEventBus() {
        if (UI.getCurrent().getSession().getAttribute("EventBus") == null) {
            EventBus eventBus = new EventBus();
            UI.getCurrent().getSession().setAttribute("EventBus", eventBus);
        }

        return (EventBus) UI.getCurrent().getSession().getAttribute("EventBus");

    }

     */

}