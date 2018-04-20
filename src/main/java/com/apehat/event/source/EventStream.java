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

package com.apehat.event.source;

/**
 * @author hanpengfei
 * @since 1.0
 */
public class EventStream {
    // 基于事件建立事件树
    // 树模型的建立的原因是：
    // 基于一个 version 为1的事件，可能会衍生出多个事件

    public EventStreamId id() {
        return null;
    }
}
