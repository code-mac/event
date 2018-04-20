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

import com.apehat.event.SubscribeScope;
import com.apehat.event.Subscriber;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class BusSubscriberRegister extends AbstractSubscriberRegister {

    private final Set<TimeStampedSubscriber<?>> subscribers = new ConcurrentSkipListSet<>();

    @Override
    protected Collection<TimeStampedSubscriber<?>> allSubscribers() {
        return subscribers;
    }

    @Override
    public boolean registrable(Subscriber<?> subscriber) {
        return Objects.equals(subscriber.scope(), SubscribeScope.BUS);
    }
}
