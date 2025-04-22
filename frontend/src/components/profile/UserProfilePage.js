import React, { useState, useEffect } from "react";
import { useParams, Navigate } from "react-router-dom";
import axios from "axios";
import "./UserProfilePage.css";

const UserProfilePage = () => {
  const { email } = useParams();
  const token = localStorage.getItem("token");

  const [userProfile, setUserProfile] = useState(null);
  const [joinedGroups, setJoinedGroups] = useState([]);
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) return;

    const fetchProfile = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/api/users/${email}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUserProfile(response.data);
      } catch (error) {
        console.error("Error fetching user profile:", error);
      }
    };

    const fetchJoinedGroups = async () => {
      try {
        const resp = await axios.get(`http://localhost:8080/api/studygroups/joined/${email}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        setJoinedGroups(resp.data || []);
      } catch (error) {
        console.error("Error fetching joined study groups:", error);
      }
    };

    const fetchAchievements = async () => {
      try {
        const resp = await axios.get(`http://localhost:8080/api/achievements/user/${email}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (resp.data) {
          const sorted = resp.data.sort(
            (a, b) => new Date(b.earnedAt) - new Date(a.earnedAt)
          );
          setAchievements(sorted);
        }
      } catch (error) {
        console.error("Error fetching achievements:", error);
      }
    };

    Promise.all([fetchProfile(), fetchJoinedGroups(), fetchAchievements()])
      .finally(() => setLoading(false));
  }, [email, token]);

  if (!token) return <Navigate to="/login" />;
  if (loading) return <div>Loading...</div>;
  if (!userProfile) return <div>No profile found for {email}</div>;

  return (
    <div className="profile-page">
      <h1>{userProfile.email}'s Profile</h1>

      <div className="profile-picture-container">
  <div className="profile-picture-placeholder">
    <span>{userProfile.email.charAt(0).toUpperCase()}</span>
  </div>
</div>

      <div className="study-interests-section profile-section">
        <h2>Study Interests</h2>
        {userProfile.studyInterests && userProfile.studyInterests.length > 0 ? (
          <ul className="interest-list">
            {userProfile.studyInterests.map((interest, idx) => (
              <li key={idx} className="interest-item">{interest}</li>
            ))}
          </ul>
        ) : (
          <p>No study interests specified.</p>
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
          <p>This user has not joined any study groups.</p>
        )}
      </div>

      <div className="achievements-section profile-section">
        <h2>Achievements</h2>
        {achievements.length > 0 ? (
          <ul className="achievements-list">
            {achievements.map((ach, idx) => (
              <li key={idx}>
                <strong>{ach.achievementName}</strong>
                <p>{ach.achievementDescription}</p>
                <span>
                  Earned: {new Date(ach.earnedAt).toLocaleDateString()}
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <p>No achievements available.</p>
        )}
      </div>
    </div>
  );
};

export default UserProfilePage;
