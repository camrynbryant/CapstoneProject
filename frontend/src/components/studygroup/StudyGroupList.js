import React, { useEffect, useState, useCallback } from "react";
import {
  getAllStudyGroups,
  updateStudyGroup,
  deleteStudyGroup,
} from "../../api/studyGroupService";
import { jwtDecode } from "jwt-decode";

const getCurrentUser = () => {
  try {
    const token = localStorage.getItem("token");
    if (!token) {
      return null;
    }
    const decoded = jwtDecode(token);
    return {
      email: decoded.sub,
      name: decoded.name,
    };
  } catch (error) {
    console.error("Failed to decode token or token not found:", error);
    return null;
  }
};

const StudyGroupList = ({ refresh }) => {
  const [groups, setGroups] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);

  const fetchGroups = useCallback(async () => {
    try {
      const response = await getAllStudyGroups();
      setGroups(response.data || []);
    } catch (error) {
      console.error("Failed to fetch study groups", error);
      setGroups([]);
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

    if (group.memberIds.includes(currentUser.email)) {
      console.warn("User is already a member of this group.");
      return;
    }

    const updatedGroup = {
      ...group,
      memberIds: [...group.memberIds, currentUser.email],
    };

    try {
      await updateStudyGroup(group.id, updatedGroup);
      fetchGroups();
    } catch (error) {
      console.error("Failed to join group:", error);
    }
  };

  const handleLeave = async (group) => {
    if (!currentUser) return;

    if (!group.memberIds.includes(currentUser.email)) {
      console.warn("User is not a member of this group.");
      return;
    }

    const updatedMembers = group.memberIds.filter(
      (email) => email !== currentUser.email
    );
    const updatedGroup = { ...group, memberIds: updatedMembers };

    try {
      await updateStudyGroup(group.id, updatedGroup);
      fetchGroups();
    } catch (error) {
      console.error("Failed to leave group:", error);
    }
  };

  const handleDelete = async (group) => {
    if (!currentUser) return;

    if (group.owner !== currentUser.email) {
      alert("Only the group owner can delete the study group.");
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete the group "${group.name}"? This action cannot be undone.`
    );
    if (!confirmed) return;

    try {
      await deleteStudyGroup(group.id);
      fetchGroups();
    } catch (error) {
      console.error("Failed to delete group:", error);
    }
  };

  return (
    <div>
      <h3>Available Study Groups</h3>
      {groups.length === 0 ? (
        <p>No study groups found.</p>
      ) : (
        <ul>
          {groups.map((group) => {
            const isMember =
              currentUser && group.memberIds.includes(currentUser.email);
            const isOwner = currentUser && group.owner === currentUser.email;

            return (
              <li
                key={group.id}
                style={{
                  border: "1px solid #ccc",
                  margin: "10px 0",
                  padding: "15px",
                  borderRadius: "4px",
                }}
              >
                <h4>{group.name}</h4>
                <p>{group.description}</p>
                <p>
                  <strong>Owner:</strong> {group.ownerName || group.owner}{" "}
                </p>
                <p>
                  <strong>Members ({group.memberIds.length}):</strong>{" "}
                  {group.memberIds.join(", ")}
                </p>

                <div style={{ marginTop: "10px" }}>
                  {currentUser && !isMember && !isOwner && (
                    <button onClick={() => handleJoin(group)}>
                      Join Group
                    </button>
                  )}
                  {currentUser && isMember && !isOwner && (
                    <button onClick={() => handleLeave(group)}>
                      Leave Group
                    </button>
                  )}
                  {currentUser && isOwner && (
                    <button
                      onClick={() => handleDelete(group)}
                      style={{ backgroundColor: "#f44336", color: "white" }}
                    >
                      Delete Group
                    </button>
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