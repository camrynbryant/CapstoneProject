import axios from "axios";

const API_URL = "http://localhost:8080/api/sessions";

const getAuthHeader = () => {
  const token = localStorage.getItem("token");
  return {
    headers: {
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : undefined,
    }
  };
};

export const createSession = (sessionData) => {
  return axios.post(API_URL, sessionData, getAuthHeader());
};

export const getSessionsByGroup = (groupId) => {
  return axios.get(`${API_URL}/group/${groupId}`, getAuthHeader());
};

export const updateSession = (id, sessionData) => {
  return axios.put(`${API_URL}/${id}`, sessionData, getAuthHeader());
};

export const deleteSession = (id) => {
  return axios.delete(`${API_URL}/${id}`, getAuthHeader());
};

export const joinSession = (id, userId) => {
  return axios.put(`${API_URL}/${id}/join?userId=${userId}`, {}, getAuthHeader());
};

export const leaveSession = (id, userId) => {
  return axios.put(`${API_URL}/${id}/leave?userId=${userId}`, {}, getAuthHeader());
};

const sessionService = {
  createSession,
  getSessionsByGroup,
  updateSession,
  deleteSession,
  joinSession,
  leaveSession,
};

export default sessionService;
