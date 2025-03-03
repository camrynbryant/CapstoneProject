import React, { useEffect, useState } from "react";
import { getAllStudyGroups, updateStudyGroup, deleteStudyGroup } from "../../api/studyGroupService";
import { jwtDecode } from "jwt-decode";

const StudyGroupList = () => {
  const [groups, setGroups] = useState([]);

  const fetchGroups = async () => {
    try {
      const response = await getAllStudyGroups();
      setGroups(response.data);
    } catch (error) {
      console.error("Failed to fetch study groups", error);
    }
  };

  useEffect(() => {
    fetchGroups();
  }, []);

  const getCurrentUser = () => {
    const token = localStorage.getItem("token");
    if (!token) return null;
    const decoded = jwtDecode(token);
    return {
      email: decoded.sub,
      name: decoded.name,
    };
  };

  const currentUser = getCurrentUser();

  const handleJoin = async (group) => {
    if (!currentUser) return;
    if (group.memberIds.includes(currentUser.email)) return;
    const updatedGroup = { ...group, memberIds: [...group.memberIds, currentUser.email] };
    try {
      await updateStudyGroup(group.id, updatedGroup);
      fetchGroups();
    } catch (error) {
      console.error("Failed to join group", error);
    }
  };

  const handleLeave = async (group) => {
    if (!currentUser) return;
    const updatedMembers = group.memberIds.filter(email => email !== currentUser.email);
    const updatedGroup = { ...group, memberIds: updatedMembers };
    try {
      await updateStudyGroup(group.id, updatedGroup);
      fetchGroups();
    } catch (error) {
      console.error("Failed to leave group", error);
    }
  };

  const handleDelete = async (group) => {
    if (!currentUser) return;
    if (group.owner !== currentUser.email) {
      alert("Only the group owner can delete the study group.");
      return;
    }
    const confirmed = window.confirm("Are you sure you want to delete the group?");
    if (!confirmed) return;
    try {
      await deleteStudyGroup(group.id);
      fetchGroups();
    } catch (error) {
      console.error("Failed to delete group", error);
    }
  };

  return (
    <div>
      <h3>Available Study Groups</h3>
      <ul>
        {groups.map((group) => (
          <li key={group.id} style={{ border: "1px solid #ccc", margin: "10px", padding: "10px" }}>
            <h4>{group.name}</h4>
            <p>{group.description}</p>
            <p>
              <strong>Owner:</strong> {group.owner}
            </p>
            <p>
              <strong>Members:</strong> {group.memberIds.join(", ")}
            </p>
            {currentUser && !group.memberIds.includes(currentUser.email) && (
              <button onClick={() => handleJoin(group)}>Join Group</button>
            )}
            {currentUser && group.memberIds.includes(currentUser.email) && currentUser.email !== group.owner && (
              <button onClick={() => handleLeave(group)}>Leave Group</button>
            )}
            {currentUser && group.owner === currentUser.email && (
              <>
                <button onClick={() => handleDelete(group)} style={{ backgroundColor: "#f44336", color: "white" }}>
                  Delete Group
                </button>
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default StudyGroupList;
