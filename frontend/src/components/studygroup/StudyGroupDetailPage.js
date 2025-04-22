import React, { useEffect, useState, useCallback } from "react";
import { useParams, Link } from "react-router-dom";
import { getStudyGroupById } from "../../api/studyGroupService";
import fileService from "../../api/fileService";
import SessionForm from "../sessions/SessionForm";
import SessionList from "../sessions/SessionList";
import "./StudyGroupDetail.css";
import { jwtDecode } from "jwt-decode";

const getCurrentUserEmail = () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) return null;
    const decoded = jwtDecode(token);
    return decoded.sub;
  } catch (error) {
    console.error("Failed to decode token or token not found:", error);
    return null;
  }
};

const StudyResources = ({ groupId }) => {
  const [files, setFiles] = useState([]);
  const [selectedFiles, setSelectedFiles] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [downloadError, setDownloadError] = useState("");
  const [deleteError, setDeleteError] = useState("");
  const [deleteSuccess, setDeleteSuccess] = useState("");

  const currentUserEmail = getCurrentUserEmail();

  const fetchFiles = useCallback(async () => {
    if (!groupId) return;
    setLoadingFiles(true);
    setError("");
    setSuccess("");
    setDownloadError("");
    setDeleteError("");
    setDeleteSuccess("");
    try {
      const response = await fileService.getFilesForGroup(groupId);
      setFiles(response.data || []);
    } catch (err) {
      console.error("Failed to fetch files:", err);
      setError(err.response?.data?.message || err.response?.data || "Could not load files.");
      setFiles([]);
    } finally {
      setLoadingFiles(false);
    }
  }, [groupId]);

  useEffect(() => {
    fetchFiles();
  }, [fetchFiles]);

  const handleFileChange = (event) => {
    setSelectedFiles(event.target.files);
    setError("");
    setSuccess("");
    setDownloadError("");
    setDeleteError("");
    setDeleteSuccess("");
  };

  const handleUpload = async () => {
    if (!selectedFiles || selectedFiles.length === 0) {
      setError("Please select one or more files to upload.");
      return;
    }
    setError("");
    setSuccess("");
    setDownloadError("");
    setDeleteError("");
    setDeleteSuccess("");
    setUploading(true);
    try {
      const response = await fileService.uploadFiles(groupId, selectedFiles);
      if (response.status === 207 && response.data) {
        const successes = response.data.successes || [];
        const failures = response.data.failures || [];
        let successMsg = successes.length > 0 ? `${successes.length} file(s) uploaded successfully.` : "";
        let errorMsg = failures.length > 0 ? ` Failed uploads: ${failures.join(', ')}` : "";
        setSuccess(successMsg);
        setError(errorMsg);
      } else {
        const uploadedCount = Array.isArray(response.data) ? response.data.length : 1;
        setSuccess(`${uploadedCount} file(s) uploaded successfully!`);
      }
      setSelectedFiles(null);
      document.getElementById("file-input").value = "";
      fetchFiles();
    } catch (err) {
      console.error("Upload failed:", err);
      let errorMsg = "File upload failed.";
      if (err.response?.data) {
        if (Array.isArray(err.response.data)) {
          errorMsg = err.response.data.join("; ");
        } else if (typeof err.response.data === "string") {
          errorMsg = err.response.data;
        } else if (err.response.data.message) {
          errorMsg = err.response.data.message;
        }
      }
      setError(errorMsg + " Check file types/sizes.");
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (resourceId, filename) => {
    setError("");
    setSuccess("");
    setDownloadError("");
    setDeleteError("");
    setDeleteSuccess("");
    try {
      await fileService.downloadFile(resourceId, filename);
    } catch (err) {
      console.error("Download failed:", err);
      setDownloadError(err.message || "Failed to initiate download.");
    }
  };

  const handleDelete = async (resourceId, filename) => {
    setError("");
    setSuccess("");
    setDownloadError("");
    setDeleteError("");
    setDeleteSuccess("");
    if (!window.confirm(`Are you sure you want to delete the file "${filename}"? This action cannot be undone.`)) {
      return;
    }
    try {
      await fileService.deleteFile(resourceId);
      setDeleteSuccess(`File "${filename}" deleted successfully.`);
      fetchFiles();
    } catch (err) {
      console.error("Delete failed:", err);
      setDeleteError(err.response?.data || err.message || `Failed to delete file "${filename}".`);
    }
  };

  return (
    <div className="study-resources-section">
      <h4>Study Resources</h4>
      <div className="upload-form">
        <h5>Upload New Resource(s)</h5>
        <input id="file-input" type="file" multiple onChange={handleFileChange} disabled={uploading} />
        <button onClick={handleUpload} disabled={!selectedFiles || selectedFiles.length === 0 || uploading}>
          {uploading ? `Uploading ${selectedFiles?.length || ""} file(s)...` : "Upload Selected"}
        </button>
        {error && <p className="error-message">{error}</p>}
        {success && <p className="success-message">{success}</p>}
      </div>
      <div className="file-list">
        <h5>Uploaded Files</h5>
        {deleteError && <p className="error-message">{deleteError}</p>}
        {deleteSuccess && <p className="success-message">{deleteSuccess}</p>}
        {loadingFiles ? (
          <p>Loading files...</p>
        ) : files.length === 0 && !error && !deleteError && !deleteSuccess ? (
          <p>No resources uploaded yet.</p>
        ) : (
          <>
            {downloadError && <p className="error-message">{downloadError}</p>}
            <ul>
              {files.map((file) => {
                const canDelete =
                  currentUserEmail &&
                  file.uploaderEmail &&
                  currentUserEmail.toLowerCase() === file.uploaderEmail.toLowerCase();
                return (
                  <li key={file.id}>
                    <span>
                      {file.filename} ({(file.size / 1024).toFixed(1)} KB) - Uploaded by {file.uploaderEmail} on{" "}
                      {new Date(file.uploadDate).toLocaleDateString()}
                    </span>
                    <div className="file-actions">
                      <button onClick={() => handleDownload(file.id, file.filename)} className="download-button">
                        Download
                      </button>
                      {canDelete && (
                        <button onClick={() => handleDelete(file.id, file.filename)} className="delete-button">
                          Delete
                        </button>
                      )}
                    </div>
                  </li>
                );
              })}
            </ul>
          </>
        )}
      </div>
    </div>
  );
};

const StudyGroupDetailPage = () => {
  const { groupId } = useParams();
  const [refreshSessions, setRefreshSessions] = useState(false);
  const [group, setGroup] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isMember, setIsMember] = useState(false);

  const handleSessionCreated = useCallback(() => {
    setRefreshSessions((prev) => !prev);
  }, []);

  useEffect(() => {
    const fetchGroup = async () => {
      if (!groupId) {
        setError("No Group ID provided.");
        setLoading(false);
        return;
      }
      setLoading(true);
      setError(null);
      setIsMember(false);
      const currentUserEmail = getCurrentUserEmail();
      try {
        const response = await getStudyGroupById(groupId);
        setGroup(response.data);
        if (currentUserEmail && response.data?.memberIds?.includes(currentUserEmail)) {
          setIsMember(true);
        }
      } catch (err) {
        console.error("Failed to fetch group info:", err);
        setError(
          err.response?.data?.message || err.response?.data || "Could not load group details. Please ensure you are logged in and the group exists."
        );
        setGroup(null);
      } finally {
        setLoading(false);
      }
    };
    fetchGroup();
  }, [groupId]);

  return (
    <div className="study-group-detail-page">
      {loading ? (
        <p>Loading group details...</p>
      ) : error ? (
        <p style={{ color: "red", padding: "1rem", border: "1px solid red", backgroundColor: "#ffebee" }}>{error}</p>
      ) : group ? (
        <>
          <div className="group-info-card">
            <h2>{group.name || "Study Group Details"}</h2>
            <p>{group.description || "No description provided."}</p>
            <p>
              <strong>Owner:</strong> {group.owner || "N/A"}
            </p>
{/* Members Section */}
<div className="group-members-section">
  <h3>Group Members</h3>
  {group.memberIds && group.memberIds.length > 0 ? (
    <p>
      {group.memberIds.map((memberEmail, idx) => (
        <React.Fragment key={memberEmail}>
          <Link to={`/profile/${memberEmail}`}>{memberEmail}</Link>
          {idx < group.memberIds.length - 1 && ", "}
        </React.Fragment>
      ))}
    </p>
  ) : (
    <p>No members found in this group.</p>
  )}
</div>
          </div>
          <hr />
          {isMember ? (
            <>
              <div className="study-resources-container">
                <StudyResources groupId={groupId} />
              </div>
              <div style={{ marginTop: "1rem" }}>
                <Link to={`/groups/${groupId}/chat`}>
                  <button className="chat-button">Open Group Chat</button>
                </Link>
              </div>
              <hr />
              <div className="session-section">
                <SessionForm groupId={groupId} onSessionCreated={handleSessionCreated} />
                <SessionList groupId={groupId} key={refreshSessions} />
              </div>
            </>
          ) : (
            <p style={{ fontStyle: "italic", marginTop: "1rem" }}>
              Join the group to view resources and sessions.
            </p>
          )}
        </>
      ) : (
        <p>Group details not available.</p>
      )}
    </div>
  );
};

export default StudyGroupDetailPage;
