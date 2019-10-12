package engine.view;

import engine.GameContainer;
import engine.Renderer;
import engine.Settings;
import engine.audio.SoundClip;
import engine.gfx.Button;
import game.Editor;

import java.awt.event.KeyEvent;

public class PausedEdit extends View {

    private Settings s;
    private SoundClip hover, click;

    public PausedEdit(Settings settings) {
        s = settings;
        hover = new SoundClip("/audio/hover.wav");
        click = new SoundClip("/audio/click.wav");

        buttons.add(new Button("Try", "edit"));
        buttons.add(new Button("Save", "creativeMode"));
        buttons.add(new Button("Cancel", "creativeMode"));
        buttons.add(new Button("Back", "edit"));
    }

    @Override
    public void update(GameContainer gc, float dt) {

        if (gc.getInputHandler().isKeyDown(KeyEvent.VK_ESCAPE)) gc.setActiView("edit");

        for (Button btn : buttons) {
            if (isSelected(gc, btn)) {
                switch (btn.getText()) {
                    case "Save":
                        Editor.creaImg.saveIt(Editor.rename);
                        CreativeMode.once = false;
                        Editor.setSpawn(false);
                        break;
                    case "Try": Editor.setSpawn(true); break;
                    case "Cancel": Editor.setSpawn(false); break;
                }
                click.play();
                gc.setActiView(btn.getTargetView());
                gc.setPrevView(gc.getActiView());
            }

            if (btn.setHover(isHover(gc, btn))) {
                if (!btn.isHoverSounded()) {
                    if (!hover.isRunning()) hover.play();
                    btn.setHoverSounded(true);
                }
            } else {
                btn.setHoverSounded(false);
            }
        }
    }

    @Override
    public void render(GameContainer gc, Renderer r) {

        r.fillRect(r.getCamX(), r.getCamY(), gc.getWidth(), gc.getHeight(), 0x99000000);

        int startY = gc.getHeight() / 4;

        for (Button btn : buttons) {
            btn.setOffX(gc.getWidth() / 2 - 85);
            btn.setOffY(startY);
            startY += btn.getHeight() + 5;
            r.drawButton(btn, s.translate(btn.getText()));
        }
    }
}