import React, { useEffect, useState } from "react";
import axios from "axios";
import "./NotificationsPage.css";
import { markNotificationAsRead } from "../../api/notificationService";


const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [loadingSettings, setLoadingSettings] = useState(true);
  
  const userId = localStorage.getItem("userEmail");
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!userId) return;
    axios.get(`http://localhost:8080/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    .then((res) => {
      setNotificationsEnabled(res.data.notificationsEnabled);
      setLoadingSettings(false);
    })
    .catch((err) => {
      console.error("Error fetching user settings:", err);
      setLoadingSettings(false);
    });
  }, [userId, token]);

  useEffect(() => {
    if (!userId || !notificationsEnabled) return;
    axios
      .get(`http://localhost:8080/api/notifications/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => {
        setNotifications(res.data);
      })
      .catch((err) => console.error("Error fetching notifications:", err));
  }, [userId, token, notificationsEnabled]);

  const toggleNotifications = () => {
    axios
      .put(
        `http://localhost:8080/api/users/${userId}/notifications`,
        { notificationsEnabled: !notificationsEnabled },
        { headers: { Authorization: `Bearer ${token}` } }
      )
      .then((res) => {
        const newSetting = res.data.notificationsEnabled;
        setNotificationsEnabled(newSetting);
        if (newSetting) {
          axios
            .get(`http://localhost:8080/api/notifications/${userId}`, {
              headers: { Authorization: `Bearer ${token}` },
            })
            .then((res) => {
              setNotifications(res.data);
            })
            .catch((err) =>
              console.error("Error fetching notifications:", err)
            );
        } else {
          setNotifications([]);
        }
      })
      .catch((err) =>
        console.error("Error updating notification settings:", err)
      );
  };

  useEffect(() => {
    axios
      .get(`http://localhost:8080/api/notifications/${userId}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((res) => setNotifications(res.data))
      .catch((err) => console.error("Error fetching notifications:", err));
  }, [userId, token]);

  const markAsRead = (notificationId) => {
    markNotificationAsRead(notificationId, token)
      .then((updatedNotification) => {
        setNotifications((prev) =>
          prev.map((n) =>
            n.id === notificationId ? updatedNotification : n
          )
        );
      })
      .catch((err) => console.error("Error marking notification as read:", err));
  };

  

  return (
    <div className="notifications-page">
      <h2>Notifications</h2>
      {loadingSettings ? (
        <p>Loading settings...</p>
      ) : (
        <button
          onClick={toggleNotifications}
          className={notificationsEnabled ? "disable-button" : "enable-button"}
        >
          {notificationsEnabled ? "Disable Notifications" : "Enable Notifications"}
        </button>
      )}
      <div className="notifications-list">
        {notificationsEnabled ? (
          notifications.length === 0 ? (
            <p>No notifications yet.</p>
          ) : (
            notifications.map((notification) => (
              <div
                key={notification.id}
                className={`notification-card ${notification.read ? "read" : "unread"}`}
              >
                <h3>New Alert!</h3>
                <p>{notification.message}</p>
                <span className="timestamp">
                  {new Date(notification.createdAt).toLocaleString()}
                </span>
                {!notification.read && (
                  <button
                    className="mark-read-button"
                    onClick={() => markAsRead(notification.id)}
                  >
                    Mark as read
                  </button>
                )}
              </div>
            ))
          )
        ) : (
          <p>Notifications are disabled.</p>
        )}
      </div>
    </div>
  );
};

export default NotificationsPage;
