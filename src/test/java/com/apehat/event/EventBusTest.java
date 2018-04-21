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

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class EventBusTest {

    static int count = 0;

    @AfterClass public static void tearDown() {
        System.out.println(count);
    }

    @Test(threadPoolSize = 2, invocationCount = 300) public void testSubmit() {
        EventBus eventBus = EventBus.getDefault().reset();
        eventBus.subscribe(new Subscriber<Event>() {
            @Override public Class<? extends Event> subscribeTo() {
                return Event.class;
            }

            @Override public void onEvent(Event event) {
                System.out.println("Handle event " + event);
                count++;
            }
        });
        Event e = new E(EventBusTest.class.getName());
        eventBus.submit(e);
    }

    private class E extends AbstractEvent {

        E(String id) {
            super(new TriggerId() {
                @Override public String toString() {
                    return id;
                }
            });
        }
    }
}