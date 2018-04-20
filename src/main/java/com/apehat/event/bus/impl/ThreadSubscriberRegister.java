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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class ThreadSubscriberRegister extends AbstractSubscriberRegister {

    private final Map<Long, Queue<TimeStampedSubscriber<?>>> threadMap = new ConcurrentHashMap<>();

    @Override
    protected <T extends Event> void doRegister(TimeStampedSubscriber<T> subscriber) {
        getCurrentThreadSubscriberQueue().offer(subscriber);
    }

    /**
     * Unregister the specified subscriber.
     *
     * @param subscriber the subscriber to unregister
     * @param <T>        the type of event, that specified subscriber subscribed
     * @throws NullPointerException the specified subscriber is null
     */
    @Override
    public <T extends Event> void unregister(Subscriber<T> subscriber) {
        getCurrentThreadSubscriberQueue().remove(cast(subscriber));
    }

    @Override
    public <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event) {
        Set<Subscriber<? super T>> subscribers = new LinkedHashSet<>();

        Queue<TimeStampedSubscriber<?>> subscriberQueue = getCurrentThreadSubscriberQueue();
        for (TimeStampedSubscriber subscriber : subscriberQueue) {
            if (!isSubscribeBefore(subscriber, event)) {
                break;
            }
            if (isSubscribed(subscriber, event)) {
                @SuppressWarnings("unchecked") Subscriber<? super T> s = subscriber;
                subscribers.add(s);
            }
        }
        return Collections.unmodifiableSet(subscribers);
    }

    @Override
    public boolean contains(Subscriber<?> subscriber) {
        return getCurrentThreadSubscriberQueue().contains(cast(subscriber));
    }

    /**
     * Remove all subscribers of the current thread.
     */
    public void reset() {
        getCurrentThreadSubscriberQueue().clear();
    }

    private Queue<TimeStampedSubscriber<?>> getCurrentThreadSubscriberQueue() {
        long threadId = Thread.currentThread().getId();
        return threadMap.computeIfAbsent(threadId, k -> new LinkedList<>());
    }
}
