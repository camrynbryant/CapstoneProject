import axios from "axios";

const API_URL = "http://localhost:8080/api/studygroups";

export const createStudyGroup = async (studyGroup) => {
  return await axios.post(`${API_URL}/add`, studyGroup);
};

export const getAllStudyGroups = async () => {
  return await axios.get(`${API_URL}/all`);
};

export const updateStudyGroup = async (id, studyGroup) => {
  return await axios.put(`${API_URL}/${id}`, studyGroup);
};

export const deleteStudyGroup = async (id) => {
  return await axios.delete(`${API_URL}/${id}`);
};
