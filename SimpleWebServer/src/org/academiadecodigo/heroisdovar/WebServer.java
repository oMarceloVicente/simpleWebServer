package org.academiadecodigo.heroisdovar;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class WebServer {

    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedOutputStream out;
    private boolean isConnected;

    public WebServer(int port) {
        this.port = port;
        this.isConnected = false;
    }

    public void createSocket() throws IOException {
        serverSocket = new ServerSocket(port);
        isConnected = true;
    }

    public void createStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new BufferedOutputStream(clientSocket.getOutputStream());
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

    public String getHeader(String fileType, int bytesToExtract) {

        return "HTTP/1.0 200 Document Follows" + "\r\n" +
                "Content-Type: " + (fileType.equals("html") ? "text/html; charset=UTF-8" : "image/" + fileType) + "\r\n" +
                "Content-Length: " + bytesToExtract + "\r\n" +
                "\r\n";
    }

    public void start() {

        try {

            createSocket();

            while(isConnected) {
                clientSocket = serverSocket.accept();
                createStreams();

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
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            disconnect();
        }
    }

    public static void main(String[] args) {
        WebServer webServer = new WebServer(8080);
        webServer.start();
    }

}
