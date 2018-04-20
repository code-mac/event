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

import java.util.*;

/**
 * @author hanpengfei
 * @since 1.0
 */
public final class SubscriberRegisterImpl implements SubscriberRegister {

    private static final Map<Class<? extends Event>, Set<Subscriber>> GLOBAL_SUBSCRIBERS;

    static {
        Map<Class<? extends Event>, Set<Subscriber>> temp = new HashMap<>();
        // load configured subscriber
        ServiceLoader<Subscriber> configuredSubscribers = ServiceLoader.load(Subscriber.class);
        for (Subscriber subscriber : configuredSubscribers) {
            if (!SubscribeScope.GLOBAL.equals(subscriber.scope())) {
                throw new ExceptionInInitializerError(
                        subscriber.getClass() + " must have global scope.");
            }

            @SuppressWarnings("unchecked") Class<? extends Event> eventType = subscriber.subscribeTo();

            Set<Subscriber> subscribers = temp.get(eventType);
            if (subscribers == null) {
                subscribers = new HashSet<>();
            }
            subscribers.add(subscriber);

            temp.put(eventType, subscribers);
        }
        GLOBAL_SUBSCRIBERS = Collections.unmodifiableMap(temp);
    }

    private final Map<Class<? extends Event>, Set<SubscriberWrapper>>
            busGlobalSubscribers = new HashMap<>();
    private final Map<Class<? extends Event>, Stack<SubscriberWrapper>>
            threadLocalSubscribers = new HashMap<>();

    /**
     * Determine whether the specified subscriber have bus global scope.
     *
     * @param subscriber the subscriber to determine
     * @return true, the subscriber have global scope, otherwise false.
     */
    private boolean isBusGlobalSubscriber(Subscriber<?> subscriber) {
        return SubscribeScope.BUS.equals(subscriber.scope());
    }

    /**
     * Event bus global subscribe by specified subscriberWrapper.
     *
     * @param subscriberWrapper the subscriberWrapper to subscribe
     * @throws AssertionError the specified subscriberWrapper scope isn't global
     * @see Subscriber#scope()
     * @see SubscribeScope#GLOBAL
     */
    private void busGlobalSubscribe(SubscriberWrapper subscriberWrapper) {
        Subscriber<?> source = subscriberWrapper.subscriber;

        assert isBusGlobalSubscriber(source);

        Class<? extends Event> eventType = source.subscribeTo();
        Set<SubscriberWrapper> subscribers = busGlobalSubscribers.get(eventType);
        if (subscribers == null) {
            subscribers = new LinkedHashSet<>();
        }
        assert !subscribers.contains(subscriberWrapper);
        subscribers.add(subscriberWrapper);
        busGlobalSubscribers.put(eventType, subscribers);
    }

    @Override
    public void clear() {

    }

    @Override
    public <T extends Event> void register(Subscriber<T> subscriber) {
        SubscriberWrapper<T> subscriberWrapper = new SubscriberWrapper(subscriber);
        // register global subscriber
        if (isBusGlobalSubscriber(subscriber)) {
            busGlobalSubscribe(subscriberWrapper);
        } else {
            // is thread local
            Class<? extends Event> eventType = subscriberWrapper.subscriber.subscribeTo();
            Stack<SubscriberWrapper> subscribers = threadLocalSubscribers.get(eventType);
            if (subscribers == null) {
                subscribers = new Stack<>();
            }
            assert !subscribers.contains(subscriberWrapper);
            subscribers.add(subscriberWrapper);
            threadLocalSubscribers.put(eventType, subscribers);
        }
    }

    @Override
    public <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event) {
        Objects.requireNonNull(event);
        Set<Subscriber> subscribers = new LinkedHashSet<>();

        // todo 从ThreadLocal中查找
        Collection collection = subscribers(threadLocalSubscribers, event.getClass());
        // todo 根据找到的结合进行事件筛查

        // TODO 从 BusGlobal 中查找

        // todo 从 GLOBAL 中查找
        Collection collection1 = subscribers(GLOBAL_SUBSCRIBERS, event.getClass());
        subscribers.addAll(collection1);
        return Collections.emptySet();
    }

    private Collection subscribers(Map map, Class<? extends Event> eventType) {
        Set<Subscriber> subscribers = new HashSet<>();

        @SuppressWarnings("unchecked") Set<Class<? extends Event>> keySet = map.keySet();
        for (Class<? extends Event> clazz : keySet) {
            if (clazz.isAssignableFrom(eventType)) {
                Collection c = (Collection) map.get(clazz);
                subscribers.addAll(c);
            }
        }
        return subscribers;
    }

    /**
     * A subscriber wrapper class, mark subscriber with a timestamp
     */
    private static class SubscriberWrapper<T extends Event> implements
            Subscriber<T> {

        private final long timestamp;
        private final Subscriber<T> subscriber;

        private SubscriberWrapper(Subscriber<T> subscriber) {
            this.timestamp = System.currentTimeMillis();
            this.subscriber = Objects.requireNonNull(subscriber);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof SubscriberWrapper)) {
                return false;
            }
            SubscriberWrapper subscriberWrapper = (SubscriberWrapper) o;
            return timestamp == subscriberWrapper.timestamp
                    && subscriber.equals(subscriberWrapper.subscriber);
        }

        @Override
        public int hashCode() {
            int result = 251;
            result += 31 * Objects.hashCode(timestamp);
            result += 31 * subscriber.hashCode();
            return result;
        }

        @Override
        public SubscribeScope scope() {
            return subscriber.scope();
        }

        @Override
        public Class<? extends T> subscribeTo() {
            return subscriber.subscribeTo();
        }

        @Override
        public void onEvent(T event) {
            subscriber.onEvent(event);
        }
    }
}
