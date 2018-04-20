/*
 * Copyright Apehat.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apehat.event.bus;

import com.apehat.event.Event;
import com.apehat.event.Subscriber;
import com.apehat.event.bus.impl.DefaultSubscriberRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class {@code EventBus} is be used as facade to subscribe and submit
 * event.
 *
 * @author hanpengfei
 * @since 1.0
 */
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    private static final Map<String, EventBus> BUS_CACHE_POOL = new ConcurrentHashMap<>();

    /** The default event bus id */
    private static final String DEFAULT_ID = "default";

    static {
        BUS_CACHE_POOL.put(DEFAULT_ID, new Builder(DEFAULT_ID).build());
    }

    /** The id of event bus */
    private final String id;

    /** The exception handler, be used to handle subscribe exception */
    private final ExceptionHandler exceptionHandler;

    /** The event queue be used to store events, what's waiting to publish. */
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();

    /** The subscriber register be used to register subscribers */
    private final SubscriberRegister subscriberRegister;

    private final Lock publishLock = new ReentrantLock();

    private EventBus(Builder builder) {
        this.id = builder.id;
        this.subscriberRegister = builder.subscriberRegister;
        this.exceptionHandler = builder.exceptionHandler;
    }

    /**
     * Returns the default event bus.
     *
     * @return the default event bus
     */
    public static EventBus getDefault() {
        return get(DEFAULT_ID);
    }

    /**
     * Returns the event bus by specified id.
     *
     * @param id the id of event bus
     * @return the event bus with specified id, or null if non event bus with
     * specified id
     */
    public static EventBus get(final String id) {
        return ThreadLocal.withInitial(() -> BUS_CACHE_POOL.get(id)).get();
    }

    public <T extends Event> void subscribe(Subscriber<? extends T> subscriber) {
        subscriberRegister.register(subscriber);
    }

    /**
     * Submit an event to publish. If the event queue don't have event, will
     * publish now.
     *
     * @param event the event to publish
     */
    public void submit(Event event) {
        eventQueue.add(Objects.requireNonNull(event));
        publish();
    }

    /**
     * Check {@code eventQueue} and publish event
     */
    private void publish() {
        if (publishLock.tryLock()) {
            Event event;
            try {
                while ((event = eventQueue.poll()) != null) {
                    publishHelper(event);
                }
            } finally {
                publishLock.unlock();
            }
        }
    }

    private <T extends Event> void publishHelper(T event) {
        assert event != null;
        Set<Subscriber<? super T>> subscribers = getSubscribers(event);
        for (Subscriber<? super T> subscriber : subscribers) {
            invokeSubscriberHandler(event, subscriber);
        }
    }

    /**
     * Returns the subscribers of the specified event.
     *
     * @param event the event to get subscribers
     * @param <T>   the type of event
     * @return the subscribers of specified event
     */
    private <T extends Event> Set<Subscriber<? super T>> getSubscribers(T event) {
        assert event != null;
        // convert safe;
        // the event type must be Class<T>
        // determine by parameter
        return subscriberRegister.subscribersOf(event);
    }

    /**
     * Invoke {@link Subscriber#onEvent(Event)} method, will throw exception
     * when handle event, will use {@code exceptionHandler} to handle this
     * exception
     *
     * @param event      the event to be handled
     * @param subscriber the subscriber
     * @param <T>        the type of event
     */
    private <T extends Event> void invokeSubscriberHandler(T event, Subscriber<? super T> subscriber) {
        assert event != null;
        assert subscriber != null;
        try {
            subscriber.onEvent(event);
        } catch (Exception e) {
            exceptionHandler.handle(e, event, subscriber);
        }
    }

    /**
     * Returns the id of current event bus.
     *
     * @return the current event bus id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the exception exceptionHandler of this. If the exception
     * handler hadn't be set, will return {@code DEFAULT_EXCEPTION_HANDLER}
     *
     * @return the exception exceptionHandler of this, or {@code
     * DEFAULT_EXCEPTION_HANDLER} if the exception exceptionHandler hadn't be set
     */
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Returns an unmodifiable subscriber subscriberRegister of the current event bus.
     * <p>
     * The subscriberRegister can carried out query instructions, like
     * {@link SubscriberRegister#subscribersOf(Event)} and so on.
     * Else, will throw {@link UnsupportedOperationException}
     *
     * @return an unmodifiable subscriber subscriberRegister of the current event bus
     */
    public SubscriberRegister getSubscriberRegister() {
        return new SubscriberRegister() {
            @Override
            public <T extends Event> void register(Subscriber<T> subscriber) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Event> void unregister(Subscriber<T> subscriber) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event) {
                Set<Subscriber<? super T>> subscribers = subscriberRegister
                        .subscribersOf(event);
                return Collections.unmodifiableSet(subscribers);
            }

            @Override
            public boolean contains(Subscriber<?> subscriber) {
                return subscriberRegister.contains(subscriber);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EventBus)) {
            return false;
        }
        EventBus eventBus = (EventBus) o;
        return id.equals(eventBus.id);
    }

    @Override
    public int hashCode() {
        int result = 253;
        result += 31 * id.hashCode();
        return result;
    }

    /** Event Bus Builder */
    public static class Builder {

        /** The default exception handler, use logger to record exception. */
        private static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER = new ExceptionLogger(log);

        private String id;
        private ExceptionHandler exceptionHandler;
        private SubscriberRegister subscriberRegister;

        public Builder(String id) {
            if (BUS_CACHE_POOL.get(id) != null) {
                throw new IllegalArgumentException(String
                        .format("Event bus with id %s already exists.", id));
            }
            this.id = Objects.requireNonNull(id);
        }

        /**
         * Sets the exception exceptionHandler of this.
         *
         * @param exceptionHandler the exception exceptionHandler to be use
         */
        public void setExceptionHandler(ExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        /**
         * Set the subscriber subscriberRegister to event bus.
         *
         * @param subscriberRegister the subscriberRegister
         */
        public void setSubscriberRegister(SubscriberRegister subscriberRegister) {
            this.subscriberRegister = subscriberRegister;
        }

        /**
         * Build the event bus
         *
         * @return a event instance
         */
        public EventBus build() {
            if (exceptionHandler == null) {
                exceptionHandler = DEFAULT_EXCEPTION_HANDLER;
            }
            if (subscriberRegister == null) {
                subscriberRegister = new DefaultSubscriberRegister();
            }
            EventBus eventBus = new EventBus(this);
            BUS_CACHE_POOL.put(id, eventBus);
            return eventBus;
        }
    }
}