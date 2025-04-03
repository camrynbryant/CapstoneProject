import React, { useEffect, useState, useCallback } from "react";
import {
  getAllStudyGroups,
  updateStudyGroup,
  deleteStudyGroup,
} from "../../api/studyGroupService";
import { jwtDecode } from "jwt-decode";
import { Link } from "react-router-dom";

const getCurrentUser = () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) return null;
    const decoded = jwtDecode(token);
    return { email: decoded.sub, name: decoded.name };
  } catch (error) {
    console.error("Failed to decode token or token not found:", error);
    return null;
  }
};

const StudyGroupList = ({ refresh, showDetailLinks }) => {
  const [groups, setGroups] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [feedbackMessage, setFeedbackMessage] = useState("");
  const [feedbackType, setFeedbackType] = useState("");
  const [confirmingDeleteGroupId, setConfirmingDeleteGroupId] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchGroups = useCallback(async () => {
    setLoading(true);
    setFeedbackMessage("");
    setFeedbackType("");
    try {
      const response = await getAllStudyGroups();
      setGroups(response.data || []);
    } catch (error) {
      console.error("Failed to fetch study groups", error);
      setFeedbackMessage("Failed to load study groups.");
      setFeedbackType("error");
      setGroups([]);
    } finally {
       setLoading(false);
    }
  }, []);


  useEffect(() => {
    setCurrentUser(getCurrentUser());
  }, []);

  useEffect(() => {
    fetchGroups();
  }, [refresh, fetchGroups]);

  const handleJoin = async (group) => {
    if (!currentUser) return;
    setFeedbackMessage("");
    setFeedbackType("");

    if (Array.isArray(group.memberIds) && group.memberIds.includes(currentUser.email)) {
      console.warn("User is already a member of this group.");
      setFeedbackMessage("You are already a member of this group.");
      setFeedbackType("error");
      return;
    }

    const updatedGroup = {
      ...group,
      memberIds: [...(group.memberIds || []), currentUser.email],
    };

    try {
      await updateStudyGroup(group.id, updatedGroup);
      setFeedbackMessage(`Successfully joined ${group.name}.`);
      setFeedbackType("success");
      fetchGroups();
    } catch (error) {
      console.error("Failed to join group:", error);
      setFeedbackMessage("Failed to join group.");
      setFeedbackType("error");
    }
  };

  const handleLeave = async (group) => {
    if (!currentUser) return;
    setFeedbackMessage("");
    setFeedbackType("");

    if (!Array.isArray(group.memberIds) || !group.memberIds.includes(currentUser.email)) {
      console.warn("User is not a member of this group.");
      setFeedbackMessage("You are not a member of this group.");
      setFeedbackType("error");
      return;
    }

    const updatedMembers = group.memberIds.filter((email) => email !== currentUser.email);
    const updatedGroup = { ...group, memberIds: updatedMembers };

    try {
      await updateStudyGroup(group.id, updatedGroup);
      setFeedbackMessage(`Successfully left ${group.name}.`);
      setFeedbackType("success");
      fetchGroups();
    } catch (error) {
      console.error("Failed to leave group:", error);
      setFeedbackMessage("Failed to leave group.");
      setFeedbackType("error");
    }
  };

  const handleDeleteRequest = (group) => {
     if (!currentUser) return;
     setFeedbackMessage("");
     setFeedbackType("");

     if (group.owner !== currentUser.email) {
       setFeedbackMessage("Only the group owner can delete the study group.");
       setFeedbackType("error");
       return;
     }
     setConfirmingDeleteGroupId(group.id);
   };

   const confirmDelete = async (group) => {
     try {
       await deleteStudyGroup(group.id);
       setFeedbackMessage(`Successfully deleted ${group.name}.`);
       setFeedbackType("success");
       setConfirmingDeleteGroupId(null);
       fetchGroups();
     } catch (error) {
       console.error("Failed to delete group:", error);
       setFeedbackMessage("Failed to delete group.");
       setFeedbackType("error");
       setConfirmingDeleteGroupId(null);
     }
   };

   const cancelDelete = () => {
     setConfirmingDeleteGroupId(null);
     setFeedbackMessage("");
     setFeedbackType("");
   };


  return (
    <div style={{ marginTop: '20px' }}>
      <h3>Available Study Groups</h3>

      {loading && <p>Loading groups...</p>}

      {feedbackMessage && (
        <p style={{
            color: feedbackType === 'error' ? 'red' : 'green',
            marginTop: '10px', marginBottom: '10px',
            padding: '8px',
            border: `1px solid ${feedbackType === 'error' ? 'red' : 'green'}`,
            borderRadius: '4px',
            backgroundColor: feedbackType === 'error' ? '#ffebee' : '#e8f5e9'
           }}>
          {feedbackMessage}
        </p>
      )}

      {!loading && groups.length === 0 && !feedbackMessage && (
        <p>No study groups found.</p>
      )}

      {!loading && groups.length > 0 && (
        <ul style={{ listStyle: 'none', padding: 0 }}>
          {groups.map((group) => {
            const isMember = currentUser && Array.isArray(group.memberIds) && group.memberIds.includes(currentUser.email);
            const isOwner = currentUser && group.owner === currentUser.email;
            const isConfirmingDelete = confirmingDeleteGroupId === group.id;

            return (
              <li
                key={group.id}
                style={{
                  border: "1px solid #ccc",
                  margin: "10px 0",
                  padding: "15px",
                  borderRadius: "4px",
                  backgroundColor: isConfirmingDelete ? '#fff9c4' : '#fff'
                }}
              >
                <h4>{group.name}</h4>
                <p>{group.description}</p>
                <p><strong>Owner:</strong> {group.ownerName || group.owner}</p>
                <p><strong>Members ({Array.isArray(group.memberIds) ? group.memberIds.length : 0}):</strong> {Array.isArray(group.memberIds) ? group.memberIds.join(", ") : "None"}</p>

                <div style={{ marginTop: "10px" }}>
                  {currentUser && !isMember && !isOwner && !isConfirmingDelete && (
                    <button onClick={() => handleJoin(group)} style={{ marginRight: '5px', padding: '5px 10px' }}>Join Group</button>
                  )}
                  {currentUser && isMember && !isOwner && !isConfirmingDelete && (
                    <button onClick={() => handleLeave(group)} style={{ marginRight: '5px', padding: '5px 10px' }}>Leave Group</button>
                  )}

                  {currentUser && isOwner && !isConfirmingDelete && (
                    <button onClick={() => handleDeleteRequest(group)} style={{ backgroundColor: "#f44336", color: "white", marginRight: '5px', padding: '5px 10px', border: 'none', borderRadius: '3px' }}>Delete Group</button>
                  )}

                  {isConfirmingDelete && (
                    <div style={{ marginTop: '10px' }}>
                      <span style={{ marginRight: '10px', color: '#d32f2f', fontWeight: 'bold' }}>Are you sure?</span>
                      <button onClick={() => confirmDelete(group)} style={{ backgroundColor: "#d32f2f", color: "white", marginRight: '5px', padding: '5px 10px', border: 'none', borderRadius: '3px' }}>Confirm Delete</button>
                      <button onClick={cancelDelete} style={{ padding: '5px 10px' }}>Cancel</button>
                    </div>
                  )}

                  {showDetailLinks && !isConfirmingDelete && (
                    <Link to={`/studygroups/${group.id}`} style={{ marginLeft: "10px" }}>View Sessions</Link>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

export default StudyGroupList;
