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
import com.apehat.event.Subscriber;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class SubscriberRAMRegister extends AbstractTimestampSubscriberRegister {

    private final SubscriberRegister globalSubscriberRegister = new GlobalSubscriberRegister();

    private final SubscriberRegister busSubscriberRegister = new BusSubscriberRegister();

    private final ThreadSubscriberRegister threadSubscriberRegister = new ThreadSubscriberRegister();

    @Override protected void doRegister(TimeStampedSubscriber<?> subscriber) {
        if (globalSubscriberRegister.registrable(subscriber)) {
            globalSubscriberRegister.register(subscriber);
        } else if (busSubscriberRegister.registrable(subscriber)) {
            busSubscriberRegister.register(subscriber);
        } else {
            threadSubscriberRegister.register(subscriber);
        }
        //        globalSubscriberRegister.register(subscriber);
        //        busSubscriberRegister.register(subscriber);
        //        threadSubscriberRegister.register(subscriber);
    }

    @Override protected SortedSet<TimeStampedSubscriber<?>> allSubscribers() {
        // needn't impl
        return null;
    }

    @Override
    public <T extends Event> void unregister(Subscriber<T> subscriber) {
        globalSubscriberRegister.unregister(subscriber);
        busSubscriberRegister.unregister(subscriber);
        threadSubscriberRegister.unregister(subscriber);
    }

    @Override
    public <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event) {
        Set<Subscriber<? super T>> subscribers = new HashSet<>();
        subscribers.addAll(threadSubscriberRegister.subscribersOf(event));
        subscribers.addAll(busSubscriberRegister.subscribersOf(event));
        subscribers.addAll(globalSubscriberRegister.subscribersOf(event));
        return Collections.unmodifiableSet(subscribers);
    }

    @Override public boolean contains(Subscriber<?> subscriber) {
        return globalSubscriberRegister
                .contains(subscriber) || busSubscriberRegister
                .contains(subscriber) || threadSubscriberRegister
                .contains(subscriber);
    }

    @Override public boolean registrable(Subscriber<?> subscriber) {
        return subscriber != null;
    }

    /**
     * Clear all subscribers of current thread in this register.
     * The global subscribers and bus subscribers will not be clear.
     *
     * @see com.apehat.event.SubscribeScope
     */
    @Override public void clear() {
        threadSubscriberRegister.clear();
    }
}
