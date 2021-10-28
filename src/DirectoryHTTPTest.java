import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class DirectoryHTTPTest {
    private static final Logger logger = Logger.getLogger("DirectoryHTTPServer");
    public static void main(String[] args) throws IOException {
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if(port<1||port>65535)  port=80;
        } catch (RuntimeException ex) {
            port = 80;
        }
        try {
            String dictName = args[0];
            Path path = Paths.get(dictName);
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            DirectoryHTTPServer server = new DirectoryHTTPServer(port,dictName);
            for (Path file : stream) {
                String name = file.getFileName().toString();
                server.fileNames.add(name);
            }
            server.start();
            DirectoryHTTPClient client = new DirectoryHTTPClient();
            String echo;
            echo = client.sendEcho("index");
//            System.out.println(echo);
            echo = client.sendEcho("server is working");
//            System.out.println(echo);
//            client.sendEcho("end");
            client.close();
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Usage: java DirectoryHTTPServer filename port encoding");
        } catch (IOException ex) {
            System.out.println(ex);
            logger.severe(ex.getMessage());
        }

    }
}
