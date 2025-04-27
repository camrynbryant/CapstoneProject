import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { getChatHistory } from '../../api/chatService';
import { connect, disconnect, sendMessage, sendCallNotification } from '../../utils/websocket';
import './GroupChat.css';
import VideoCallModal from './VideoCallModal';

const formatTime = (isoString) => {
  if (!isoString) return '';
  const date = new Date(isoString);
  return isNaN(date) ? '' : date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const GroupChat = () => {
  const { groupId } = useParams();

  const currentUser = useMemo(() => ({
    email: localStorage.getItem("userEmail"),
    name: localStorage.getItem("userName") || "Unknown",
  }), []);

  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isCallModalOpen, setIsCallModalOpen] = useState(false);
  const [incomingCallBanner, setIncomingCallBanner] = useState(null);
  const messagesEndRef = useRef(null);
  const stompClientRef = useRef(null);

  const scrollToBottom = () => {
    if (messagesEndRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = messagesEndRef.current.parentNode;
      if (scrollHeight - scrollTop <= clientHeight + 100) {
        messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
      }
    }
  };

  const handleMessageReceived = useCallback((msg) => {
    console.log("Received WebSocket message:", msg);
    console.log("FULL WebSocket message received:", JSON.stringify(msg, null, 2));
    if (msg.type?.toUpperCase() === "CALL_STARTED" && msg.sender !== currentUser.email) {
      setIncomingCallBanner(`${msg.sender} has started a video call`);
      setTimeout(() => setIncomingCallBanner(null), 8000);
      return;
    }

    if (msg.type?.toUpperCase() === "CHAT") {
      const msgWithTimestamp = {
        ...msg,
        timestamp: msg.timestamp || new Date().toISOString(),
      };
      console.log("Appending chat message:", msgWithTimestamp);
      setMessages(prev => [...prev, msgWithTimestamp]);
    }
  }, [currentUser.email]);

  const handleSend = () => {
    if (newMessage.trim()) {
      const message = {
        content: newMessage,
        senderEmail: currentUser.email,
        senderName: currentUser.name,
        type: "CHAT",
        timestamp: new Date().toISOString()
      };
      sendMessage(groupId, message);
      console.log("Sent message:", message);
      setNewMessage('');
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleStartCall = () => {
    sendCallNotification(groupId, currentUser.email);
    setIsCallModalOpen(true);
  };

  const renderIncomingCallBanner = () => (
    <div className="call-banner-container">
      <div className="call-banner">
         {incomingCallBanner}
      </div>
    </div>
  );

  useEffect(() => {
    getChatHistory(groupId).then(res => {
      const formattedMessages = (res.data || []).map(msg => ({
        ...msg,
        timestamp: msg.timestamp || new Date().toISOString()
      }));
      setMessages(formattedMessages);
      scrollToBottom();
    });

    stompClientRef.current = connect({
      groupId,
      currentUser,
      onMessageReceived: handleMessageReceived,
      onConnected: scrollToBottom,
      onError: console.error,
    });

    return () => {
      disconnect(stompClientRef.current, groupId);
    };
  }, [groupId, handleMessageReceived, currentUser]);

  useEffect(scrollToBottom, [messages]);

  return (
    <div className="chat-wrapper">
      <h2 className="chat-title">Group Chat</h2>
      {incomingCallBanner && renderIncomingCallBanner()}
      <div className="chat-controls">
        <button className="video-call-button" onClick={handleStartCall}>
          Start Video Call
        </button>
      </div>
      <div className="chat-container">
        <div className="chat-messages">
          {messages.map((msg, i) => (
            <div
              key={i}
              className={`chat-message ${
                msg.senderEmail === currentUser.email ? 'my-message' : 'other-message'
              }`}
            >
              <div className="message-content">
                <strong>{msg.senderName || "Unknown"}:</strong> {msg.content || "(No message)"}
              </div>
              <div style={{ fontSize: '0.75rem', color: '#6c757d', marginTop: '2px' }}>
                {msg.timestamp ? formatTime(msg.timestamp) : ""}
              </div>
            </div>
          ))}
          <div ref={messagesEndRef}></div>
        </div>
        <div className="chat-input">
          <input
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Type your message..."
          />
          <button onClick={handleSend}>Send</button>
        </div>
      </div>
      <VideoCallModal isOpen={isCallModalOpen} onClose={() => setIsCallModalOpen(false)} />
    </div>
  );
};

export default GroupChat;
