package io.github.game_izzy_magordito2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Empezar con la pantalla de inicio
        setScreen(new PantallaInicio(this));
    }

    @Override
    public void render() {
        super.render(); // llamar al render de la pantalla actual
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
