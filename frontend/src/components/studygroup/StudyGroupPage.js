import React, { useState } from "react";
import "./StudyGroup.css";
import StudyGroupForm from "./StudyGroupForm";
import StudyGroupList from "./StudyGroupList";

const StudyGroupPage = () => {
  const [refresh, setRefresh] = useState(false);

  const handleGroupCreated = () => {
    setRefresh(!refresh);
  };

  return (
    <div className="studygroup-container">
      <h2>Study Groups</h2>
      <StudyGroupForm onGroupCreated={handleGroupCreated} />
      <StudyGroupList refresh={refresh} />
    </div>
  );
};

export default StudyGroupPage;