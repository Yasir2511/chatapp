import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // GUI Components
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageInput;
    private JButton sendButton;

    public Client(String serverAddress) {
        createGUI();

        try {
            socket = new Socket(serverAddress, 7777);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startReading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createGUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        messageInput = new JTextField();
        messageInput.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(messageInput, BorderLayout.SOUTH);

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
            }
        });

        frame.setVisible(true);
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            chatArea.append("Me: " + message + "\n");
            out.println(message); // Send to server
            messageInput.setText("");
        }
    }

    private void startReading() {
        Thread readerThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    chatArea.append(message + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readerThread.start();
    }

    public static void main(String[] args) {
        new Client("localhost");
    }
}
