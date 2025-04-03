import React from "react";
import { useNavigate } from "react-router-dom";

const Logout = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId"); 
    navigate("/login");
  };

  return (
    <div className="logout-container" style={{ padding: '20px', textAlign: 'center' }}>
      <h2>Logout</h2>
      <button onClick={handleLogout} style={{ padding: '10px 20px', fontSize: '1rem', cursor: 'pointer' }}>
        Logout
      </button>
    </div>
  );
};

export default Logout;
