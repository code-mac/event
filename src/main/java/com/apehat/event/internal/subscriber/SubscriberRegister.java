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

import java.util.Set;

/**
 * @author hanpengfei
 * @since 1.0
 */
public interface SubscriberRegister {

    /**
     * Register a subscriber to current register.
     * <p>
     * If the scope of subscriber is {@link SubscribeScope#GLOBAL}
     * the register will be find by all register.
     *
     * @param subscriber the subscriber to register
     * @throws NullPointerException the specified subscriber is null
     * @see SubscribeScope
     */
    <T extends Event> void register(Subscriber<T> subscriber);

    /**
     * Unregister the specified subscriber form this.
     *
     * @param subscriber the subscriber to unregister
     * @throws NullPointerException the specified subscriber is null
     */
    <T extends Event> void unregister(Subscriber<T> subscriber);

    /**
     * Returns the subscribers of specified event.
     *
     * @param event the event to get the subscribers
     * @param <T>   the type of event
     * @return the subscribers of specified event, or empty set, if does not
     * have subscribers of specified event
     * @throws NullPointerException the specified event is null
     */
    <T extends Event> Set<Subscriber<? super T>> subscribersOf(T event);

    /**
     * Determine whether the current register contains specified subscriber.
     *
     * @param subscriber the subscriber to check
     * @return true, if the current register contains specified subscriber,
     * else, otherwise false
     * @throws NullPointerException specified subscriber is null
     */
    boolean contains(Subscriber<?> subscriber);

    /**
     * Determine whether specified subscriber can register by this register.
     *
     * @param subscriber the subscriber to check
     * @return true, current register can register specified subscriber;
     * otherwise false
     */
    boolean registrable(Subscriber<?> subscriber);

    /**
     * Clear subscribers of this register.
     *
     * @implSpec
     */
    default void clear() {
    }
}
