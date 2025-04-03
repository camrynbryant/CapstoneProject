import React, { useState } from "react";
import { createSession } from "../../api/sessionService";

const SessionForm = ({ groupId, onSessionCreated }) => {
  const [formData, setFormData] = useState({
    topic: "",
    description: "",
    startTime: "",
    endTime: "",
    location: ""
  });
  const [loading, setLoading] = useState(false);
  const [feedbackMessage, setFeedbackMessage] = useState("");
  const [feedbackType, setFeedbackType] = useState("");

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setFeedbackMessage("");
    setFeedbackType("");

    const topic = formData.topic.trim();
    const description = formData.description.trim();
    const startTime = formData.startTime;
    const endTime = formData.endTime;
    const location = formData.location.trim();

    if (!topic) {
        setFeedbackMessage("Topic is required.");
        setFeedbackType("error");
        setLoading(false);
        return;
    }
    if (!startTime) {
        setFeedbackMessage("Start Time is required.");
        setFeedbackType("error");
        setLoading(false);
        return;
    }
    if (!endTime) {
        setFeedbackMessage("End Time is required.");
        setFeedbackType("error");
        setLoading(false);
        return;
    }
    if (!location) {
        setFeedbackMessage("Location is required.");
        setFeedbackType("error");
        setLoading(false);
        return;
    }

    if (new Date(endTime) <= new Date(startTime)) {
      setFeedbackMessage("End time must be after start time.");
      setFeedbackType("error");
      setLoading(false);
      return;
    }

    const sessionData = {
      topic,
      description,
      startTime,
      endTime,
      location, 
      groupId,
      participantIds: []
    };

    try {
      await createSession(sessionData);
      setFeedbackMessage("Session created successfully!");
      setFeedbackType("success");
      onSessionCreated();
      setFormData({
        topic: "",
        description: "",
        startTime: "",
        endTime: "",
        location: ""
      });
    } catch (error) {
      console.error("Error creating session:", error);
      let message = "Failed to create session.";
      if (error.response) {
          if (error.response.status === 403) {
              message = "Creation failed: You must be a member of this group to create a session.";
          } else if (error.response.data?.message) {
              message = `Creation failed: ${error.response.data.message}`;
          } else {
              message = `Creation failed: Server responded with status ${error.response.status}.`;
          }
      } else if (error.request) {
          message = "Creation failed: Could not connect to the server.";
      } else {
          message = `Creation failed: ${error.message || "Unknown error"}`;
      }
      setFeedbackMessage(message);
      setFeedbackType("error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="session-form">
      <h3>Schedule a Study Session</h3>
      <form onSubmit={handleSubmit} noValidate>
        <div>
          <label htmlFor="sessionTopic">Topic:</label>
          <input id="sessionTopic" type="text" name="topic" value={formData.topic} onChange={handleChange} required />
        </div>
        <div>
          <label htmlFor="sessionDescription">Description:</label>
          <textarea id="sessionDescription" name="description" value={formData.description} onChange={handleChange} />
        </div>
        <div>
          <label htmlFor="sessionStartTime">Start Time:</label>
          <input id="sessionStartTime" type="datetime-local" name="startTime" value={formData.startTime} onChange={handleChange} required />
        </div>
        <div>
          <label htmlFor="sessionEndTime">End Time:</label>
          <input id="sessionEndTime" type="datetime-local" name="endTime" value={formData.endTime} onChange={handleChange} required />
        </div>
        <div>
          <label htmlFor="sessionLocation">Location:</label>
          <input id="sessionLocation" type="text" name="location" value={formData.location} onChange={handleChange} required />
        </div>

        {feedbackMessage && (
          <p className={feedbackType === 'success' ? 'feedback-success' : ''}>
            {feedbackMessage}
          </p>
        )}

        <button type="submit" disabled={loading}>
          {loading ? "Creating..." : "Create Session"}
        </button>
      </form>
    </div>
  );
};

export default SessionForm;
