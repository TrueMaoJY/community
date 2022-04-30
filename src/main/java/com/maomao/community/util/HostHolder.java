package com.maomao.community.util;

import com.maomao.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.
 */
@Component
public class HostHolder {

    private static ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public  static User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }

}
