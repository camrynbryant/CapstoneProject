import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api/notifications";

export const getUnreadCount = async (userId, token) => {
  const response = await axios.get(`${API_BASE_URL}/${userId}/unreadCount`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return response.data;
};

export const markNotificationAsRead = async (notificationId, token) => {
  const response = await axios.put(
    `${API_BASE_URL}/${notificationId}/read`,
    {},
    { headers: { Authorization: `Bearer ${token}` } }
  );
  return response.data;
};

export const getUpcomingNotifications = async (userId, token) => {
    return await axios.get(`${API_BASE_URL}/${userId}/upcoming`, {
        headers: { Authorization: `Bearer ${token}` }
    });
};

  
