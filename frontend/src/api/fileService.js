import axios from "axios";

const API_URL = "http://localhost:8080/api";

const getAuthHeader = () => {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const uploadFiles = async (groupId, files) => {
  const formData = new FormData();
  for (let i = 0; i < files.length; i++) {
    formData.append("files", files[i]);
  }

  return axios.post(`${API_URL}/studygroups/${groupId}/files`, formData, {
    headers: {
      ...getAuthHeader(),
    },
  });
};

const getFilesForGroup = async (groupId) => {
  return axios.get(`${API_URL}/studygroups/${groupId}/files`, {
    headers: getAuthHeader(),
  });
};

const downloadFile = async (resourceId, filename) => {
  try {
    const response = await axios.get(`${API_URL}/files/${resourceId}`, {
      headers: getAuthHeader(),
      responseType: 'blob',
    });

    const blob = new Blob([response.data], { type: response.headers['content-type'] });
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(link.href);

  } catch (error) {
    console.error("Error downloading file:", error);
    let message = "Failed to download file.";
    if (error.response) {
      if (error.response.data instanceof Blob && error.response.data.type === "application/json") {
        try {
          const errJson = JSON.parse(await error.response.data.text());
          message = errJson.message || errJson.error || message;
        } catch (parseError) {}
      } else if (typeof error.response.data === 'string') {
        message = error.response.data;
      } else if (error.response.status === 404) {
        message = "File not found.";
      } else if (error.response.status === 403) {
        message = "You do not have permission to download this file.";
      }
    }
    throw new Error(message);
  }
};

const deleteFile = async (resourceId) => {
  return axios.delete(`${API_URL}/files/${resourceId}`, {
    headers: getAuthHeader(),
  });
};

const fileService = {
  uploadFiles,
  getFilesForGroup,
  downloadFile,
  deleteFile,
};

export default fileService;
