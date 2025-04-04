import axios from "axios";

const API_URL = "http://localhost:8080/api/users"; 

export const addUser = async (user) => {
  return await axios.post(API_URL, user); 
};

export const getAllUsers = async () => {
  return await axios.get(API_URL); 
};