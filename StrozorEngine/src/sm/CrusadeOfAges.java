package sm;

import sm.engine.PlayerStats;
import sm.engine.GameContainer;
import sm.engine.Settings;
import sm.engine.World;
import sm.exceptions.ConfException;
import sm.game.Conf;
import sm.game.GameManager;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class CrusadeOfAges {

    private static String appdata;
    private static String host = "localhost";
    private static int port = 5338;

    /**
     * Starts the game
     * @param args [-c ConfigFolder] [-h Host] [-p Port]
     */
    public static void main(String[] args) throws IOException {

        String OS = (System.getProperty("os.name")).toUpperCase();
        if (OS.contains("WIN")) {
            appdata = System.getenv("AppData");
        } else {
            appdata = System.getProperty("user.dir");
        }

        manageArgs(args);

        Settings settings = new Settings();
        World world = new World();
        try {
            Conf conf = new Conf(appdata);
            conf.initiate();
            conf.readSettings(settings);
        } catch (ConfException e) {
            return;
        }

        try {
            Socket socket = new Socket(host, port);
            GameManager gm = new GameManager(socket, world);
            PlayerStats ps = new PlayerStats();
            GameContainer gc = new GameContainer(gm, settings, world, ps);
            gc.setTitle("Crusade of Ages");
            gc.setScale(settings.getScale());
            gc.start();
        } catch(ConnectException e) {
            System.out.println("Connection refused: " + host + ":" + port);
        }
    }

    private static void manageArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-c": appdata = args[i+1]; break;
                case "-h": host = args[i+1]; break;
                case "-p": port = Integer.parseInt(args[i+1]); break;
            }
        }
    }
}
