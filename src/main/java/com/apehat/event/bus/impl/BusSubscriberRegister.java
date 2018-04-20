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

package com.apehat.event.bus.impl;

import com.apehat.event.Event;
import com.apehat.event.Subscriber;
import com.apehat.event.bus.AbstractSubscriberRegister;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class BusSubscriberRegister extends AbstractSubscriberRegister {

    private final Queue<TimeStampedSubscriber<?>> subscribers = new ConcurrentLinkedQueue<>();

    @Override
    protected <T extends Event> void doRegister(TimeStampedSubscriber<T> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public <T extends Event> void unregister(Subscriber<T> subscriber) {
        if (contains(subscriber)) {
            subscribers.remove(cast(subscriber));
        }
    }

    @Override
    public <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event) {
        Set<Subscriber<? super T>> registeredSubscribers = new HashSet<>();
        for (TimeStampedSubscriber<?> subscriber : subscribers) {
            if (!isSubscribeBefore(subscriber, event)) {
                break;
            }
            if (isSubscribed(subscriber, event)) {
                @SuppressWarnings("unchecked") Subscriber<? super T> source = (Subscriber<? super T>) subscriber
                        .getSource();
                registeredSubscribers.add(source);
            }
        }
        return Collections.unmodifiableSet(registeredSubscribers);
    }

    @Override
    public boolean contains(Subscriber<?> subscriber) {
        return subscribers.contains(cast(subscriber));
    }
}
