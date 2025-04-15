import axios from "axios";

const API_URL = "http://localhost:8080/api/studygroups";


const getAuthHeader = () => {
  const token = localStorage.getItem("token");
  const headers = {
    "Content-Type": "application/json", 
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  return { headers };
};

export const createStudyGroup = async (studyGroup) => {
  return await axios.post(`${API_URL}/add`, studyGroup, getAuthHeader());
};

export const getAllStudyGroups = async () => {
  return await axios.get(`${API_URL}/all`);
};


export const getStudyGroupById = async (id) => {
   
    return await axios.get(`${API_URL}/${id}`, getAuthHeader());
};


export const updateStudyGroup = async (id, studyGroup) => {
  return await axios.put(`${API_URL}/${id}`, studyGroup, getAuthHeader());
};

export const deleteStudyGroup = async (id) => {
  return await axios.delete(`${API_URL}/${id}`, getAuthHeader());
};

const studyGroupService = {
    createStudyGroup,
    getAllStudyGroups,
    getStudyGroupById, 
    updateStudyGroup,
    deleteStudyGroup
};

export default studyGroupService;
