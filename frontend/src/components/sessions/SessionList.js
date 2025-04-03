import React, { useState, useEffect, useCallback, useRef } from "react";
import sessionService from "../../api/sessionService";
import { jwtDecode } from "jwt-decode";

const getCurrentUserEmail = () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) return null;
    const decoded = jwtDecode(token);
    return decoded.sub;
  } catch (error) {
    console.error("Failed to decode token:", error);
    return null;
  }
};

const SessionItem = ({ session, currentUserId, onDelete, onJoinLeave, onUpdate }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState(null);
  const [editLoading, setEditLoading] = useState(false);
  const [editFeedback, setEditFeedback] = useState({ message: "", type: ""});
  const [confirmingDelete, setConfirmingDelete] = useState(false);
  const editTimeoutRef = useRef(null);

  useEffect(() => {
    return () => {
      if (editTimeoutRef.current) {
        clearTimeout(editTimeoutRef.current);
      }
    };
  }, []);

  const clearEditFeedback = () => {
      setEditFeedback({ message: "", type: "" });
      editTimeoutRef.current = null;
  };

  const isParticipant = session.participantIds?.includes(currentUserId);
  const isCreator = session.createdBy === currentUserId;

  const handleDeleteRequest = () => { setConfirmingDelete(true); };
  const handleCancelDelete = () => { setConfirmingDelete(false); };
  const handleConfirmDelete = () => { onDelete(session.id); setConfirmingDelete(false); };
  const handleJoin = () => { onJoinLeave(session.id, currentUserId, 'join'); };
  const handleLeave = () => { onJoinLeave(session.id, currentUserId, 'leave'); };

  const handleEditClick = () => {
    const formatForInput = (dateString) => {
        if (!dateString) return "";
        try {
            const date = new Date(dateString);
            const timezoneOffset = date.getTimezoneOffset() * 60000;
            const localISOTime = new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 16);
            return localISOTime;
        } catch (e) { return ""; }
    };
    setEditData({
      topic: session.topic || "", description: session.description || "",
      startTime: formatForInput(session.startTime), endTime: formatForInput(session.endTime),
      location: session.location || "", participantIds: session.participantIds || [],
      groupId: session.groupId, createdBy: session.createdBy
    });
    setIsEditing(true);
    setEditFeedback({ message: "", type: "" });
    setConfirmingDelete(false);
  };

  const handleCancelClick = () => { setIsEditing(false); setEditData(null); setEditFeedback({ message: "", type: "" }); };
  const handleEditChange = (e) => { setEditData({ ...editData, [e.target.name]: e.target.value }); };

  const handleSaveClick = async (e) => {
    e.preventDefault();
    if (editTimeoutRef.current) clearTimeout(editTimeoutRef.current);
    setEditLoading(true);
    setEditFeedback({ message: "", type: "" });

    const trimmedData = {
        ...editData, topic: editData.topic.trim(),
        description: editData.description.trim(), location: editData.location.trim(),
        startTime: editData.startTime, endTime: editData.endTime,
    };
    if (!trimmedData.topic || !trimmedData.startTime || !trimmedData.endTime) {
        setEditFeedback({ message: "Topic, Start Time, and End Time are required.", type: "error"});
        setEditLoading(false);
        editTimeoutRef.current = setTimeout(clearEditFeedback, 3000);
        return;
    }
    if (new Date(trimmedData.endTime) <= new Date(trimmedData.startTime)) {
      setEditFeedback({ message: "End time must be after start time.", type: "error"});
      setEditLoading(false);
      editTimeoutRef.current = setTimeout(clearEditFeedback, 3000);
      return;
    }
    try {
      await onUpdate(session.id, trimmedData);
      setEditFeedback({ message: "Session updated successfully!", type: "success"});
      setIsEditing(false);
      setEditData(null);
    } catch (error) {
      console.error("Error updating session:", error);
      setEditFeedback({ message: error.message || "Failed to update session.", type: "error"});
      editTimeoutRef.current = setTimeout(clearEditFeedback, 3000);
    } finally {
      setEditLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try { return new Date(dateString).toLocaleString(); }
    catch (e) { return 'Invalid Date'; }
  };

  const cardClassName = `session-item-card ${isEditing ? 'editing' : ''} ${confirmingDelete ? 'confirming-delete' : ''}`;

  if (isEditing) {
    return (
      <li className={cardClassName}>
        <h4>Edit Session</h4>
        <form onSubmit={handleSaveClick}>
           <div>
            <label>Topic:</label>
            <input type="text" name="topic" value={editData.topic} onChange={handleEditChange} required />
          </div>
          <div>
            <label>Description:</label>
            <textarea name="description" value={editData.description} onChange={handleEditChange} />
          </div>
          <div>
            <label>Start Time:</label>
            <input type="datetime-local" name="startTime" value={editData.startTime} onChange={handleEditChange} required />
          </div>
          <div>
            <label>End Time:</label>
            <input type="datetime-local" name="endTime" value={editData.endTime} onChange={handleEditChange} required />
          </div>
          <div>
            <label>Location:</label>
            <input type="text" name="location" value={editData.location} onChange={handleEditChange} />
          </div>
          {editFeedback.message && (
             <p className={`feedback-${editFeedback.type}`}>
                {editFeedback.message}
             </p>
          )}
          <div>
            <button type="submit" disabled={editLoading}>
              {editLoading ? 'Saving...' : 'Save Changes'}
            </button>
            <button type="button" onClick={handleCancelClick} disabled={editLoading}>
              Cancel
            </button>
          </div>
        </form>
      </li>
    );
  }

  return (
    <li className={cardClassName}>
       {editFeedback.message && editFeedback.type === 'success' && (
          <p className={`feedback-${editFeedback.type}`}>
             {editFeedback.message}
          </p>
       )}
      <h4>{session.topic || 'No Topic'}</h4>
      <p>{session.description || 'No description.'}</p>
      <p><strong>Starts:</strong> {formatDate(session.startTime)}</p>
      <p><strong>Ends:</strong> {formatDate(session.endTime)}</p>
      <p><strong>Location:</strong> {session.location || 'N/A'}</p>
      <p><strong>Participants ({session.participantIds?.length || 0}):</strong> {session.participantIds?.join(', ') || 'None'}</p>
      <p><small>Created by: {session.createdBy || 'Unknown'}</small></p>
      <div>
        {currentUserId && !isParticipant && !confirmingDelete && !isEditing && ( <button onClick={handleJoin} className="join-btn">Join</button> )}
        {currentUserId && isParticipant && !isCreator && !confirmingDelete && !isEditing && ( <button onClick={handleLeave} className="leave-btn">Leave</button> )}
        {currentUserId && isCreator && !isEditing && (
          <>
            {!confirmingDelete ? (
              <>
                <button onClick={handleEditClick} className="edit-btn">Edit</button>
                <button onClick={handleDeleteRequest} className="delete-btn">Delete</button>
              </>
            ) : (
              <div className="delete-confirm">
                <span>Delete?</span>
                <button onClick={handleConfirmDelete}>Confirm</button>
                <button onClick={handleCancelDelete}>Cancel</button>
              </div>
            )}
          </>
        )}
      </div>
    </li>
  );
};


const SessionList = ({ groupId }) => {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentUserEmail, setCurrentUserEmail] = useState(null);
  const [listFeedback, setListFeedback] = useState({ message: "", type: "" });
  const listTimeoutRef = useRef(null);

  useEffect(() => {
    return () => {
      if (listTimeoutRef.current) {
        clearTimeout(listTimeoutRef.current);
      }
    };
  }, []);

   const clearListFeedback = () => {
      setListFeedback({ message: "", type: "" });
      listTimeoutRef.current = null;
  };


  const fetchSessions = useCallback(async () => {
    if (!groupId) { setSessions([]); return; }
    setLoading(true); setError(null);
    if (listTimeoutRef.current) clearTimeout(listTimeoutRef.current);
    setListFeedback({ message: "", type: "" });
    try {
      const response = await sessionService.getSessionsByGroup(groupId);
      setSessions(response.data || []);
    } catch (err) {
      console.error("Error fetching sessions:", err);
      let message = "Failed to load sessions.";
       if (err.response) {
           if (err.response.status === 403) { message = "You must be a member of this group to view its sessions."; }
           else if (err.response.data?.message) { message = `Failed to load sessions: ${err.response.data.message}`; }
           else { message = `Failed to load sessions: Server responded with status ${err.response.status}.`; }
       } else if (err.request) { message = "Failed to load sessions: Could not connect to the server."; }
       else { message = `Failed to load sessions: ${err.message || "Unknown error"}`; }
      setError(message);
      setSessions([]);
    } finally { setLoading(false); }
  }, [groupId]);

  useEffect(() => { setCurrentUserEmail(getCurrentUserEmail()); fetchSessions(); }, [fetchSessions]);

  const handleDeleteSession = async (sessionId) => {
    if (listTimeoutRef.current) clearTimeout(listTimeoutRef.current);
    setListFeedback({ message: "Deleting...", type: "info" }); setError(null);
    try {
      await sessionService.deleteSession(sessionId);
      setListFeedback({ message: "Session deleted successfully.", type: "success" });
      listTimeoutRef.current = setTimeout(clearListFeedback, 3000);
      fetchSessions();
    } catch (err) {
      console.error("Error deleting session:", err);
      const message = err.response?.data?.message || err.message || "Failed to delete session.";
      setListFeedback({ message, type: "error" });
      listTimeoutRef.current = setTimeout(clearListFeedback, 3000);
    }
  };

  const handleJoinLeaveSession = async (sessionId, userId, action) => {
     if (!userId) { setListFeedback({ message: "Could not identify user.", type: "error" }); return; }
     if (listTimeoutRef.current) clearTimeout(listTimeoutRef.current);
     setListFeedback({ message: "", type: "" }); setError(null);
    try {
      if (action === 'join') { await sessionService.joinSession(sessionId, userId); }
      else if (action === 'leave') { await sessionService.leaveSession(sessionId, userId); }
      setListFeedback({ message: `Successfully ${action}ed session.`, type: "success" });
      listTimeoutRef.current = setTimeout(clearListFeedback, 3000);
      fetchSessions();
    } catch (err) {
      console.error(`Error ${action}ing session:`, err);
      const message = err.response?.data?.message || err.message || `Failed to ${action} session.`;
      setListFeedback({ message, type: "error" });
      listTimeoutRef.current = setTimeout(clearListFeedback, 3000);
    }
  };

  const handleUpdateSession = (sessionId, updatedData) => {
    if (listTimeoutRef.current) clearTimeout(listTimeoutRef.current);
    setListFeedback({ message: "", type: "" });
    setError(null);
    return new Promise(async (resolve, reject) => {
        try {
            await sessionService.updateSession(sessionId, updatedData);
            fetchSessions();
            resolve();
        } catch (err) {
            console.error("Error updating session in list:", err);
            const message = err.response?.data?.message || err.message || "Failed to update session.";
            setListFeedback({ message, type: "error" });
            listTimeoutRef.current = setTimeout(clearListFeedback, 3000);
            reject(new Error(message));
        }
    });
  };


  return (
    <div className="session-list">
      <h3>Study Sessions for this Group</h3>
      {loading && <p>Loading sessions...</p>}
      {listFeedback.message && ( <p className={`feedback-${listFeedback.type}`}> {listFeedback.message} </p> )}
      {error && !listFeedback.message && <p className="feedback-error">{error}</p>}
      {!loading && !error && sessions.length === 0 && !listFeedback.message && ( <p>No study sessions scheduled for this group yet.</p> )}
      {!loading && !error && sessions.length > 0 && (
        <ul>
          {sessions.map((session) => (
            <SessionItem key={session.id} session={session} currentUserId={currentUserEmail}
              onDelete={handleDeleteSession} onJoinLeave={handleJoinLeaveSession} onUpdate={handleUpdateSession} />
          ))}
        </ul>
      )}
    </div>
  );
};

export default SessionList;
