import React from "react";
import { useForm } from "react-hook-form";
import { jwtDecode } from "jwt-decode";
import { createStudyGroup } from "../../api/studyGroupService";

const StudyGroupForm = ({ onGroupCreated }) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    const token = localStorage.getItem("token");
    if (!token) {
      alert("You must be logged in to create a study group.");
      return;
    }
    // Decode token; assume email is stored in sub and name in name
    const user = jwtDecode(token);
    const currentUser = {
      email: user.sub,
      name: user.name,
    };
    const groupData = {
      name: data.name,
      description: data.description,
      owner: currentUser.email, // Set the current user as the president/owner
      memberIds: [currentUser.email] // Automatically join as a member
    };
    try {
      await createStudyGroup(groupData);
      onGroupCreated();
    } catch (error) {
      alert("Failed to create study group.");
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <h3>Create Study Group</h3>
      <input
        type="text"
        placeholder="Group Name"
        {...register("name", { required: "Group name is required" })}
      />
      {errors.name && <p>{errors.name.message}</p>}
      <textarea
        placeholder="Group Description (optional)"
        {...register("description")}
      />
      <button type="submit">Create Group</button>
    </form>
  );
};

export default StudyGroupForm;
