import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api"; 


export const getMyAchievements = async (token) => {
  if (!token) {
    return Promise.reject(new Error("Authentication token is missing."));
  }
  return axios.get(`${API_BASE_URL}/achievements/me`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
};