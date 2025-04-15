import React from "react";
import { useNavigate } from "react-router-dom";
import "./Home.css";
import heroImage from '../assets/virtual-learning-illustration.jpg';

const Home = () => {
  const navigate = useNavigate();

  const handleGoToGroups = () => {
    navigate("/studygroups");
  };

  const heroStyle = {
    backgroundImage: `url(${heroImage})`
  };

  return (
    <div className="home-page">
      <section className="hero-section" style={heroStyle}>
        <div className="hero-overlay"></div>
        <div className="hero-content">
          <header className="hero-header">
            <h1>Online Learning Virtual Group Study</h1>
          </header>
          <p>Connect, learn, and grow together.</p>
          <button className="cta-button" onClick={handleGoToGroups}>
            Go to Groups
          </button>
        </div>
      </section>
    </div>
  );
};

export default Home;
