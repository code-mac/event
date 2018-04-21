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

package com.apehat.event.annotation;

import com.apehat.event.Event;
import com.apehat.event.Subscriber;

import java.lang.annotation.*;

/**
 * Subscribe events by defined {@code Subscriber}s.
 * <p>
 * The annotation {@code Subscribe} is an compile time annotation. i.e. the
 * process action happened at compile time, and the generated class file will be
 * modifier at compile time.
 * <p>
 * The specified {@code subscriber} must have no parameter constructor; the
 * specified {@code EventSubscribers} must can onEvent the type of specified
 * {@code eventType}
 *
 * @author hanpengfei
 * @since 1.0
 */
@Documented
@Inherited
@Repeatable(Subscribes.class)
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Subscribe {

    /**
     * Subscribe a event.
     *
     * @return be subscribed event.
     */
    Class<? extends Event> evenType();

    /**
     * The subscribers to subscribe specified event.
     *
     * @return the subscribes to subscriber
     */
    Class<? extends Subscriber>[] by();
}
