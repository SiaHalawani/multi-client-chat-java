
/*
 * -----------------------------------------------------------------------------
 * Project: Multi-Client Messaging System over Sockets
 * Course: Network Configuration and Programming
 * Author: Sondos Halawani
 * Student ID: A2112613
 * Deadline: 2/5/2025
 * File: ChatServer.java
 *
 * Description:
 * This Java file defines the server-side logic for a multi-client chat system 
 * using Java sockets. The server handles multiple client connections concurrently, 
 * supports private messaging, user and room management, admin operations, and 
 * maintains a persistent log and user history. It is built with thread safety 
 * and modular extensibility in mind.
 *
 * Features:
 * - Accepts multiple client connections and assigns threads
 * - Room-based message broadcasting and isolation
 * - Private messaging via /msg command
 * - Admin tools: kick, mute, ban, shutdown, log, stats
 * - Chat history caching and log rotation
 * - Real-time user and room tracking
 * -----------------------------------------------------------------------------
 */

/*
 ***************************************************************
 * ⚠️                     NOTE!!!!!
 * For full documentation, testing instructions, and screenshots,
 * please refer to the README.md file and the images in the 
 * chatapp.socket.multiclient.images package.
 ***************************************************************
 */

// Final ChatServer.java with full room support
package chatapp.socket.multiclient;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


// Main server class responsible for client connections, room management,
// message broadcasting, command handling, and server-side logging.
public class ChatServer{
    private static final int PORT = 1234;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> mutedUsers = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> bannedUsers = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> userRegistry = Collections.synchronizedSet(new HashSet<>());
    private static final LinkedList<String> history = new LinkedList<>();
    private static final Set<String> rooms = Collections.synchronizedSet(new HashSet<>(List.of("main")));
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static PrintWriter logWriter;
    private static long startTime;
    private static int messageCount = 0;

    
    /**
     * Entry point of the server application.
     * Initializes logging, loads user/ban registries,
     * starts listening for incoming client connections,
     * and spawns admin command listener.
     */
    public static void main(String[] args) {
        try {
            rotateLog();
            logWriter = new PrintWriter(new FileWriter("server.log", true), true);
            loadUsers();
            loadBanned();
            ServerSocket serverSocket = new ServerSocket(PORT);
            startTime = System.currentTimeMillis();
            log("SERVER STARTED ON PORT " + PORT);
            new Thread(() -> listenToAdmin(serverSocket)).start();
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Logs a message to the console and appends it to the server log file.
     *
     * @param msg the message to log
     */
    static void log(String msg) {
        String line = "[" + timestamp() + "] " + msg;
        System.out.println(line);
        logWriter.println(line);
    }

    
    /**
     * Sends a message to all connected clients in a specific room except the sender.
     *
     * @param msg the message to broadcast
     * @param exclude the client to exclude from receiving the message
     * @param room the room where the message is broadcasted
     */
    static void broadcast(String msg, ClientHandler exclude, String room) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != exclude && client.room.equals(room)) {
                    client.sendMessage(msg);
                }
            }
        }
    }

    
    /**
     * Removes a client handler from the active client list.
     *
     * @param c the client handler to remove
     */
    static void removeClient(ClientHandler c) {
        clients.remove(c);
    }

    static boolean usernameTaken(String name) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (c.name.equalsIgnoreCase(name)) return true;
            }
        }
        return false;
    }

    
    /**
     * Returns a list of currently connected client usernames.
     *
     * @return space-separated string of usernames
     */
    static String getClientList() {
        StringBuilder sb = new StringBuilder();
        synchronized (clients) {
            for (ClientHandler c : clients) sb.append(c.name).append(" ");
        }
        return sb.toString().trim();
    }

    
    /**
     * Retrieves a client handler by their username.
     *
     * @param name the username to search for
     * @return the ClientHandler object or null if not found
     */
    static ClientHandler getClientByName(String name) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (c.name.equalsIgnoreCase(name)) return c;
            }
        }
        return null;
    }

    
    /**
     * Admin console input listener.
     * Accepts admin commands from standard input to control the server.
     *
     * @param serverSocket the server socket to manage connections
     */
    static void listenToAdmin(ServerSocket serverSocket) {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                String input = sc.nextLine();
                if (input.startsWith("/kick ")) {
                    String name = input.substring(6).trim();
                    ClientHandler c = getClientByName(name);
                    if (c != null) {
                        c.sendMessage("[Server]: You were kicked");
                        c.forceClose();
                        log("Admin kicked " + name);
                    } else log("No such user to kick: " + name);
                } else if (input.startsWith("/mute ")) {
                    String name = input.substring(6).trim();
                    mutedUsers.add(name.toLowerCase());
                    log("Admin muted " + name);
                } else if (input.startsWith("/ban ")) {
                    String name = input.substring(5).trim();
                    bannedUsers.add(name.toLowerCase());
                    saveBanned();
                    ClientHandler c = getClientByName(name);
                    if (c != null) {
                        c.sendMessage("[Server]: You were banned");
                        c.forceClose();
                    }
                    log("Admin banned " + name);
                } else if (input.equals("/users")) {
                    log("Online users: " + getClientList());
                } else if (input.equals("/stats")) {
                    long up = System.currentTimeMillis() - startTime;
                    long sec = (up / 1000) % 60;
                    long min = (up / 1000 / 60) % 60;
                    long hrs = (up / 1000 / 60 / 60);
                    log("Uptime: " + hrs + "h " + min + "m " + sec + "s, Users: " + clients.size() + ", Msgs: " + messageCount);
                } else if (input.equals("/shutdown")) {
                    log("Server shutting down...");
                    synchronized (clients) {
                        for (ClientHandler c : clients) {
                            c.sendMessage("[Server]: Server is shutting down");
                            c.forceClose();
                        }
                    }
                    serverSocket.close();
                    System.exit(0);
                } else {
                    log("Unknown admin command.");
                }
            }
        } catch (IOException e) {
            log("Admin error: " + e.getMessage());
        }
    }

    
    /**
     * Rotates the log file if it exceeds a certain size.
     * Archives the old log and starts a new one.
     */
    static void rotateLog() {
        File log = new File("server.log");
        if (log.exists() && log.length() > 1024 * 1024) {
            File archive = new File("server-" + System.currentTimeMillis() + ".log");
            log.renameTo(archive);
        }
    }

    static String timestamp() {
        return sdf.format(new Date());
    }

    
    /**
     * Persists a newly seen username to users.txt
     *
     * @param name the username to save
     */
    static void saveUser(String name) {
        try (PrintWriter out = new PrintWriter(new FileWriter("users.txt", true))) {
            if (!userRegistry.contains(name.toLowerCase())) {
                out.println(name);
                userRegistry.add(name.toLowerCase());
            }
        } catch (IOException ignored) {}
    }

    
    /**
     * Loads usernames from users.txt into memory on server startup.
     */
    static void loadUsers() {
        File file = new File("users.txt");
        if (file.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = in.readLine()) != null) {
                    userRegistry.add(line.toLowerCase());
                }
            } catch (IOException ignored) {}
        }
    }

    
    /**
     * Loads banned usernames from banned.txt into memory.
     */
    static void loadBanned() {
        File file = new File("banned.txt");
        if (file.exists()) {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = in.readLine()) != null) {
                    bannedUsers.add(line.toLowerCase());
                }
            } catch (IOException ignored) {}
        }
    }

    
    /**
     * Writes the banned usernames set into the banned.txt file.
     */
    static void saveBanned() {
        try (PrintWriter out = new PrintWriter(new FileWriter("banned.txt"))) {
            for (String name : bannedUsers) {
                out.println(name);
            }
        } catch (IOException ignored) {}
    }

    
    /**
     * Adds a message to the in-memory message history buffer.
     *
     * @param msg the message to store
     */
    static void addToHistory(String msg) {
        synchronized (history) {
            if (history.size() >= 10) history.removeFirst();
            history.add(msg);
        }
    }

    
// Handles each connected client in a separate thread.
// Manages user-specific input/output, room joining, message routing,
// and command execution per client.
static class ClientHandler implements Runnable {
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        String name;
        String room = "main";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        public void forceClose() {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("[Server]: Enter your name:");
                name = in.readLine();
                if (name == null || name.isEmpty() || usernameTaken(name) || bannedUsers.contains(name.toLowerCase())) {
                    out.println("[Server]: Invalid or banned name");
                    socket.close();
                    return;
                }
                saveUser(name);
                clients.add(this);
                log("Client connected: " + name + " (" + socket.getInetAddress().getHostAddress() + ")");
                synchronized (history) {
                    for (String line : history) out.println("\u001B[35m" + line + "\u001B[0m");
                }
                broadcast("\u001B[33m" + name + " has joined the chat\u001B[0m", this, room);

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/msg ")) {
                        int firstSpace = input.indexOf(' ', 5);
                        if (firstSpace > 5) {
                            String targetName = input.substring(5, firstSpace);
                            String message = input.substring(firstSpace + 1);
                            ClientHandler target = getClientByName(targetName);
                            if (target != null && target.room.equals(this.room)) {
                                target.sendMessage("\u001B[32m[PM from " + name + "]: " + message + "\u001B[0m");
                                out.println("\u001B[36m[PM to " + targetName + "]: " + message + "\u001B[0m");
                                out.println("[Server]: Message delivered");
                            } else {
                                out.println("[Server]: User not found in your room.");
                            }
                        } else {
                            out.println("[Server]: Usage: /msg username message");
                        }
                    } else if (input.equals("/all")) {
                        out.println("[Server]: Online users: " + getClientList());
                    } else if (input.equals("/help")) {
                        out.println("[Server]: Commands: /all /msg <user> <msg> /create <room> /join <room> /help /exit");
                    } else if (input.startsWith("/create ")) {
                        String newRoom = input.substring(8).trim();
                        if (!newRoom.isEmpty() && !rooms.contains(newRoom)) {
                            rooms.add(newRoom);
                            this.room = newRoom;
                            out.println("[Server]: Room created and joined: " + newRoom);
                        } else {
                            out.println("[Server]: Room exists or name invalid.");
                        }
                    } else if (input.startsWith("/join ")) {
                        String targetRoom = input.substring(6).trim();
                        if (rooms.contains(targetRoom)) {
                            this.room = targetRoom;
                            out.println("[Server]: Switched to room: " + targetRoom);
                        } else {
                            out.println("[Server]: Room does not exist.");
                        }
                    } else if (input.equals("exit")) {
                        break;
                    } else if (input.startsWith("/")) {
                        out.println("[Server]: Unknown command. Try /help");
                    } else {
                        if (mutedUsers.contains(name.toLowerCase())) {
                            out.println("[Server]: You are muted.");
                        } else {
                            String fullMsg = "\u001B[32m[" + timestamp() + "] [" + name + "]: " + input + "\u001B[0m";
                            log("[" + name + "]: " + input);
                            broadcast(fullMsg, this, room);
                            out.println("\u001B[36m[" + timestamp() + "] [You]: " + input + "\u001B[0m");
                            addToHistory("[" + timestamp() + "] [" + name + "]: " + input);
                            messageCount++;
                        }
                    }
                }

            } catch (IOException e) {
                log("Connection error with " + name);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                removeClient(this);
                log("[" + name + "] disconnected");
                broadcast("\u001B[33m" + name + " has left the chat\u001B[0m", this, room);
            }
        }
    }
}
