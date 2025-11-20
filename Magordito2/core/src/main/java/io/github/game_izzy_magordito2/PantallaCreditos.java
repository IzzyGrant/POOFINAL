package io.github.game_izzy_magordito2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PantallaCreditos implements Screen {

    private Main game;
    private SpriteBatch batch;
    private BitmapFont fontTitulo;
    private BitmapFont fontTexto;

    public PantallaCreditos(Main game) {
        this.game = game;
        this.batch = game.batch;
    }

    @Override
    public void show() {
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(3.5f);
        fontTitulo.setColor(Color.GOLD);

        fontTexto = new BitmapFont();
        fontTexto.getData().setScale(3.0f);
        fontTexto.setColor(Color.WHITE);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Título
        fontTitulo.draw(batch, "CRÉDITOS",
            Gdx.graphics.getWidth()/2 - 80,
            Gdx.graphics.getHeight() - 100);

        // Información de créditos
        fontTexto.draw(batch, "Desarrollado por:", 100, 400);
        fontTexto.draw(batch, "Isidoro Granados Osorio | Arroyo Juarez Joseph Dylan | Salazar Hernandez Alejandro", 150, 350);

        fontTexto.draw(batch, "Música: M. J. Hood Music (Youtube)", 100, 250);
        fontTexto.draw(batch, "Gráficos / Motor / Herramientas de Desarrollo: LibGDX | Android Studio | Linux | Java", 100, 200);

        fontTexto.draw(batch, "Presiona la pantalla para salir.", 100, 100);

        batch.end();

        // Volver al menú principal
        if (Gdx.input.justTouched() ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK)) {
            game.setScreen(new PantallaInicio(game));
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        fontTitulo.dispose();
        fontTexto.dispose();
    }
}
