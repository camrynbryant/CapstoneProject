import React from "react";
import { BrowserRouter as Router, Route, Routes, Link } from "react-router-dom";
import Register from "./components/auth/Register";
import Login from "./components/auth/Login";
import Logout from "./components/auth/Logout";
import StudyGroupPage from "./components/studygroup/StudyGroupPage";

function App() {
  return (
    <Router>
      <div>
        <h1>Virtual Study Platform</h1>
        <nav>
          <ul>
            <li><Link to="/register">Register</Link></li>
            <li><Link to="/login">Login</Link></li>
            <li><Link to="/logout">Logout</Link></li>
            <li><Link to="/studygroups">Study Groups</Link></li>
          </ul>
        </nav>
        <Routes>
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/logout" element={<Logout />} />
          <Route path="/studygroups" element={<StudyGroupPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
