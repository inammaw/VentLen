package com.venturelens.utils;

import com.venturelens.model.User;
import com.venturelens.model.Venture;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton managing user session and active venture context across all UI panels.
 */
public final class UserSession {

    private static UserSession instance;

    private User currentUser;
    private Venture activeVenture;
    private final List<SessionListener> listeners = new ArrayList<>();

    public interface SessionListener {
        void onUserLoggedIn(User user);
        void onUserLoggedOut();
        void onActiveVentureChanged(Venture venture);
    }

    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            for (SessionListener listener : listeners) {
                listener.onUserLoggedIn(user);
            }
        } else {
            for (SessionListener listener : listeners) {
                listener.onUserLoggedOut();
            }
        }
    }

    public Venture getActiveVenture() {
        return activeVenture;
    }

    public void setActiveVenture(Venture venture) {
        this.activeVenture = venture;
        for (SessionListener listener : listeners) {
            listener.onActiveVentureChanged(venture);
        }
    }

    public void logout() {
        this.currentUser = null;
        this.activeVenture = null;
        for (SessionListener listener : listeners) {
            listener.onUserLoggedOut();
        }
    }

    public void addListener(SessionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SessionListener listener) {
        listeners.remove(listener);
    }
}
