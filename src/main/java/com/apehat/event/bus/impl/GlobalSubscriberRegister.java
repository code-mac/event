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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class GlobalSubscriberRegister extends AbstractSubscriberRegister {

    private static final Set<TimeStampedSubscriber<?>> SUBSCRIBERS = new ConcurrentSkipListSet<>();

    @Override
    protected <T extends Event> void doRegister(TimeStampedSubscriber<T> subscriber) {
        assert isGlobalSubscriber(subscriber);
        SUBSCRIBERS.add(subscriber);
    }

    @Override
    public <T extends Event> void unregister(Subscriber<T> subscriber) {
        TimeStampedSubscriber<T> cast = cast(subscriber);
        if (!contains(cast(subscriber))) {
            throw new IllegalStateException(String
                    .format("%s not exists", subscriber));
        }
        SUBSCRIBERS.remove(cast);
    }

    @Override
    public <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event) {
        Set<Subscriber<? super T>> subscribers = new LinkedHashSet<>();
        for (TimeStampedSubscriber<?> subscriber : SUBSCRIBERS) {
            if (!isSubscribeBefore(subscriber, event)) {
                break;
            }
            if (isSubscribed(subscriber, event)) {
                @SuppressWarnings("unchecked") Subscriber<? super T> s = (Subscriber<? super T>) subscriber;
                subscribers.add(s);
            }
        }
        return Collections.unmodifiableSet(subscribers);
    }

    @Override
    public boolean contains(Subscriber<?> subscriber) {
        return SUBSCRIBERS.contains(cast(subscriber));
    }
}
