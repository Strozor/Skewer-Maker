package com.strozor.game;

import com.strozor.engine.*;
import com.strozor.engine.audio.SoundClip;
import com.strozor.engine.gfx.*;

import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GameManager extends AbstractGame {

    public static final int TS = 32;
    public static final String APPDATA = System.getenv("APPDATA") + "\\.squaremonster";

    private static boolean mapTester = false;
    private static String mapTest;

    private ArrayList<GameObject> objects = new ArrayList<>();
    private ArrayList<FlashNotif> notifs = new ArrayList<>();
    private Camera camera;
    private SoundClip gameOver;

    private GameMap gameMap;
    private int currLevel = 0;
    private String[] levelList = {
            "/levels/0.png",
            "/levels/1.png"
    };

    private GameManager(GameMap gameMap) {
        this.gameMap = gameMap;

        if(mapTester) load(mapTest);
        else load(levelList[currLevel]);

        gameOver = new SoundClip("/audio/gameover.wav");
        gameOver.setVolume(-10f);
        objects.add(new Player("player", gameMap, 1));
        camera = new Camera("player", gameMap);
    }

    @Override
    public void update(GameContainer gc, float dt) {

        if(gc.getInput().isKeyDown(KeyEvent.VK_ESCAPE)) gc.setState(2);

        if(gc.getInput().isKeyDown(KeyEvent.VK_F12)) {
            try {
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
                String filename = sdf.format(new Date()) + ".png";
                File out = new File(GameManager.APPDATA + "\\screenshots\\" + filename);
                ImageIO.write(gc.getWindow().getImage(), "png", out);
                notifs.add(new FlashNotif(filename, 3, 100, -1));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        //Notifications update
        for(int i = 0; i < notifs.size(); i++) {
            notifs.get(i).update(gc, dt);
            if(notifs.get(i).isEnded()) notifs.remove(i);
        }

        for(int i = 0; i < objects.size(); i++) {
            objects.get(i).update(gc, this, dt);
            if(objects.get(i).isDead()) {
                objects.remove(i);
                i--;
            }
        }

        //Animations
        gameMap.animate(dt * 3);

        //Reload level
        if(getObject("player") == null && (gc.getLastState() == 7 || gc.getLastState() == 0)) {

            if(mapTester) load(mapTest);
            else load(levelList[currLevel]);

            gameOver.stop();
            objects.add(new Player("player", gameMap, 1));

            camera = null;
            camera = new Camera("player", gameMap);
        }

        camera.update(gc, this, dt);
    }

    @Override
    public void render(GameContainer gc, GameRender r) {
        camera.render(r);
        r.drawMap(gameMap);
        if(gc.getSettings().isShowLights())
            r.drawMapLights(gameMap, new Light(80, 0xffffff00));
        for(GameObject obj : objects) obj.render(gc, this, r);
        for(FlashNotif notif : notifs) notif.render(gc, r);
    }

    public void load(String path) {
        gameMap.init(new Image(path, mapTester));
    }

    public int getCurrLevel() {
        return currLevel;
    }

    public void setCurrLevel(int currLevel) {
        this.currLevel = currLevel;
    }

    public String[] getLevelList() {
        return levelList;
    }

    public GameObject getObject(String tag) {
        for(GameObject obj : objects) if(obj.getTag().equals(tag)) return obj;
        return null;
    }

    public SoundClip getGameOver() {
        return gameOver;
    }

    public boolean isMapTesting() {
        return mapTester;
    }

    public static String getMapTest() {
        return mapTest;
    }

    static private void writeAppData() {
        //.squaremonster
        File smFolder = new File(APPDATA);
        if(!smFolder.exists()) smFolder.mkdir();

        //assets
        File smAssets = new File(APPDATA + "\\assets");
        if(!smAssets.exists()) smAssets.mkdir();

        //screenshots
        File smScreenshots = new File(APPDATA + "\\screenshots");
        if(!smScreenshots.exists()) smScreenshots.mkdir();

        //creative_mode
        File smCrea = new File(APPDATA + "\\creative_mode");
        if(!smCrea.exists()) smCrea.mkdir();

        //options.txt
        File smOptFile = new File(APPDATA + "\\options.txt");
        if(!smOptFile.exists()) {
            try {
                List<String> lines = Arrays.asList(
                        "lang:en",
                        "guiScale:3",
                        "maxFPS:60",
                        "showFPS:false",
                        "showLights:true"
                );
                Path file = Paths.get(APPDATA + "\\options.txt");
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    static private void readOptions(Settings settings) {
        try(BufferedReader br = new BufferedReader(new FileReader(APPDATA + "\\options.txt"))) {
            String line = br.readLine();
            while (line != null) {
                String[] sub = line.split(":");
                switch(sub[0]) {
                    case "lang":
                        switch(sub[1]) {
                            case "en": settings.setLangIndex(0); break;
                            case "fr": settings.setLangIndex(1); break;
                            default: settings.setLangIndex(0); break;
                        }
                        break;
                    case "guiScale": settings.setScale(Float.valueOf(sub[1])); break;
                    case "maxFPS": settings.setMaxFPS(Integer.valueOf(sub[1])); break;
                    case "showFPS": settings.setShowFps(sub[1].equals("true")); break;
                    case "showLights": settings.setShowLights(sub[1].equals("true")); break;
                }
                line = br.readLine();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Settings settings = new Settings();
        writeAppData();
        readOptions(settings);
        if(args.length == 1) {
            mapTester = true;
            mapTest = args[0];
        }
        GameContainer gc = new GameContainer(new GameManager(new GameMap()), settings);
        gc.setTitle("Square Monster");
        gc.setScale(settings.getScale());
        gc.start();
    }
}
