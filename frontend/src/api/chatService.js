import axios from 'axios';

const API_BASE = "http://localhost:8080/api/chat";

export const getChatHistory = async (groupId) => {
  const token = localStorage.getItem("token");
  return await axios.get(`${API_BASE}/${groupId}/history`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};