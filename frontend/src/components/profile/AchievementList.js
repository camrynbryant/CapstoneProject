import React from 'react';
import AchievementItem from './AchievementItem'; 
import './Profile.css';

const AchievementList = ({ achievements, loading }) => {
  if (loading) {
    return <p>Loading achievements...</p>;
  }

  if (!achievements || achievements.length === 0) {
    return <p>No achievements earned yet. Keep studying!</p>;
  }

  return (
    <ul className="achievement-list">
      {achievements.map((ach) => (
        <AchievementItem key={ach.id} achievement={ach} />
      ))}
    </ul>
  );
};

export default AchievementList;