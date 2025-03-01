import React, { useState } from "react";
import StudyGroupForm from "./StudyGroupForm";
import StudyGroupList from "./StudyGroupList";

const StudyGroupPage = () => {
  // A simple trigger to force reloading the list when a new group is created.
  const [refresh, setRefresh] = useState(false);

  const handleGroupCreated = () => {
    // Toggle refresh state
    setRefresh(!refresh);
  };

  return (
    <div>
      <StudyGroupForm onGroupCreated={handleGroupCreated} />
      <StudyGroupList key={refresh} />
    </div>
  );
};

export default StudyGroupPage;
