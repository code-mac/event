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

import com.apehat.event.AbstractEvent;
import com.apehat.event.Event;
import com.apehat.event.TriggerId;
import org.testng.annotations.Test;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class EventBusTest {

    @Test
    public void testSubmit() {
//        EventBus eventBus = EventBus.getDefault();
        Event e = new E(EventBusTest.class.getName());
//        eventBus.submit(e);
        e.publish();
    }

    private class E extends AbstractEvent {

        E(String id) {
            super(new TriggerId() {
                @Override
                public String toString() {
                    return id;
                }
            });
        }
    }
}