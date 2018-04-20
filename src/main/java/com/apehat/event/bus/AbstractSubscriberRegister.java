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
import com.apehat.event.SubscribeScope;
import com.apehat.event.Subscriber;

import java.util.Objects;

/**
 * @author hanpengfei
 * @since 1.0
 */
public abstract class AbstractSubscriberRegister implements SubscriberRegister {

    @Override
    public <T extends Event> void register(Subscriber<T> subscriber) {
        Objects.requireNonNull(subscriber);
        TimeStampedSubscriber<T> timeStampedSubscriber = cast(subscriber);
        if (!contains(timeStampedSubscriber)) {
            doRegister(timeStampedSubscriber);
        }
    }

    /**
     * Register the specified source. The subscribe must not be null.
     *
     * @param subscriber a wrapped source, wrapped source with
     *                   timestamp
     * @param <T>        the event type that source subscribed
     */
    protected abstract <T extends Event> void doRegister(TimeStampedSubscriber<T> subscriber);

    protected boolean isGlobalSubscriber(Subscriber<?> subscriber) {
        return SubscribeScope.GLOBAL.equals(subscriber.scope());
    }

    protected boolean isBusSubscriber(Subscriber<?> subscriber) {
        return SubscribeScope.BUS.equals(subscriber.scope());
    }

    protected boolean isSubscribed(Subscriber<?> subscriber, Event event) {
        Class<?> subscribeType = subscriber.subscribeTo();
        Class<? extends Event> eventType = event.getClass();
        return subscribeType.isAssignableFrom(eventType);
    }

    protected boolean isSubscribeBefore(TimeStampedSubscriber subscriber, Event event) {
        return subscriber.timestamp <= event.occurredOn();
    }

    protected <T extends Event> TimeStampedSubscriber<T> cast(Subscriber<T> subscriber) {
        return (subscriber instanceof TimeStampedSubscriber) ?
               (TimeStampedSubscriber<T>) subscriber :
               new TimeStampedSubscriber<>(subscriber);
    }

    protected static class TimeStampedSubscriber<T extends Event>
            implements Subscriber<T> {

        private final Subscriber<T> source;
        private final long timestamp;

        protected TimeStampedSubscriber(Subscriber<T> source) {
            assert source != null;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public Class<? extends T> subscribeTo() {
            return source.subscribeTo();
        }

        public Subscriber<T> getSource() {
            return source;
        }

        @Override
        public void onEvent(T event) {
            source.onEvent(event);
        }

        @Override
        public SubscribeScope scope() {
            return source.scope();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TimeStampedSubscriber<?> that = (TimeStampedSubscriber<?>) o;
            return timestamp == that.timestamp && Objects
                    .equals(source, that.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, timestamp);
        }
    }
}
