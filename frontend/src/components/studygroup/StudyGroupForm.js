import React, { useState, useEffect, useRef } from "react";
import { useForm } from "react-hook-form";
import { jwtDecode } from "jwt-decode";
import { createStudyGroup } from "../../api/studyGroupService";

const StudyGroupForm = ({ onGroupCreated }) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm();
  const [feedbackMessage, setFeedbackMessage] = useState("");
  const [feedbackType, setFeedbackType] = useState("");
  const timeoutRef = useRef(null);

  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  const clearFeedback = () => {
      setFeedbackMessage("");
      setFeedbackType("");
      timeoutRef.current = null;
  };

  const onSubmit = async (data) => {
    if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
    }
    setFeedbackMessage("");
    setFeedbackType("");

    const token = localStorage.getItem("token");
    if (!token) {
      setFeedbackMessage("You must be logged in to create a study group.");
      setFeedbackType("error");
      timeoutRef.current = setTimeout(clearFeedback, 3000);
      return;
    }

    let userEmail = null;
    try {
        const user = jwtDecode(token);
        userEmail = user.sub;
    } catch (e) {
        console.error("Failed to decode token:", e);
        setFeedbackMessage("Invalid session. Please log in again.");
        setFeedbackType("error");
        timeoutRef.current = setTimeout(clearFeedback, 3000);
        return;
    }

     if (!userEmail) {
        setFeedbackMessage("Could not identify user from token.");
        setFeedbackType("error");
        timeoutRef.current = setTimeout(clearFeedback, 3000);
        return;
    }

    const groupData = {
      name: data.name,
      description: data.description,
      owner: userEmail,
      memberIds: [userEmail]
    };

    try {
      await createStudyGroup(groupData);
      setFeedbackMessage("Study group created successfully!");
      setFeedbackType("success");
      onGroupCreated();
      reset();
      timeoutRef.current = setTimeout(clearFeedback, 3000);
    } catch (error) {
      console.error("Failed to create study group:", error);
      setFeedbackMessage(error.response?.data?.body || error.message || "Failed to create study group.");
      setFeedbackType("error");
      timeoutRef.current = setTimeout(clearFeedback, 3000);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <h3>Create Study Group</h3>

      {feedbackMessage && (
        <p style={{
            color: feedbackType === 'error' ? '#721c24' : '#155724',
            backgroundColor: feedbackType === 'error' ? '#f8d7da' : '#d4edda',
            border: `1px solid ${feedbackType === 'error' ? '#f5c6cb' : '#c3e6cb'}`,
            padding: '0.75rem 1.25rem',
            marginBottom: '1rem',
            borderRadius: '0.25rem',
            fontSize: '0.9rem',
            textAlign: 'center'
           }}>
          {feedbackMessage}
        </p>
      )}

      <input
        type="text"
        placeholder="Group Name"
        aria-label="Group Name"
        {...register("name", { required: "Group name is required" })}
      />
      {errors.name && <p>{errors.name.message}</p>}
      <textarea
        placeholder="Group Description (optional)"
        aria-label="Group Description"
        {...register("description")}
      />
      <button type="submit">Create Group</button>
    </form>
  );
};

export default StudyGroupForm;
