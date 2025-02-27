import React, { useState, useEffect } from "react";
import { addUser, getAllUsers } from "../api/userService"; 

function App() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [users, setUsers] = useState([]);

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    const response = await getAllUsers();
    setUsers(response.data);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await addUser({ name, email });
    setName("");
    setEmail("");
    fetchUsers();
  };

  return (
    <div style={{ textAlign: "center", padding: "20px" }}>
      <h2>React + Spring Boot + MongoDB</h2>

      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <button type="submit">Add User</button>
      </form>

      <h3>Stored Users</h3>
      <ul>
        {users.map((user) => (
          <li key={user.id}>{user.name} ({user.email})</li>
        ))}
      </ul>
    </div>
  );
}

export default App;
