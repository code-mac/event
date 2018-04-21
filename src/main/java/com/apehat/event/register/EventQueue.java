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

package com.apehat.event.register;

import com.apehat.event.Event;

import java.util.Queue;

/**
 * @author hanpengfei
 * @since 1.0
 */
public interface EventQueue extends Queue<Event> {

    /**
     * Register a blocked event to this register
     *
     * @param event
     *         the event to register
     * @throws NullPointerException
     *         specified event is null
     */
    default void register(Event event) {
        offer(event);
    }

    /**
     * Returns the next blocked event, or null, if no event be blocked.
     *
     * @return the next blocked event, or null, if no event be blocked
     */
    default Event nextEvent() {
        return poll();
    }
}
