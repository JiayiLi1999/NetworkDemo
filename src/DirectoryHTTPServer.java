import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DirectoryHTTPServer extends Thread {
    private static final Logger logger = Logger.getLogger("DirectoryHTTPServer");
    private final int port;
    private final String dictName;
    Set<String> fileNames;
    public final static int MAX_PACKET_SIZE = 256;
    byte[] receiveData = new byte[MAX_PACKET_SIZE];

    public DirectoryHTTPServer(int port, String dictName) throws SocketException {
        this.port = port;
        this.dictName = dictName;
        fileNames = new HashSet<>();
    }

    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(port);
            String sendString = "polo";
            byte[] sendData = sendString.getBytes("UTF-8");
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            while (true) {

                serverSocket.receive(receivePacket);
                String sentence = new String( receivePacket.getData(), 0, receivePacket.getLength() );
                System.out.println("RECEIVED: " + sentence);
                // now send acknowledgement packet back to sender
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(sendPacket);






//
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                socket.receive(packet);
//
//                InetAddress address = packet.getAddress();
//                int port = packet.getPort();
//                packet = new DatagramPacket(buffer, buffer.length, address, port);
//                String received = new String(packet.getData(), 0, packet.getLength());
//                if(received.startsWith("index")) System.out.println(received);
//
//                if (received.equals("end")) {
//                    break;
//                }
//                socket.send(packet);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





    private class HTTPHandler implements Callable<Void> {
        private final Socket connection;
        HTTPHandler(Socket connection) {
            this.connection = connection;
        }
        @Override
        public Void call() throws IOException {
            try {
                OutputStream out = new BufferedOutputStream( connection.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line = in.readLine();
                while( line != null )
                {
                    System.out.println( line );
                    if (line.equals("index")) {
                        for (String name : fileNames) {
                            out.write(name.getBytes(Charset.forName("UTF-8")));
                            out.write('\n');
                        }
                        out.flush();
                    }
                    if (line.equals("exit")) {
                        break;
                    }
                    if(line.startsWith("get")){
                        String findTarget = line.split(" ")[1];
                        if(fileNames.contains(findTarget)){
                            Path path = Paths.get(dictName+"/"+findTarget);
                            byte[] data = Files.readAllBytes(path);
                            logger.info("Get File: ");
                            out.write("OK".getBytes());
                            out.write('\n');
                            out.write(data);
                            out.flush();
                            break;
                        }else{
                            out.write("Error: No such file\n".getBytes());
                            out.flush();
                            logger.log(Level.SEVERE, "No such file");
                            throw new IllegalArgumentException();
                        }
                    }
                    line = in.readLine();
                }
                in.close();
                out.close();
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error writing to client", ex);
            } finally {
                connection.close();
            }
            return null;
        }
    }
    }