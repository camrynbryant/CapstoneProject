import React, { useState, useEffect } from "react";
import { Route, Routes, Link, useLocation } from "react-router-dom";
import { getUnreadCount } from "./api/notificationService";
import Home from "./components/Home";
import Register from "./components/auth/Register";
import Login from "./components/auth/Login";
import Logout from "./components/auth/Logout";
import StudyGroupPage from "./components/studygroup/StudyGroupPage";
import StudyGroupDetailPage from "./components/studygroup/StudyGroupDetailPage";
import NotificationsPage from "./components/notifications/NotificationsPage";
import NotificationListener from "./components/notifications/NotificationListener"; 
import GroupChat from "./components/chat/GroupChat";
import "./App.css";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const location = useLocation();

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userEmail");

  useEffect(() => {
    setIsAuthenticated(!!token);

    if (token && userId) {
      getUnreadCount(userId, token).then(setUnreadCount).catch(console.error);
    }
  }, [location, token, userId]);

  return (
    <div className="App">
            <NotificationListener userId={userId} token={token} />
      <nav className="app-nav">
        <ul>
          <li><Link to="/">Home</Link></li>
          {isAuthenticated ? (
            <>
              <li><Link to="/studygroups">Study Groups</Link></li>
              <li>
                <Link to="/notifications">
                  Notifications ({unreadCount})
                </Link>
              </li>
              <li><Link to="/logout">Logout</Link></li>
            </>
          ) : (
            <>
              <li><Link to="/register">Register</Link></li>
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
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/groups/:groupId/chat" element={<GroupChat />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
