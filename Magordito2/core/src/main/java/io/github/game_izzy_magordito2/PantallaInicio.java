package io.github.game_izzy_magordito2;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class PantallaInicio implements Screen {

    private Main game;
    private Music musicaMenu;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont fontTitulo;
    private BitmapFont fontBotones;
    private Texture fondo;

    // Botones
    private Rectangle btnJugar;
    private Rectangle btnCreditos;
    private Rectangle btnSalir;

    public PantallaInicio(Main game) {
        this.game = game;
        this.batch = game.batch;
    }

    @Override
    public void show() {
        // Inicia ShapeRenderer para dibujar formas
        shapeRenderer = new ShapeRenderer();

        // inciiar fuentes...
        fontTitulo = new BitmapFont();
        fontTitulo.getData().setScale(3.0f);
        fontTitulo.setColor(Color.GOLD);

        fontBotones = new BitmapFont();
        fontBotones.getData().setScale(2.0f);

        // Cargar imagen de titulo
        try {
            fondo = new Texture("fondo_menu.png");
        } catch (Exception e) {
            fondo = null;
        }


        try {
            musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("Inicio.mp3"));
            musicaMenu.setLooping(true);
            musicaMenu.setVolume(0.7f);
            musicaMenu.play();
        } catch (Exception e) {
            Gdx.app.error("MUSICA", "No se pudo cargar música del menú: " + e.getMessage());
            musicaMenu = null;
        }

        int anchoBoton = 300;
        int altoBoton = 80;
        int centroX = Gdx.graphics.getWidth() / 2 - anchoBoton / 2;

        btnJugar = new Rectangle(centroX, 400, anchoBoton, altoBoton);
        btnCreditos = new Rectangle(centroX, 280, anchoBoton, altoBoton);
        btnSalir = new Rectangle(centroX, 160, anchoBoton, altoBoton);
    }


    public void detenerMusica() {
        if (musicaMenu != null) {
            musicaMenu.stop();
        }
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (fondo != null) {
            batch.begin();
            batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
        }

        // Dibujar botones ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Botón JUGAR
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(btnJugar.x, btnJugar.y, btnJugar.width, btnJugar.height);

        // Botón de creditos...
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(btnCreditos.x, btnCreditos.y, btnCreditos.width, btnCreditos.height);

        // Botón SALIR...
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(btnSalir.x, btnSalir.y, btnSalir.width, btnSalir.height);

        shapeRenderer.end();

        // Dibuja bordes de botones...
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(btnJugar.x, btnJugar.y, btnJugar.width, btnJugar.height);
        shapeRenderer.rect(btnCreditos.x, btnCreditos.y, btnCreditos.width, btnCreditos.height);
        shapeRenderer.rect(btnSalir.x, btnSalir.y, btnSalir.width, btnSalir.height);
        shapeRenderer.end();

        // se escriben los textos...
        batch.begin();

        // Título del juego
        fontTitulo.draw(batch, "  MAGORDITO 2D",
            Gdx.graphics.getWidth()/2 - 180,
            Gdx.graphics.getHeight() - 100);

        // Texto de los botones
        fontBotones.setColor(Color.WHITE);

        // Botón JUGAR
        float textoJugarX = btnJugar.x + (btnJugar.width - fontBotones.getXHeight() * 5) / 2;
        float textoJugarY = btnJugar.y + btnJugar.height / 2 + fontBotones.getCapHeight() / 2;
        fontBotones.draw(batch, "JUGAR", textoJugarX, textoJugarY);

        // Botón CRÉDITOS
        float textoCreditosX = btnCreditos.x + (btnCreditos.width - fontBotones.getXHeight() * 8) / 2;
        float textoCreditosY = btnCreditos.y + btnCreditos.height / 2 + fontBotones.getCapHeight() / 2;
        fontBotones.draw(batch, "CRÉDITOS", textoCreditosX, textoCreditosY);

        // Botón SALIR
        float textoSalirX = btnSalir.x + (btnSalir.width - fontBotones.getXHeight() * 5) / 2;
        float textoSalirY = btnSalir.y + btnSalir.height / 2 + fontBotones.getCapHeight() / 2;
        fontBotones.draw(batch, "SALIR", textoSalirX, textoSalirY);

        batch.end();


        manejarInput();
    }

    private void manejarInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);

            float touchY = Gdx.graphics.getHeight() - touchPos.y;

            if (btnJugar.contains(touchPos.x, touchY)) {
                detenerMusica();
                game.setScreen(new Juego(game));
            } else if (btnCreditos.contains(touchPos.x, touchY)) {
                detenerMusica();
                game.setScreen(new PantallaCreditos(game));
            } else if (btnSalir.contains(touchPos.x, touchY)) {
                detenerMusica();
                Gdx.app.exit();
            }
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK)) {
            Gdx.app.exit();
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
        fontBotones.dispose();
        if (fondo != null) fondo.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
