package org.academiadecodigo.heroisdovar;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class ConcurrentWebServer {
    // run new thread when new request/client comes in
    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean isConnected;

    public ConcurrentWebServer(int port) {
        this.port = port;
        this.isConnected = false;
    }

    public void createSocket() throws IOException {
        serverSocket = new ServerSocket(port);
        isConnected = true;
    }

    public String getFilePath(String request){
        String[] path = request.split(" ");
        return path[1];
    }

    public String getFileType(String filePath){
        String[] type = filePath.split("\\.");
        return (type.length > 1) ? type[1] : "html";
    }

    public FileInputStream getFile(String path) {
        try {
            return new FileInputStream("lib" + path);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return get404();
        }
    }

    public FileInputStream get404(){
        try {
            return new FileInputStream("lib/404.html");
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            disconnect();
            return null;
        }
    }

    public void disconnect(){

        try {

            if (clientSocket != null) {
                clientSocket.close();
            }

            if (serverSocket != null) {
                serverSocket.close();
            }

        } catch (IOException ex) {

            System.out.println("Error closing connection: " + ex.getMessage());

        }
    }


    public void start() {

        try {

            createSocket();

            int i = 0;
            while(isConnected) {

                clientSocket = serverSocket.accept();

                    Thread thread = new Thread(new ClientRequest(clientSocket));

                    thread.setName("Thread " + i);
                    i++;
                    System.out.println(thread.getName());
                    thread.start();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            disconnect();
        }
    }

    public class ClientRequest implements Runnable {

        private final Socket clientSocket;

        public ClientRequest(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream());

                String request = in.readLine();
                System.out.println(request);

                String filePath = getFilePath(request);
                String fileType = getFileType(filePath);

                FileInputStream inputStream = getFile(filePath);

                int bytesToExtract = inputStream.available();
                byte[] buffer = new byte[bytesToExtract];

                String header = "HTTP/1.0 200 Document Follows" + "\r\n" +
                        "Content-Type: " + (fileType.equals("html") ? "text/html; charset=UTF-8" : "image/" + fileType) + "\r\n" +
                        "Content-Length: " + bytesToExtract + "\r\n" +
                        "\r\n";

                inputStream.read(buffer);
                out.write(header.getBytes());
                out.write(buffer);

                out.close();
                clientSocket.close();

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        ConcurrentWebServer webServer = new ConcurrentWebServer(8085);
        webServer.start();
    }
}
