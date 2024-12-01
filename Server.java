import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import java.awt.*;

public class Server {
    private static final Set<PrintWriter> clientWriters = new HashSet<>();
    private JTextArea logArea;

    public static void main(String[] args) {
        System.out.println("Server started...");
        new Server().startServer();
    }

    private void startServer() {
        setupGUI();

        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logMessage("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                // Start a new thread to handle this client
                new Thread(new ClientHandler(clientSocket, out)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupGUI() {
        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    logMessage("Received: " + message);
                    broadcastMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                removeClient();
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    if (writer != out) { // Avoid sending the message back to the sender
                        writer.println("Client: " + message);
                    }
                }
            }
        }

        private void removeClient() {
            synchronized (clientWriters) {
                clientWriters.remove(out);
            }
        }
    }
}
