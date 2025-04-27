import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;

export function connect({ groupId, currentUser, onMessageReceived, onConnected, onError }) {
  const token = localStorage.getItem("token");

  stompClient = new Client({
    webSocketFactory: () => new SockJS(`http://localhost:8080/ws?token=${token}`),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    debug: (str) => console.log(str),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe(`/topic/group/${groupId}`, (message) => {
        const body = JSON.parse(message.body);
        onMessageReceived(body);
      });
      stompClient.subscribe(`/topic/group/${groupId}/call`, (message) => {
        const body = JSON.parse(message.body);
        if (body.type === 'CALL_STARTED') {
          onMessageReceived(body);
        }
      });
      onConnected();
    },
    onStompError: (frame) => {
      onError(frame);
    },
  });

  stompClient.activate();
  return stompClient;
}

export function disconnect(client, groupId) {
  if (client && client.active) {
    client.deactivate();
  }
}

export function sendMessage(groupId, message) {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: `/app/chat/${groupId}`,
      body: JSON.stringify(message),
    });
  }
}

export function sendCallNotification(groupId, senderEmail) {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: `/app/call/${groupId}`,
      body: JSON.stringify({
        type: "CALL_STARTED",
        sender: senderEmail,
        timestamp: new Date().toISOString(),
      }),
    });
  }
}
