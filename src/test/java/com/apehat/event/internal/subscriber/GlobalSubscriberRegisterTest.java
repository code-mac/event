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
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class GlobalSubscriberRegisterTest {

    private static SubscriberRegister register = new GlobalSubscriberRegister();
    private static Set<Integer> hashCodes = new HashSet<>();

    private static int invocationCount = 800;

    @AfterClass public static void afterClass() {
        GlobalSubscriberRegister subscriberRegister = (GlobalSubscriberRegister) register;
        int                      size               = subscriberRegister
                .allSubscribers().size();
        System.out.println("Expected: " + invocationCount);
        System.out.println("Current: " + size);
        assert size == invocationCount;
    }

    @Test(threadPoolSize = 5, invocationCount = 800)
    public void testRegister() {
        register.register(new Subscriber<Event>() {
            @Override public Class<? extends Event> subscribeTo() {
                return Event.class;
            }

            @Override public void onEvent(Event event) {
            }

            @Override public SubscribeScope scope() {
                return SubscribeScope.GLOBAL;
            }

            @Override public boolean equals(Object obj) {
                return false;
            }

            @Override public int hashCode() {
                Random random = new Random();
                int    code   = random.nextInt();
                while (hashCodes.contains(code)) {
                    code = random.nextInt();
                }
                hashCodes.add(code);
                return code;
            }
        });
    }
}