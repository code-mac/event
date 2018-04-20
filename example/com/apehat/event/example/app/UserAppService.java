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

package com.apehat.event.example.app;

import com.apehat.event.example.domain.user.User;
import com.apehat.event.example.domain.user.UserId;
import com.apehat.event.example.domain.user.UserRepository;

import java.util.UUID;

/**
 * @author hanpengfei
 * @since 1.0
 */
public final class UserAppService {

    private final UserRepository userRepository;

    public UserAppService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(String username, String password) {
        UserId userId = new UserId(UUID.randomUUID().toString());
        User user = new User(userId, username, password);
        getUserRepository().store(user);
    }

    private UserRepository getUserRepository() {
        return userRepository;
    }
}
