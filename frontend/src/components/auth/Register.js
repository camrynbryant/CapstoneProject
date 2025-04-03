import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { registerUser } from "../../api/authService";
import { useNavigate } from "react-router-dom";

const Register = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();
  const navigate = useNavigate();
  const [feedbackMessage, setFeedbackMessage] = useState("");
  const [feedbackType, setFeedbackType] = useState("");

  const onSubmit = async (data) => {
    setFeedbackMessage("");
    setFeedbackType("");
    try {
      const response = await registerUser(data);
      console.log("Registration Response:", response);
      navigate("/login");
    } catch (error) {
      console.error("Registration Error:", error);
      setFeedbackMessage(error.message || "Registration failed. Please try again.");
      setFeedbackType("error");
    }
  };

  return (
    <div className="auth-container">
      <h2>Register</h2>
      {feedbackMessage && (
        <p className={`error ${feedbackType === 'success' ? 'text-green-600 bg-green-100 border border-green-300 p-3 rounded-md' : ''}`}>
          {feedbackMessage}
        </p>
      )}
      <form onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label htmlFor="registerName">Name</label>
          <input
            id="registerName"
            type="text" 
            {...register("name", { required: "Name is required" })}
           />
          {errors.name && <p className="error">{errors.name.message}</p>}
        </div>

        <div>
          <label htmlFor="registerEmail">Email</label>
          <input
            id="registerEmail"
            type="email"
            {...register("email", {
              required: "Email is required",
              pattern: {
                value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                message: "Invalid email address"
               }
            })}
          />
          {errors.email && <p className="error">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="registerPassword">Password</label>
          <input
            id="registerPassword"
            type="password"
            {...register("password", {
              required: "Password is required",
              minLength: { value: 8, message: "Password must be at least 8 characters" }
            })}
          />
          {errors.password && <p className="error">{errors.password.message}</p>}
        </div>

        <button type="submit">Register</button>
      </form>
    </div>
  );
};

export default Register;
