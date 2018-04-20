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

import com.apehat.event.EventBus;

import java.util.Objects;

/**
 * @author hanpengfei
 * @since 1.0
 */
public final class User {

    private final UserId userId;

    private String username;
    private String password;
    private String email;

    public User(UserId userId, String username, String password) {
        this.userId = Objects.requireNonNull(userId);
        setUsername(username);
        this.password = Objects.requireNonNull(password);

        EventBus.getDefault().submit(new UserRegistered(userId, username));
    }

    public void setUsername(String username) {
        this.username = Objects.requireNonNull(username);
    }

    public String username() {
        return username;
    }

    public void changePassword(final String oldPassword, final String newPassword)
            throws IllegalAccessException {
        Objects.requireNonNull(oldPassword);
        Objects.requireNonNull(newPassword);

        if (!Objects.equals(oldPassword, password)) {
            throw new IllegalAccessException("Invalid password");
        }
        password = newPassword;
    }

    public UserId id() {
        return userId;
    }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", username='" + username + '\'' + ", password='" + password + '\'' + '}';
    }
}
