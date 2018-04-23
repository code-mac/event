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

import java.util.Collection;

/**
 * @author hanpengfei
 * @since 1.0
 */
@FunctionalInterface
public interface SubscriberFilter {

    /**
     * Filter the specified subscribers with specified event.
     *
     * @param subscribers
     *         the subscriber collection to filter.
     * @param event
     *         the event to be used filter subscribers
     * @param <T>
     *         the type of event
     * @return Sorted collection, contains subscriber had subscribed specified
     * event
     * @throws UnsupportedEventTypeException
     *         specified event unsupported in this filter
     */
    <T extends Event> Collection<Subscriber<? super T>> filter(
            Collection<Subscriber<?>> subscribers, T event);
}
