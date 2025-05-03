# Multi-Client Messaging System over Sockets

## 📚 Course & Author Information
| Detail               | Information                              |
|----------------------|-------------------------------------------|
| **Course**           | Network Configuration and Programming     |
| **Project Title**    | Multi-Client Messaging System over Sockets |
| **Developer**        | Sondos Halawani                           |
| **Student ID**       | A2112613                                  |
| **Institution**      | University of Balamand                    |
| **Instructor**       | Dr. Dani Nini                             |
| **Semester**         | Spring 2025                               |


###### 📚 Course & Author Information

- **Course**: Network Configuration and Programming  
- **Project Title**: Multi-Client Messaging System over Sockets  
- **Developer**: Sondos Halawani  
- **Student ID**: A2112613  
- **Institution**: University of Balamand  
- **Instructor**: Dr. Dani Nini  
- **Semester**: Spring 2025  
- **GitHub**: [github.com/SiaHalawani](https://github.com/SiaHalawani)
---

## 📑 Table of Contents
- [📁 Project Structure](#-project-structure)
- [📝 Project Overview](#-project-overview)
- [🚀 Application Workflow](#-application-workflow)
- [🔐 Key Features](#-key-features)
- [⚙️ Technical Design](#️-technical-design)
- [🛠 How to Run (NetBeans)](#-how-to-run-netbeans)
- [🧪 Testing Screenshots](#-testing-instructions)
- [📋 Sample Terminal Logs](#-sample-terminal-logs)
- [🧾 File Overview](#-file-overview)
- [📄 License](#-license)

---

## 📁 Project Structure

```
java-socket-multiclient-chat/
├── src/
│   └── chatapp/socket/multiclient/
│       ├── ChatClient.java
│       ├── ChatServer.java
│       └── README.md
├── server.log       # Log file with rotation
├── users.txt        # Registered usernames
├── banned.txt       # Banned usernames
├── build.xml        # NetBeans build file
├── nbproject/       # NetBeans configuration folder
```

---

## 📝 Project Overview

This Java-based project provides a **command-line multi-client messaging platform** that enables real-time communication over TCP sockets. Developed with a focus on socket programming fundamentals and multi-threaded server management, the application separates the concerns of server operations, client connections, room management, and message routing.

Designed and built using **pure Java**, the system ensures robustness, clear logging, and a modular approach to future enhancements.

---

## 🚀 Application Workflow

### Server Initialization
- Starts a `ServerSocket` on port `1234`
- Listens for incoming connections via a dedicated handler thread
- Initializes user registry, ban list, and rotating logs

### Client Lifecycle
- Each client enters a unique username (validated server-side)
- The client joins the default room (`main`) or creates/joins custom rooms
- Clients may:
  - Broadcast to the room
  - Send private messages (`/msg <user> <text>`)
  - View online users (`/all`)
  - Navigate rooms (`/create`, `/join`)
  - Exit cleanly (`exit`)

### Admin Console (via Server Terminal)
- Issue real-time commands:
  - `/kick <user>` – Disconnect a user
  - `/mute <user>` – Silently ignore user’s input
  - `/ban <user>` – Disconnect & prevent reconnecting
  - `/users` – List current users
  - `/stats` – Server uptime & message stats
  - `/shutdown` – Graceful shutdown

---

## 🔐 Key Features

| Feature | Description |
|--------|-------------|
| 💬 **Multi-Room Chat** | Clients can create or join chat rooms dynamically |
| 🛡️ **Admin Controls** | Kick, mute, ban users in real-time |
| 🧾 **Log Rotation** | Automatically archives logs after 1MB |
| 🕓 **Server Statistics** | Tracks uptime, user count, and message volume |
| 🧠 **Command Recognition** | Includes `/help`, validation, and feedback |
| 🧍 **Persistent Userbase** | `users.txt` and `banned.txt` ensure continuity |
| 🎨 **Color-coded Output** | ANSI formatting for clarity (client terminal only) |
| 🕵️ **Private Messaging** | `/msg <username> <message>` for DM support |
| 🧠 **Duplicate Name Prevention** | Enforces unique usernames per session |
| 🗂️ **Thread-Safe Structures** | Synchronized sets for all shared state objects |

---

## ⚙️ Technical Design

- **Language**: Java SE 17+
- **Architecture**: Multi-threaded, TCP Socket-based
- **Concurrency**: `synchronized` collections, each client handled in a separate thread
- **Data Persistence**: File-based I/O for user/ban registry
- **Logging**: Custom logger with file rotation, timestamping via `SimpleDateFormat`
- **ANSI Console Output**: Colored terminal output based on message type

---

## 🛠 How to Run (NetBeans)

1. **Launch NetBeans**
2. **Open Project** → Select `java-socket-multiclient-chat` root folder
3. Right-click `ChatServer.java` → **Run**
4. Right-click `ChatClient.java` → **Run** (multiple times for multi-client tests)
5. To restart cleanly, delete:
   - `users.txt`
   - `banned.txt`
   - `server.log`

---


# 🧪 Testing Screenshots

This section visually demonstrates the various capabilities of the Multi-Client 
Messaging System using real interaction logs from the terminal interface. 
Each image corresponds to a specific scenario covering client behavior, 
server monitoring, and admin commands.

## 👤 Client 1: Sondos Halawani

### ✅ Client Startup and Messaging

The user "Sondos" joins the server, creates a room, and sends public and private messages.

![Client1Screen](chatapp.socket.multiclient.images/Client1Screen.png)

### ✅ Room Creation and Communication

User "Sondos" creates a room and interacts with others. We observe room switching, message delivery, and command outputs like `/all`, `/msg`, `/create`, and `/join`.

![Client2Screen](chatapp.socket.multiclient.images/Client2Screen.png)

## 👤 Client 2: Dr. Dani

### ✅ Private Message and Room Joining

Dani joins the chat, receives messages, switches to the correct room using `/join`, and replies privately to "Sondos".

![Client3Screen](chatapp.socket.multiclient.images/Client3Screen.png)

## 🛡️ Server Admin Console

### ✅ Monitoring Logs

Shows client joins, message logs, and use of admin features like `/mute`, `/ban`, `/kick`, `/users`, `/stats`, and `/shutdown`.

![ServerScreen1](chatapp.socket.multiclient.images/ServerScreen1.png)

### ✅ Server Stats and Disconnection

Final system snapshot with statistics summary, graceful shutdown, and logging behavior after users disconnect.

![ServerScreen2](chatapp.socket.multiclient.images/ServerScreen2.png)


---

## 📋 Sample Terminal Logs

Paste real-time test output here for demonstration:

```
[2025-05-03 13:43:47] [You]: Hello!
[2025-05-03 13:43:50] [PM to Dani]: Hello privately
[2025-05-03 13:44:00] [Server]: Room created and joined: Students
[2025-05-03 13:45:12] [Dani]: Hello Sondos!
[2025-05-03 13:46:22] [Server]: Online users: Dani Sondos
[2025-05-03 13:47:30] [Server]: Message delivered
```

---

## 🧾 File Overview

| File | Description |
|------|-------------|
| `ChatClient.java` | Terminal-based Java client that connects to the server and handles input/output |
| `ChatServer.java` | Main server class that handles client connections, rooms, admin commands, and logs |
| `server.log` | Persistent log file of server activities (auto-rotated) |
| `users.txt` | List of usernames that have previously connected |
| `banned.txt` | List of usernames not allowed to connect |
| `README.md` | Project overview and documentation (you are reading it!) |

---

## 📄 License

This software is developed under academic purposes only.  
All rights reserved © 2025 by **Sondos Halawani** | University of Balamand  
**Course**: Network Configuration and Programming – *Instructor: Dr. Dani Nini*  
Redistribution or reuse is restricted to educational demonstration only.

---