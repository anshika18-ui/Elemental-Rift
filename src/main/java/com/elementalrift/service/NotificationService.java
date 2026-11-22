package com.elementalrift.service;

import com.elementalrift.interfaces.Notifiable;
import javafx.application.Platform;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe notification service dispatching combat alerts, wave updates, and warnings.
 * [OOP Requirement: Interface - Notifiable]
 * [OOP Requirement: Multithreading - thread-safe dispatch using Platform.runLater]
 */
public class NotificationService {
    private final List<Notifiable> listeners = new CopyOnWriteArrayList<>();
    private final List<String> messageHistory = new CopyOnWriteArrayList<>();

    public void addListener(Notifiable listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Notifiable listener) {
        listeners.remove(listener);
    }

    /**
     * Broadcasts notification safely to all UI listeners on the JavaFX Application Thread.
     */
    public void notify(String message, String type) {
        messageHistory.add(String.format("[%s] %s", type, message));
        if (messageHistory.size() > 50) {
            messageHistory.remove(0);
        }

        // Ensure UI updates execute on the JavaFX Application Thread
        if (Platform.isFxApplicationThread()) {
            for (Notifiable listener : listeners) {
                listener.onNotification(message, type);
            }
        } else {
            Platform.runLater(() -> {
                for (Notifiable listener : listeners) {
                    listener.onNotification(message, type);
                }
            });
        }
    }

    public List<String> getMessageHistory() {
        return messageHistory;
    }
}
