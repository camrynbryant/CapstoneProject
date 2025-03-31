import React from "react";
import { useForm } from "react-hook-form";
import { registerUser } from "../../api/authService";
import { useNavigate } from "react-router-dom";
import "./Register.css";  

const Register = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();
  const navigate = useNavigate();

  const onSubmit = async (data) => {
    try {
      const response = await registerUser(data);
      console.log("Response:", response);
      navigate("/login");  
    } catch (error) {
      console.error("Error:", error);
      alert(error.message || "Registration failed. Please try again.");
    }
  };
  
  return (
    <div className="register-container">
      <h2>Register</h2>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label>Name</label>
          <input {...register("name", { required: "Name is required" })} />
          {errors.name && <p className="error">{errors.name.message}</p>}
        </div>

        <div>
          <label>Email</label>
          <input
            type="email"
            {...register("email", { 
              required: "Email is required", 
              pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/ 
            })}
          />
          {errors.email && <p className="error">{errors.email.message}</p>}
        </div>

        <div>
          <label>Password</label>
          <input 
            type="password" 
            {...register("password", { 
              required: "Password is required", 
              minLength: 8 
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