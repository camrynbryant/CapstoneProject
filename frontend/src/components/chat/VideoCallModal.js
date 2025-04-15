import { useEffect } from "react";

const VideoCallModal = ({ isOpen, onClose }) => {
  useEffect(() => {
    if (isOpen) {
      window.open("https://studyconnect.daily.co/study-group-call", "_blank", "noopener,noreferrer");
      onClose(); 
    }
  }, [isOpen, onClose]); 

  return null;
};

export default VideoCallModal;