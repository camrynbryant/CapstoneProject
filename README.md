# Virtual Study - Capstone Project

Virtual Study is an online platform that allows students to form study groups, chat in real time, schedule study sessions, and hold video meetings all in one place.  
This capstone project was developed to make academic collaboration easier and more engaging.

## Technologies Used
- **Backend:** Java (Spring Boot), MongoDB
- **Frontend:** React.js, CSS Modules
- **Real-time Communication:** WebSocket, Daily.co

## Setup Instructions

### 1. Start MongoDB
Make sure MongoDB is installed and running.

### 2. Initialize the Database
Run the provided database script using the MongoDB shell:
```bash
mongosh < database/capstone_init.txt
```
This will create the necessary collections and insert sample data for users, study groups, study sessions, messages, and achievements.

### 3. Start the Backend
Navigate to the `capstone/` directory and run:
```bash
./mvnw spring-boot:run
```
Ensure the backend server is running on port 8080 (or adjust configuration as needed).

### 4. Start the Frontend
Navigate to the `frontend/` directory and run:
```bash
npm install
npm start
```
This will start the React application, typically available at `http://localhost:3000/`.

## Notes
- Real-time chat is powered by WebSockets (STOMP protocol).
- Video calls are powered by Daily.co integration.
- Project source includes automated unit tests located under `/src/test/java/`.
