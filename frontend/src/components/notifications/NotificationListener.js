import { useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { getUpcomingNotifications } from '../../api/notificationService';
import axios from 'axios';

const NotificationListener = ({ userId, token }) => {
  const shownNotifications = useRef(new Set());
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  useEffect(() => {
    if (!userId) return;
    
    axios.get(`http://localhost:8080/api/users/${userId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
    .then((res) => {
      setNotificationsEnabled(res.data.notificationsEnabled);
    })
    .catch((err) => {
      console.error("Error fetching notification settings:", err);
    });
  }, [userId, token]);

  useEffect(() => {
    if (!notificationsEnabled) {
      return;
    }

    const checkNotifications = async () => {
      try {
        const response = await getUpcomingNotifications(userId, token);
        if (response.data && response.data.length > 0) {
          response.data.forEach(notification => {
            if (!shownNotifications.current.has(notification.id)) {
              toast.info(notification.message, {
                position: "top-right",
                autoClose: false,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
              });
              shownNotifications.current.add(notification.id);
            }
          });
        }
      } catch (error) {
        console.error('Error fetching notifications:', error);
      }
    };

    checkNotifications();
    const interval = setInterval(checkNotifications, 30000);

    return () => clearInterval(interval);
  }, [userId, token, notificationsEnabled]); 

  return null;
};

export default NotificationListener;
