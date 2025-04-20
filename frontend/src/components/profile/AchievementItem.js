import React from 'react';

const AchievementItem = ({ achievement }) => {
  if (!achievement) {
    return null; 
  }

  const earnedDate = achievement.earnedAt
    ? new Date(achievement.earnedAt).toLocaleDateString()
    : 'Unknown date';

  const icon = achievement.achievementIconUrl ? (
    <img src={achievement.achievementIconUrl} alt="" className="achievement-icon" />
  ) : (
    <span className="achievement-icon placeholder-icon">🏆</span> 
  );

  return (
    <li className="achievement-item">
      {icon}
      <div className="achievement-details">
        <strong className="achievement-name">{achievement.achievementName || 'Unnamed Achievement'}</strong>
        <p className="achievement-description">{achievement.achievementDescription || 'No description available.'}</p>
        <span className="achievement-date">
          Earned: {earnedDate}
        </span>
      </div>
    </li>
  );
};

export default AchievementItem;