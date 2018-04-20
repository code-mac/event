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

package com.apehat.event.example.domain.user;

import com.apehat.event.TriggerId;

import java.util.Objects;

/**
 * @author hanpengfei
 * @since 1.0
 */
public final class UserId implements TriggerId {

    private final String source;

    public UserId(String source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public String toString() {
        return source;
    }
}
