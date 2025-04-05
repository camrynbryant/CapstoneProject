import axios from "axios";
import * as jwtDecode from "jwt-decode";


const API_URL = "http://localhost:8080/api";

export const registerUser = async (userData) => {
  try {
    const response = await axios.post(`${API_URL}/register`, userData);
    return response.data;
  } catch (error) {
    console.error("Error inside authService.registerUser:", error); 
    let message = "Registration failed";
    if (error.response?.data) {
        message = typeof error.response.data === 'string' ? error.response.data : JSON.stringify(error.response.data);
    } else if (error.request) {
        message = "No response received from server. Please check if backend is running.";
    } else {
        message = error.message;
    }
    throw new Error(message);
  }
};

export const loginUser = async (userData) => {
  try {
    const response = await axios.post(`${API_URL}/login`, userData);
    if (!response.data?.token) {
        throw new Error("Login response did not include a token.");
    }
    const token = response.data.token;
    const decoded = jwtDecode.jwtDecode(token); 
    const userId = decoded.sub || decoded.email;
    return { token, userId, email: decoded.email };
  } catch (error) {
    console.error("Error inside authService.loginUser:", error); 
    let message = "Login failed";
    if (error.response?.data) {
       message = typeof error.response.data === 'string' ? error.response.data : JSON.stringify(error.response.data);
       if (message.toLowerCase().includes("invalid email or password")) {
           message = "Invalid email or password";
       }
    } else if (error.request) {
       message = "Could not connect to the server. Please ensure the backend is running.";
    } else {
       message = error.message;
    }
    throw new Error(message);
  }
};
