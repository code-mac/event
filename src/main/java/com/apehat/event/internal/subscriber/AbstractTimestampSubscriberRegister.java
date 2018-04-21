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

package com.apehat.event.internal.subscriber;

import com.apehat.event.Event;
import com.apehat.event.SubscribeScope;
import com.apehat.event.Subscriber;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author hanpengfei
 * @since 1.0
 */
public abstract class AbstractTimestampSubscriberRegister
        implements SubscriberRegister {

    @Override
    public <T extends Event> void register(Subscriber<T> subscriber) {
        TimeStampedSubscriber<T> timeStampedSubscriber = cast(subscriber);
        if (registrable(timeStampedSubscriber) && !contains(timeStampedSubscriber)) {
            doRegister(timeStampedSubscriber);
        }
    }

    @Override
    public <T extends Event> void unregister(Subscriber<T> subscriber) {
        TimeStampedSubscriber<T> timeStampedSubscriber = cast(subscriber);
        if (contains(timeStampedSubscriber)) {
            allSubscribers().remove(timeStampedSubscriber);
        }
    }

    @Override
    public <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event) {
        return doSearchByEvent(allSubscribers(), Objects.requireNonNull(event));
    }

    @Override
    public boolean contains(Subscriber<?> subscriber) {
        return subscriber != null && allSubscribers()
                .contains(cast(subscriber));
    }

    protected void doRegister(TimeStampedSubscriber<?> subscriber) {
        assert subscriber != null;
        allSubscribers().add(subscriber);
    }

    /**
     * Returns the all subscribers of the current register.
     * <p>
     * The implement invoked by {@link #unregister(Subscriber)},
     * {@link #subscribersOf(Event)}, {@link #contains(Subscriber)},
     * {@link #doRegister(TimeStampedSubscriber)}
     *
     * @return all subscribers collection, or an empty collection
     */
    protected abstract Collection<TimeStampedSubscriber<?>> allSubscribers();

    private <T extends Event> Set<Subscriber<? super T>> doSearchByEvent(Collection<TimeStampedSubscriber<?>> registeredSubscribers, T event) {
        assert registeredSubscribers != null;
        assert event != null;

        registeredSubscribers = new ConcurrentSkipListSet<>(registeredSubscribers);

        Set<Subscriber<? super T>> subscribers = new HashSet<>();
        for (TimeStampedSubscriber subscriber : registeredSubscribers) {
            // already sorted by timestamp
            // so, if the subscriber subscribe after event occur time
            // need search successor nodes
            if (isSubscribeAfter(subscriber, event)) {
                break;
            }

            if (isSubscribed(subscriber, event)) {
                @SuppressWarnings("unchecked") Subscriber<? super T> s = subscriber;
                subscribers.add(s);
            }
        }
        return Collections.unmodifiableSet(subscribers);
    }

    private boolean isSubscribeAfter(TimeStampedSubscriber subscriber, Event event) {
        return subscriber.timestamp > event.occurredOn();
    }

    private boolean isSubscribed(Subscriber<?> subscriber, Event event) {
        return subscriber.subscribeTo().isAssignableFrom(event.getClass());
    }

    private <T extends Event> TimeStampedSubscriber<T> cast(Subscriber<T> subscriber) {
        return (subscriber instanceof TimeStampedSubscriber) ?
               (TimeStampedSubscriber<T>) subscriber :
               new TimeStampedSubscriber<>(subscriber);
    }

    protected static class TimeStampedSubscriber<T extends Event>
            implements Subscriber<T>, Comparable<TimeStampedSubscriber> {

        private final Subscriber<T> source;
        private final long timestamp;

        TimeStampedSubscriber(Subscriber<T> source) {
            assert source != null;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public Class<? extends T> subscribeTo() {
            return source.subscribeTo();
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
            if (o instanceof Subscriber) {
                if (o instanceof TimeStampedSubscriber) {
                    return source.equals(((TimeStampedSubscriber) o).source);
                }
                return source.equals(o);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return source.hashCode();
        }

        @Override
        public int compareTo(TimeStampedSubscriber o) {
            long value = timestamp - o.timestamp;
            if (value > 0) {
                return 1;
            } else if (value == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
