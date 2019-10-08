package sm.engine.view;

import sm.engine.GameContainer;
import sm.engine.Renderer;
import sm.engine.Settings;
import sm.engine.audio.SoundClip;
import sm.engine.gfx.Button;

public class GameOver extends View {

    private Settings s;
    private SoundClip hover, click, gameover;

    private boolean once = false;

    public GameOver(Settings settings) {
        s = settings;
        hover = new SoundClip("/audio/hover.wav");
        click = new SoundClip("/audio/click.wav");
        gameover = new SoundClip("/audio/gameover.wav");
        buttons.add(new Button("Try again", 1));
        buttons.add(new Button("Quit to title", 0));
    }

    @Override
    public void update(GameContainer gc, float dt) {

        if(gc.getLastState() == 1 && !once) {
            gameover.play();
            once = true;
        }

        //Button selection
        for(Button btn : buttons) {
            if(isSelected(gc, btn)) {
                if(btn.getText().contains("Quit")) gc.getPlayerStats().saveData();
                click.play();
                gameover.stop();
                once = false;
                gc.setState(btn.getGoState());
                gc.setLastState(7);
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

        r.fillRect(0, 0, gc.getWidth(), gc.getHeight(), 0x99000000);

        r.drawMenuTitle(gc, s.translate("GAME OVER"), s.translate("You are dead"));

        int startY = gc.getHeight()/4;

        for(Button btn : buttons) {
            btn.setOffX(gc.getWidth()/2-85);
            btn.setOffY(startY);
            startY += btn.getHeight() + 5;
            r.drawButton(btn, s.translate(btn.getText()));
        }
    }
}
