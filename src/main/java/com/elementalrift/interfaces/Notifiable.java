package com.elementalrift.interfaces;

/**
 * Interface for receiving in-game notifications, combat alerts, and wave updates.
 * [OOP Requirement: Interface 4 of 4]
 */
public interface Notifiable {
    /**
     * Dispatches a notification event to listener objects.
     * @param message Text of the notification
     * @param type Category (INFO, WARNING, SUCCESS, DANGER)
     */
    void onNotification(String message, String type);
}
