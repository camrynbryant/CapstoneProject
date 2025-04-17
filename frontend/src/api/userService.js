import axios from "axios";

const API_URL = "http://localhost:8080/api/users";

export const addUser = async (user) => {
  return await axios.post(API_URL, user);
};

export const getAllUsers = async () => {
  return await axios.get(API_URL);
};

export const uploadProfilePicture = async (file, token) => {
  const formData = new FormData();
  formData.append("file", file);
  return axios.post(`${API_URL}/profile-picture`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
      Authorization: `Bearer ${token}`,
    },
  });
};

export const getProfilePicture = async (token) => {
  return axios.get(`${API_URL}/profile-picture`, {
    headers: { Authorization: `Bearer ${token}` },
  });
};

export const getStudyInterests = async (userId, token) => {
  return axios.get(`${API_URL}/${userId}/interests`, {
    headers: { Authorization: `Bearer ${token}` },
  });
};

export const updateStudyInterests = async (userId, interests, token) => {
  return axios.put(`${API_URL}/${userId}/interests`, interests, {
    headers: { Authorization: `Bearer ${token}` },
  });
};
