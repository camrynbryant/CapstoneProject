import React, { useState, useEffect } from "react";
import { Route, Routes, Link, useLocation } from "react-router-dom";
import Home from "./components/Home";
import Register from "./components/auth/Register";
import Login from "./components/auth/Login";
import Logout from "./components/auth/Logout";
import StudyGroupPage from "./components/studygroup/StudyGroupPage";
import StudyGroupDetailPage from "./components/studygroup/StudyGroupDetailPage";
import "./App.css";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const token = localStorage.getItem("token");
    setIsAuthenticated(!!token);
  }, [location]);

  return (
    <div className="App">
      <nav className="app-nav">
        <ul>
          <li><Link to="/">Home</Link></li>
          {isAuthenticated ? (
            <>
              <li><Link to="/studygroups">Study Groups</Link></li>
              {/* Removed className */}
              <li><Link to="/logout">Logout</Link></li>
            </>
          ) : (
            <>
              <li><Link to="/register">Register</Link></li>
              {/* Removed className */}
              <li><Link to="/login">Login</Link></li>
            </>
          )}
        </ul>
      </nav>

      <div>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/logout" element={<Logout />} />
          <Route path="/studygroups" element={<StudyGroupPage />} />
          <Route path="/studygroups/:groupId" element={<StudyGroupDetailPage />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
