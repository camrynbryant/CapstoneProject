import React, { useState, useRef, useEffect } from "react";
import {
  uploadProfilePicture,
  getStudyInterests,
  updateStudyInterests,
} from "../../api/userService";
import { getJoinedStudyGroups } from "../../api/studyGroupService";
import { getMyAchievements } from "../../api/achievementService";

import AchievementList from './AchievementList';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrash } from '@fortawesome/free-solid-svg-icons';
import "./Profile.css";

const Profile = () => {
  const userId = localStorage.getItem("userEmail") || "User";
  const token = localStorage.getItem("token");
  const [profilePic, setProfilePic] = useState(localStorage.getItem("profilePic") || null);
  const fileInputRef = useRef(null);

  const [studyInterests, setStudyInterests] = useState([]);
  const [isAddingInterest, setIsAddingInterest] = useState(false);
  const [newInterest, setNewInterest] = useState("");

  const [joinedGroups, setJoinedGroups] = useState([]);

  const [achievements, setAchievements] = useState([]);
  const [loadingAchievements, setLoadingAchievements] = useState(false);

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const localUrl = URL.createObjectURL(file);
    setProfilePic(localUrl);
    try {
      const response = await uploadProfilePicture(file, token);
      if (response.data && response.data.pictureUrl) {
        setProfilePic(response.data.pictureUrl);
        localStorage.setItem("profilePic", response.data.pictureUrl);
        URL.revokeObjectURL(localUrl);
      }
    } catch (error) {
      console.error("Error uploading profile picture", error);
    }
  };

  useEffect(() => {
    if (!token || !userId) return;

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
    const fetchJoinedGroups = async () => { 
        try {
            const resp = await getJoinedStudyGroups(userId, token);
            if (resp.data) {
                setJoinedGroups(resp.data);
            }
        } catch (err) {
            console.error("Error fetching joined study groups:", err);
        }
    };

    const fetchAchievements = async () => {
      setLoadingAchievements(true);
      try {
        const resp = await getMyAchievements(token);
        if (resp.data) {
          const sortedAchievements = resp.data.sort((a, b) =>
            new Date(b.earnedAt) - new Date(a.earnedAt)
          );
          setAchievements(sortedAchievements);
        } else {
          setAchievements([]); 
        }
      } catch (err) {
        console.error("Error fetching achievements:", err);
        setAchievements([]); 
      } finally {
        setLoadingAchievements(false);
      }
    };

    fetchInterests();
    fetchJoinedGroups();
    fetchAchievements(); 

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
  const handleDeleteInterest = async (interestToDelete) => { 
    const updatedInterests = studyInterests.filter((interest) => interest !== interestToDelete);
    try {
      const resp = await updateStudyInterests(userId, updatedInterests, token);
      if (resp.data && resp.data.studyInterests) {
        setStudyInterests(resp.data.studyInterests);
      }
    } catch (error) {
      console.error("Error deleting interest:", error);
    }
   };

  const handleProfilePicClick = () => { 
    fileInputRef.current.click();
   };

  return (
    <div className="profile-page">
      <h1>Welcome, {userId}!</h1>

      <div className="profile-picture-container" onClick={handleProfilePicClick} title="Click to change picture">
        {profilePic ? (
          <img src={profilePic} alt="Profile" className="profile-picture" />
        ) : (
          <div className="profile-picture-placeholder">
            <span>{userId.charAt(0).toUpperCase()}</span>
          </div>
        )}
        <input
          type="file"
          ref={fileInputRef}
          style={{ display: "none" }}
          accept="image/*"
          onChange={handleFileChange}
        />
      </div>
      <button onClick={handleProfilePicClick} className="change-picture-button">
         Change Picture
      </button>

      <div className="study-interests-section profile-section">
        <h2>My Study Interests</h2>
        {studyInterests.length > 0 ? (
          <ul className="interest-list">
            {studyInterests.map((interest, idx) => (
              <li key={idx} className="interest-item">
                <span>{interest}</span>
                <FontAwesomeIcon
                  icon={faTrash}
                  className="delete-icon"
                  onClick={() => handleDeleteInterest(interest)}
                  title="Delete interest"
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
              className="interest-input"
            />
            <button onClick={handleAddInterest} className="add-button">Add</button>
            <button onClick={toggleAddInterest} className="cancel-button">Cancel</button>
          </div>
        ) : (
          <button onClick={toggleAddInterest} className="add-interest-button">Add Interest</button>
        )}
      </div>

      <div className="joined-groups-section profile-section">
        <h2>Joined Study Groups</h2>
        {joinedGroups.length > 0 ? (
          <ul className="group-list">
            {joinedGroups.map((group) => (
              <li key={group.id} className="group-item">
                <strong>{group.name}</strong>
                <p>{group.description}</p>
              </li>
            ))}
          </ul>
        ) : (
          <p>You have not joined any study groups.</p>
        )}
      </div>

      <div className="achievements-section profile-section">
        <h2>My Achievements</h2>
        <AchievementList
          achievements={achievements}
          loading={loadingAchievements}
        />
      </div>

    </div>
  );
};

export default Profile;