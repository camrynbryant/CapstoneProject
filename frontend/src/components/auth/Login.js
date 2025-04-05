import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { loginUser } from "../../api/authService";
import { useNavigate } from "react-router-dom";


const Login = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();
  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();

  const onSubmit = async (data) => {
    console.log("Login onSubmit started...");
    try {
      console.log("Login data:", data);
      console.log("Calling loginUser...");
      const response = await loginUser(data);
      console.log("loginUser response:", response);
      localStorage.setItem("token", response.token);
      localStorage.setItem("userId", response.userId);
      localStorage.setItem("userEmail", response.email || data.email);
      console.log("Login successful, redirecting...");
      navigate("/studygroups"); 
    } catch (error) {
      console.error("Error in Login onSubmit:", error);
      setErrorMessage(error.message || "Login failed. Please try again.");
    }
    console.log("Login onSubmit finished.");
  };

  return (
    <div className="auth-container">
      <h2>Login</h2>
      {}
      {errorMessage && <p className="error">{errorMessage}</p>}
      <form onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label htmlFor="loginEmail">Email</label>
          <input
            id="loginEmail"
            type="email"
            {...register("email", { required: "Email is required" })}
          />
          {}
          {errors.email && <p className="error">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="loginPassword">Password</label>
          <input
            id="loginPassword"
            type="password"
            {...register("password", { required: "Password is required" })}
          />
          {}
          {errors.password && <p className="error">{errors.password.message}</p>}
        </div>

        <button type="submit">Login</button>
      </form>
    </div>
  );
};

export default Login;
