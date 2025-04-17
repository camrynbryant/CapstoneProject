// language: javascript
// filepath: /Users/kusi/Downloads/CapstoneProject-10/frontend/src/components/profile/Profile.js
import React, { useState, useRef, useEffect } from "react";
import { 
  uploadProfilePicture, 
  getStudyInterests, 
  updateStudyInterests 
} from "../../api/userService";
import { getJoinedStudyGroups } from "../../api/studyGroupService";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrash } from '@fortawesome/free-solid-svg-icons';
import "./Profile.css";

const Profile = () => {
  const userId = localStorage.getItem("userEmail") || "User";
  const token = localStorage.getItem("token");
  const [profilePic, setProfilePic] = useState(null);
  const fileInputRef = useRef(null);

  const [studyInterests, setStudyInterests] = useState([]);
  const [isAddingInterest, setIsAddingInterest] = useState(false);
  const [newInterest, setNewInterest] = useState("");

  const [joinedGroups, setJoinedGroups] = useState([]);

  // Handle file change and upload logic
  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    // Create a local preview (optional)
    const localUrl = URL.createObjectURL(file);
    setProfilePic(localUrl);
    try {
      const response = await uploadProfilePicture(file, token);
      if (response.data && response.data.pictureUrl) {
        setProfilePic(response.data.pictureUrl);
        localStorage.setItem("profilePic", response.data.pictureUrl);
      }
    } catch (error) {
      console.error("Error uploading profile picture", error);
    }
  };

  // Fetch study interests on mount
  useEffect(() => {
    const fetchInterests = async () => {
      try {
        const resp = await getStudyInterests(userId, token);
        if (resp.data && resp.data.studyInterests) {
          setStudyInterests(resp.data.studyInterests);
        }
      } catch (err) {
        console.error("Error fetching study interests:", err);
      }
    };
    if (token) fetchInterests();
  }, [token, userId]);

  // Fetch joined study groups on mount
  useEffect(() => {
    const fetchJoinedGroups = async () => {
      try {
        const resp = await getJoinedStudyGroups(userId);
        if (resp.data) {
          setJoinedGroups(resp.data);
        }
      } catch (err) {
        console.error("Error fetching joined study groups:", err);
      }
    };
    if (token) fetchJoinedGroups();
  }, [token, userId]);

  const toggleAddInterest = () => {
    setNewInterest("");
    setIsAddingInterest(!isAddingInterest);
  };

  const handleAddInterest = async () => {
    if (!newInterest.trim()) return;
    const updatedInterests = [...studyInterests, newInterest.trim()];
    try {
      const resp = await updateStudyInterests(userId, updatedInterests, token);
      if (resp.data && resp.data.studyInterests) {
        setStudyInterests(resp.data.studyInterests);
        setNewInterest("");
        setIsAddingInterest(false);
      }
    } catch (error) {
      console.error("Error adding interest:", error);
    }
  };

  const handleDeleteInterest = async (index) => {
    const updatedInterests = studyInterests.filter((_, idx) => idx !== index);
    try {
      const resp = await updateStudyInterests(userId, updatedInterests, token);
      if (resp.data && resp.data.studyInterests) {
        setStudyInterests(resp.data.studyInterests);
      }
    } catch (error) {
      console.error("Error deleting interest:", error);
    }
  };

  return (
    <div className="profile-page">
      <h1>Welcome, {userId}!</h1>
      <div className="profile-picture-container">
  {profilePic ? (
    <img src={profilePic} alt="Profile" className="profile-picture" />
  ) : (
    <div className="profile-picture-placeholder">
      <span>{userId.charAt(0).toUpperCase()}</span>
    </div>
  )}
      </div>
      <input
        type="file"
        ref={fileInputRef}
        style={{ display: "none" }}
        accept="image/*"
        onChange={handleFileChange}
      />
      {/* My Study Interests Section */}
      <div className="study-interests-section">
        <h2>My Study Interests</h2>
        {studyInterests.length > 0 ? (
          <ul>
            {studyInterests.map((interest, idx) => (
              <li key={idx}>
                {interest}
                <FontAwesomeIcon
                  icon={faTrash}
                  style={{ cursor: "pointer", marginLeft: "8px", color: "#f44336" }}
                  onClick={() => handleDeleteInterest(idx)}
                />
              </li>
            ))}
          </ul>
        ) : (
          <p>No study interests specified.</p>
        )}
        {isAddingInterest ? (
          <div className="add-interest">
            <input
              type="text"
              value={newInterest}
              onChange={(e) => setNewInterest(e.target.value)}
              placeholder="Enter new interest"
            />
            <button onClick={handleAddInterest}>Add</button>
            <button onClick={toggleAddInterest}>Cancel</button>
          </div>
        ) : (
          <button onClick={toggleAddInterest}>Add Interest</button>
        )}
      </div>
      
      {/* Joined Study Groups Section */}
      <div className="joined-groups-section">
        <h2>Joined Study Groups</h2>
        {joinedGroups.length > 0 ? (
          <ul>
            {joinedGroups.map((group) => (
              <li key={group.id}>
                <strong>{group.name}</strong>
                <p>{group.description}</p>
              </li>
            ))}
          </ul>
        ) : (
          <p>You have not joined any study groups.</p>
        )}
      </div>
    </div>
  );
};

export default Profile;