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

package com.apehat.event;

import com.apehat.event.register.EventQueue;
import com.apehat.event.register.EventRAMQueue;
import com.apehat.event.register.SubscriberRAMRegister;
import com.apehat.event.register.SubscriberRegister;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The class {@code EventBus} is be used as facade to subscribe and submit
 * event.
 * <p>
 * TODO 去掉 Scope, 利用子类，通过策略模式获取订阅者, EventBus 本身持有 Subscribers
 * <p>
 * TODO 如上，不应该提供 Reset 域
 * <p>
 * TODO 添加 Id 域，EventBus 自身生成 Id
 *
 * @author hanpengfei
 * @since 1.0
 */
public class EventBus {

    private static final Builder DEFAULT_BUILDER = new Builder();

    /** The exception handler, be used to handle subscribe exception */
    private final SubscribeExceptionHandler subscribeExceptionHandler;

    /** The subscriber register be used to register subscribers */
    private final SubscriberRegister subscriberRegister;

    /** The event register be used to blocked events. */
    private final EventQueue eventQueue;

    private final Lock publishLock;
    private final String id;

    private EventBus(Builder builder) {
        this.id = builder.id;
        this.subscribeExceptionHandler = builder.subscribeExceptionHandler;

        publishLock = new ReentrantLock();
        subscriberRegister = new SubscriberRAMRegister();
        eventQueue = new EventRAMQueue();
    }

    /**
     * Returns an event bus with default configuration.
     *
     * @return an event bus with default configuration
     */
    public static EventBus getDefault() {
        return DEFAULT_BUILDER.build();
    }

    public <T extends Event> void subscribe(
            Subscriber<? extends T> subscriber) {
        subscriberRegister.register(subscriber);
    }

    /**
     * Submit an event to publish. If the event queue don't have event, will
     * publish now.
     *
     * @param event
     *         the event to publish
     */
    public void submit(Event event) {
        eventQueue.register(event);
        publish();
    }

    /**
     * Clear all subscribers of this event bus in current thread.
     * <p>
     * This method should be invoke at started when use thread pool.
     */
    public EventBus reset() {
        subscriberRegister.clear();
        return this;
    }

    /**
     * Check {@code eventQueue} and publish event
     */
    private void publish() {
        if (publishLock.tryLock()) {
            Event event;
            try {
                while ((event = eventQueue.nextEvent()) != null) {
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
            invokeSubscriberHandler(subscriber, event);
        }
    }

    /**
     * Returns the subscribers of the specified event.
     *
     * @param event
     *         the event to get subscribers
     * @param <T>
     *         the type of event
     * @return the subscribers of specified event
     */
    private <T extends Event> Set<Subscriber<? super T>> getSubscribers(
            T event) {
        assert event != null;
        return subscriberRegister.subscribersOf(event);
    }

    /**
     * Invoke {@link Subscriber#onEvent(Event)} method, will throw exception
     * when handle event, will use {@code subscribeExceptionHandler} to handle
     * this exception
     *
     * @param event
     *         the event to be handled
     * @param subscriber
     *         the subscriber
     * @param <T>
     *         the type of event
     */
    private <T extends Event> void invokeSubscriberHandler(
            Subscriber<? super T> subscriber, T event) {
        assert event != null;
        assert subscriber != null;
        try {
            subscriber.onEvent(event);
        } catch (Exception e) {
            subscribeExceptionHandler.handle(e, event, subscriber);
        }
    }

    /**
     * Returns the exception subscribeExceptionHandler of this. If the exception
     * handler hadn't be set, will return {@code DEFAULT_EXCEPTION_HANDLER}
     *
     * @return the exception subscribeExceptionHandler of this, or {@code
     * DEFAULT_EXCEPTION_HANDLER} if the exception subscribeExceptionHandler
     * hadn't be set
     */
    public SubscribeExceptionHandler getSubscribeExceptionHandler() {
        return subscribeExceptionHandler;
    }

    /**
     * Returns an unmodifiable subscriber subscriberRegister of the current
     * event bus.
     * <p>
     * The subscriberRegister can carried out query instructions, like {@link
     * SubscriberRegister#subscribersOf(Event)} and so on. Else, will throw
     * {@link UnsupportedOperationException}
     *
     * @return an unmodifiable subscriber subscriberRegister of the current
     * event bus
     */
    public SubscriberRegister getSubscriberRegister() {
        return new UnmodifiableSubscriberRegister(subscriberRegister);
    }

    /**
     * Returns the id of this.
     *
     * @return the id of this.
     */
    public String getId() {
        return id;
    }

    static class UnmodifiableSubscriberRegister implements SubscriberRegister {

        private final SubscriberRegister subscriberRegister;

        UnmodifiableSubscriberRegister(SubscriberRegister subscriberRegister) {
            this.subscriberRegister = Objects
                    .requireNonNull(subscriberRegister);
        }

        @Override
        public <T extends Event> void register(Subscriber<T> subscriber) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Event> void unregister(Subscriber<T> subscriber) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends Event> Set<Subscriber<? super T>> subscribersOf(
                T event) {
            Set<Subscriber<? super T>> subscribers = subscriberRegister
                    .subscribersOf(event);
            return Collections.unmodifiableSet(subscribers);
        }

        @Override public boolean contains(Subscriber<?> subscriber) {
            return subscriberRegister.contains(subscriber);
        }

        @Override public boolean registrable(Subscriber<?> subscriber) {
            return subscriberRegister.registrable(subscriber);
        }
    }

    /** Event Bus Builder */
    public static class Builder {

        private final String id;
        private SubscribeExceptionHandler subscribeExceptionHandler;

        public Builder() {
            this.id = UUID.randomUUID().toString().toUpperCase();
        }

        /**
         * Sets the exception subscribeExceptionHandler of this.
         *
         * @param subscribeExceptionHandler
         *         the exception subscribeExceptionHandler to be use
         */
        public void setSubscribeExceptionHandler(
                SubscribeExceptionHandler subscribeExceptionHandler) {
            this.subscribeExceptionHandler = subscribeExceptionHandler;
        }

        /**
         * Build the event bus
         *
         * @return a event instance
         */
        public EventBus build() {
            if (subscribeExceptionHandler == null) {
                subscribeExceptionHandler = new SubscribeExceptionLogger(
                        LoggerFactory.getLogger(EventBus.class));
            }
            return new EventBus(this);
        }
    }
}