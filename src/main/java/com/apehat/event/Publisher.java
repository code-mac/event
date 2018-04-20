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

import com.apehat.event.bus.EventBus;

/**
 * @author hanpengfei
 * @since 1.0
 */
public interface Publisher<T extends Event> {

    /**
     * Publisher the event by event bus.
     * <p>
     * Default, use {@link EventBus#getDefault()} to publish.
     *
     * @param event the event to publish
     * @see EventBus#getDefault()
     */
    default void publish(T event) {
        EventBus.getDefault().submit(event);
    }
}
