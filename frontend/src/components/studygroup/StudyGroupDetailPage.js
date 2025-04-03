import React, { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import { getStudyGroupById } from "../../api/studyGroupService";
import SessionForm from "../sessions/SessionForm";
import SessionList from "../sessions/SessionList";
import "./StudyGroupDetail.css"; 

const StudyGroupDetailPage = () => {
  const { groupId } = useParams();

  const [refreshSessions, setRefreshSessions] = useState(false);
  const [group, setGroup] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const handleSessionCreated = useCallback(() => {
    setRefreshSessions(prev => !prev);
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
      try {
        const response = await getStudyGroupById(groupId);
        setGroup(response.data);
      } catch (err) {
        console.error("Failed to fetch group info:", err);
        setError(err.response?.data?.message || "Could not load group details. Please ensure you are logged in and the group exists.");
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
        <p style={{ color: 'red', padding: '1rem', border: '1px solid red', backgroundColor: '#ffebee' }}>{error}</p>
      ) : group ? (
        <>
          <div className="group-info-card">
            <h2>{group.name || "Study Group Details"}</h2>
            <p>{group.description || "No description provided."}</p>
            <p><strong>Owner:</strong> {group.owner || 'N/A'}</p>
            <p><strong>Members ({group.memberIds?.length || 0}):</strong> {group.memberIds?.join(', ') || 'None'}</p>
          </div>

          <hr />

          <div className="session-section">
            <SessionForm groupId={groupId} onSessionCreated={handleSessionCreated} />
            <SessionList groupId={groupId} key={refreshSessions} />
          </div>
        </>
      ) : (
         <p>Group details not available.</p>
      )}
    </div>
  );
};

export default StudyGroupDetailPage;

