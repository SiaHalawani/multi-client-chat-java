
/*
 * -----------------------------------------------------------------------------
 * Project: Multi-Client Messaging System over Sockets
 * Course: Network Configuration and Programming
 * Author: Sondos Halawani
 * Student ID: A2112613
 * Deadline: 2/5/2025
 * File: ChatClient.java
 *
 * Description:
 * This Java program represents the client-side implementation of a multi-client
 * socket-based messaging system. Clients can connect to a centralized server,
 * send public or private messages, and execute command-based interactions using
 * the terminal interface. It supports real-time message exchange using threads.
 *
 * Features:
 * - Connects to server at specified IP and port
 * - Sends user messages and reads server responses
 * - Receives and displays incoming messages with ANSI color formatting
 * - Supports command inputs (e.g., /msg, /all, /help)
 * - Threaded listener for concurrent message handling
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

package chatapp.socket.multiclient;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient {

    // Server configuration
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;

    /**
     * Main method that initiates the client socket connection to the server.
     * Handles input/output streams and spawns a thread for receiving messages.
     */
    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // Initial prompt and username input
            String serverPrompt = serverReader.readLine();
            System.out.print(serverPrompt + " ");
            String userName = keyboard.readLine();
            serverWriter.println(userName);

            // Start listener thread for incoming messages
            Thread listener = new Thread(new MessageListener(serverReader));
            listener.start();

            // User message input loop
            String input;
            System.out.print("Type your message: ");
            while ((input = keyboard.readLine()) != null) {
                serverWriter.println(input);
                if (input.equalsIgnoreCase("exit")) break;
                if (!input.startsWith("/")) {
                    System.out.println("\u001B[36m[" + timestamp() + "] [You]: " + input + "\u001B[0m");
                }
                System.out.print("Type your message: ");
            }

        } catch (IOException e) {
            System.err.println("\u001B[31mError connecting to server: " + e.getMessage() + "\u001B[0m");
        }
    }

    /**
     * Generates a formatted timestamp string for log output.
     *
     * @return Current date and time in yyyy-MM-dd HH:mm:ss format
     */
    private static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * Runnable class that continuously listens to incoming messages from the server.
     * Applies ANSI color formatting for different message types.
     */
    private static class MessageListener implements Runnable {
        private final BufferedReader serverReader;

        /**
         * Constructs a MessageListener with the provided BufferedReader.
         *
         * @param serverReader BufferedReader tied to the server input stream
         */
        public MessageListener(BufferedReader serverReader) {
            this.serverReader = serverReader;
        }

        /**
         * Thread run method that handles incoming message display logic.
         */
        @Override
        public void run() {
            String message;
            try {
                while ((message = serverReader.readLine()) != null) {
                    if (message.contains("[PM from")) {
                        System.out.println("\u001B[35m" + message + "\u001B[0m"); // purple
                    } else if (message.contains("[Server]:")) {
                        System.out.println("\u001B[33m" + message + "\u001B[0m"); // yellow
                    } else {
                        System.out.println("\u001B[32m" + message + "\u001B[0m"); // green
                    }
                    System.out.print("Type your message: ");
                }
            } catch (IOException e) {
                System.out.println("\u001B[31m[Disconnected from server]\u001B[0m");
            }
        }
    }
}
