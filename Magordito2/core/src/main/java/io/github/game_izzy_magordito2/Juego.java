package io.github.game_izzy_magordito2;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.audio.Sound;

public class Juego implements Screen {

    private Main game;
    private Music musicaFondo;
    private Music musicaFondo2;
    private Music musicaFondo3;
    Sound salto;
    Sound monedasound;
    private NivelManager nivelManager;

    static class Koala {
        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 10f;
        static float JUMP_VELOCITY = 40f;
        static float DAMPING = 0.87f;

        enum State {
            Standing, Walking, Jumping
        }

        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        State state = State.Walking;
        float stateTime = 0;
        boolean facesRight = true;
        boolean grounded = false;
    }

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Texture koalaTexture;
    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Koala koala;

    // HUD
    private SpriteBatch hudBatch;
    private BitmapFont font;
    private int puntuacion = 0;
    private int vidas = 3;
    private float tiempoJuego = 0;
    private int monedasRecolectadas = 0;
    private int nivel = 1;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject () {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private Array<Vector2> monedas = new Array<Vector2>();
    private Array<Boolean> monedasRecolectadasArray = new Array<Boolean>();
    //  sistema de vidas...
    private boolean perdiendoVida = false;
    private float tiempoPerdidaVida = 0;
    private static final float TIEMPO_RESPAWN = 1.5f;
    private boolean gameOver = false;
    private float tiempoGameOver = 0;
    private static final float TIEMPO_GAME_OVER = 3.0f;

    private static final float GRAVITY = -2.5f;

    private boolean debug = false;
    private ShapeRenderer debugRenderer;

    //  ...para cambio de nivel...
    private boolean cambiandoNivel = false;
    private float tiempoCambioNivel = 0;
    private static final float TIEMPO_ESPERA_CAMBIO = 2.0f;


    public Juego(Main game) {
        this.game = game;
        this.nivelManager = new NivelManager();
    }

    @Override
    public void show() {
        koala = new Koala();
        koala.position.set(2, 2);

        koalaTexture = new Texture("koalio.png");
        TextureRegion[] regions = TextureRegion.split(koalaTexture, 18, 26)[0];
        stand = new Animation<TextureRegion>(0, regions[0]);
        jump = new Animation<TextureRegion>(0, regions[1]);
        walk = new Animation<TextureRegion>(0.15f, regions[2], regions[3], regions[4]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        Koala.WIDTH = 1 / 16f * regions[0].getRegionWidth();
        Koala.HEIGHT = 1 / 16f * regions[0].getRegionHeight();

        cargarNivel(nivelManager.cargarNivelActual());

        hudBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.2f);

        musicaFondo = Gdx.audio.newMusic(Gdx.files.internal("Level1.mp3"));
        musicaFondo.setLooping(true);
        musicaFondo.setVolume(0.6f);
        musicaFondo.play();

        musicaFondo2 = Gdx.audio.newMusic(Gdx.files.internal("Level2.mp3"));
        musicaFondo2.setLooping(true);
        musicaFondo2.setVolume(0.6f);

        musicaFondo3 = Gdx.audio.newMusic(Gdx.files.internal("Level3.mp3"));
        musicaFondo3.setLooping(true);
        musicaFondo3.setVolume(0.6f);

        salto = Gdx.audio.newSound(Gdx.files.internal("Jump1.wav"));
        monedasound = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();

        debugRenderer = new ShapeRenderer();
    }

    private void cargarMonedas() {
        monedas.clear();
        monedasRecolectadasArray.clear();

        TiledMapTileLayer monedasLayer = (TiledMapTileLayer)map.getLayers().get("monedas");
        if (monedasLayer == null) {
            Gdx.app.log("MONEDAS", "No se encontró la capa 'monedas'");
            return;
        }

        for (int y = 0; y < monedasLayer.getHeight(); y++) {
            for (int x = 0; x < monedasLayer.getWidth(); x++) {
                Cell cell = monedasLayer.getCell(x, y);
                if (cell != null) {
                    monedas.add(new Vector2(x + 0.5f, y + 0.5f));
                    monedasRecolectadasArray.add(false);
                }
            }
        }

        Gdx.app.log("MONEDAS", "Cargadas " + monedas.size + " monedas");
    }

    private void cargarNivel(TiledMap nuevoMapa) {
        if (nuevoMapa == null) {
            Gdx.app.error("NIVEL", "Error: Mapa es null");
            return;
        }

        if (map != null) {
            map.dispose();
        }
        if (renderer != null) {
            renderer.dispose();
        }

        map = nuevoMapa;
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        nivel = nivelManager.getNivelActual();

        // Cargar monedas del nivel actual
        cargarMonedas();

        if (koala != null) {
            koala.position.set(2, 2);
            koala.velocity.set(0, 0);
            koala.grounded = false;
        }

        // Reiniciar estados de muerte/respawn
        perdiendoVida = false;
        gameOver = false;

        Gdx.app.log("NIVEL", "Nivel " + nivel + " cargado: " + nivelManager.getNombreNivelActual());
    }

    @Override
    public void render(float delta) {
        if (gameOver) {
            renderGameOver(delta);
            return;
        }

        // Manejar cambio de nivel
        if (cambiandoNivel) {
            renderTransicionNivel(delta);
            return;
        }

        //  respawn después de perder vida
        if (perdiendoVida) {
            renderRespawn(delta);
            return;
        }

        ScreenUtils.clear(0.7f, 0.7f, 1.0f, 1);

        tiempoJuego += Gdx.graphics.getDeltaTime();
        actualizarPuntuacion();

        verificarCambioNivel();

        verificarMuerte();

        verificarRecoleccionMonedas();

        updateKoala(delta);

        camera.position.x = koala.position.x;
        camera.update();

        renderer.setView(camera);
        renderer.render();

        // Renderizar monedas
        renderMonedas();

        renderKoala(delta);

        renderHUD();

        if (debug) renderDebug();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            detenerMusica();
            game.setScreen(new PantallaInicio(game));
        }
    }

    private void verificarMuerte() {
        // perdemos vida si cae por un precipicio (posición Y muy baja)
        if (koala.position.y < -5f) {
            perderVida();
            return;
        }

        // Morir si toca una capa de "peligro"
        TiledMapTileLayer peligroLayer = (TiledMapTileLayer)map.getLayers().get("peligro");
        if (peligroLayer != null) {
            Rectangle koalaRect = new Rectangle(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);

            // Verificar colisión con celdas de peligro
            int startX = (int)koala.position.x;
            int startY = (int)koala.position.y;
            int endX = (int)(koala.position.x + Koala.WIDTH);
            int endY = (int)(koala.position.y + Koala.HEIGHT);

            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    Cell cell = peligroLayer.getCell(x, y);
                    if (cell != null) {
                        perderVida();
                        return;
                    }
                }
            }
        }
    }

    private void perderVida() {
        if (perdiendoVida || gameOver) return; // Evitar múltiples pérdidas de vida

        vidas--;
        perdiendoVida = true;
        tiempoPerdidaVida = 0;

        Gdx.app.log("VIDAS", "Vida perdida! Vidas restantes: " + vidas);

        if (vidas <= 0) {
            gameOver();
        }
    }

    private void gameOver() {
        gameOver = true;
        tiempoGameOver = 0;
        Gdx.app.log("GAME", "Game Over!");
    }

    private void renderRespawn(float delta) {
        tiempoPerdidaVida += delta;

        // Pantalla negra con mensaje
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        hudBatch.begin();
        hudBatch.setProjectionMatrix(hudBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        float densidad = Gdx.graphics.getDensity();

        font.getData().setScale(2.0f * densidad);
        font.setColor(Color.RED);
        font.draw(hudBatch, "¡VIDA PERDIDA!",
            Gdx.graphics.getWidth()/2 - 100 * densidad,
            Gdx.graphics.getHeight()/2 + 50 * densidad);

        font.getData().setScale(1.3f * densidad);
        font.setColor(Color.WHITE);
        font.draw(hudBatch, "Vidas restantes: " + vidas,
            Gdx.graphics.getWidth()/2 - 80 * densidad,
            Gdx.graphics.getHeight()/2 - 20 * densidad);

        font.draw(hudBatch, "Reiniciando nivel...",
            Gdx.graphics.getWidth()/2 - 90 * densidad,
            Gdx.graphics.getHeight()/2 - 60 * densidad);

        hudBatch.end();

        // Después del tiempo de respawn, reiniciar nivel
        if (tiempoPerdidaVida >= TIEMPO_RESPAWN) {
            reiniciarNivelActual();
        }
    }

    private void renderGameOver(float delta) {
        tiempoGameOver += delta;

        // Pantalla roja oscura
        Gdx.gl.glClearColor(0.3f, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        hudBatch.begin();
        hudBatch.setProjectionMatrix(hudBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        float densidad = Gdx.graphics.getDensity();

        font.getData().setScale(2.5f * densidad);
        font.setColor(Color.RED);
        font.draw(hudBatch, "GAME OVER",
            Gdx.graphics.getWidth()/2 - 120 * densidad,
            Gdx.graphics.getHeight()/2 + 80 * densidad);

        font.getData().setScale(1.5f * densidad);
        font.setColor(Color.WHITE);
        font.draw(hudBatch, "Se te acabaron las vidas",
            Gdx.graphics.getWidth()/2 - 150 * densidad,
            Gdx.graphics.getHeight()/2 + 20 * densidad);

        font.draw(hudBatch, "Puntuación final: " + puntuacion,
            Gdx.graphics.getWidth()/2 - 120 * densidad,
            Gdx.graphics.getHeight()/2 - 30 * densidad);

        font.draw(hudBatch, "Monedas recolectadas: " + monedasRecolectadas,
            Gdx.graphics.getWidth()/2 - 140 * densidad,
            Gdx.graphics.getHeight()/2 - 80 * densidad);

        font.getData().setScale(1.2f * densidad);
        font.setColor(Color.YELLOW);
        font.draw(hudBatch, "Volviendo al menú principal...",
            Gdx.graphics.getWidth()/2 - 140 * densidad,
            Gdx.graphics.getHeight()/2 - 150 * densidad);

        hudBatch.end();

        // Después del tiempo de game over, volver al menú
        if (tiempoGameOver >= TIEMPO_GAME_OVER) {
            detenerMusica();
            game.setScreen(new PantallaInicio(game));
        }
    }

    private void reiniciarNivelActual() {
        cargarNivel(nivelManager.cargarNivelActual());
        perdiendoVida = false;
        tiempoPerdidaVida = 0;
    }

    // Método para verificar recolección de monedas
    private void verificarRecoleccionMonedas() {
        Rectangle koalaRect = new Rectangle(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        TiledMapTileLayer monedasLayer = (TiledMapTileLayer)map.getLayers().get("monedas");

        for (int i = 0; i < monedas.size; i++) {
            if (!monedasRecolectadasArray.get(i)) {
                Vector2 monedaPos = monedas.get(i);
                Rectangle monedaRect = new Rectangle(monedaPos.x - 0.3f, monedaPos.y - 0.3f, 0.6f, 0.6f);

                if (koalaRect.overlaps(monedaRect)) {
                    // Recolectar moneda
                    monedasRecolectadasArray.set(i, true);
                    monedasRecolectadas++;
                    //puntuacion += 100;


                    // ELIMINAR CELDA DEL MAPA
                    if (monedasLayer != null) {
                        int tileX = (int)monedaPos.x;
                        int tileY = (int)monedaPos.y;
                        monedasLayer.setCell(tileX, tileY, null);
                    }

                    Gdx.app.log("MONEDAS", "Moneda recolectada! Total: " + monedasRecolectadas);
                    break;
                }
            }
        }
    }

    // Método para renderizar las monedas
    private void renderMonedas() {
        Batch batch = renderer.getBatch();
        batch.begin();

        for (int i = 0; i < monedas.size; i++) {
            if (!monedasRecolectadasArray.get(i)) {
                Vector2 monedaPos = monedas.get(i);
                if (debug) {
                    debugRenderer.setProjectionMatrix(camera.combined);
                    debugRenderer.begin(ShapeType.Filled);
                    debugRenderer.setColor(Color.YELLOW);
                    debugRenderer.circle(monedaPos.x, monedaPos.y, 0.3f, 12);
                    debugRenderer.end();
                } else {
                    font.setColor(Color.YELLOW);
                    font.draw(batch, "○", monedaPos.x * 16 - 4, monedaPos.y * 16 + 4);
                }
            }
        }

        batch.end();
    }

    private void verificarCambioNivel() {
        if (koala.position.x > 210f) {
            iniciarCambioNivel();
        }
    }

    private void iniciarCambioNivel() {
        if (!cambiandoNivel) {
            cambiandoNivel = true;
            tiempoCambioNivel = 0;

            // Bonus por completar nivel
            puntuacion += 1000;
            monedasRecolectadas += 5;
            vidas++;

            Gdx.app.log("NIVEL", "Nivel " + nivel + " completado!");
        }
    }

    private void renderTransicionNivel(float delta) {
        tiempoCambioNivel += delta;
        detenerMusica();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        hudBatch.begin();
        hudBatch.setProjectionMatrix(hudBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        float densidad = Gdx.graphics.getDensity();

        if (nivelManager.esUltimoNivel()) {
            font.getData().setScale(2.5f * densidad);
            font.setColor(Color.GOLD);
            font.draw(hudBatch, "¡FELICIDADES!",
                Gdx.graphics.getWidth()/2 - 150 * densidad,
                Gdx.graphics.getHeight()/2 + 50 * densidad);

            font.getData().setScale(1.5f * densidad);
            font.draw(hudBatch, "Has completado todos los niveles",
                Gdx.graphics.getWidth()/2 - 200 * densidad,
                Gdx.graphics.getHeight()/2 - 20 * densidad);

            font.draw(hudBatch, "Puntuación final: " + puntuacion,
                Gdx.graphics.getWidth()/2 - 120 * densidad,
                Gdx.graphics.getHeight()/2 - 90 * densidad);

            // Mostrar estadísticas de monedas en pantalla final
            font.draw(hudBatch, "Monedas recolectadas: " + monedasRecolectadas,
                Gdx.graphics.getWidth()/2 - 140 * densidad,
                Gdx.graphics.getHeight()/2 - 130 * densidad);
        } else {
            font.getData().setScale(2.0f * densidad);
            font.setColor(Color.GOLD);
            font.draw(hudBatch, "¡NIVEL " + nivel + " COMPLETADO!",
                Gdx.graphics.getWidth()/2 - 180 * densidad,
                Gdx.graphics.getHeight()/2 + 30 * densidad);

            font.getData().setScale(1.3f * densidad);
            font.draw(hudBatch, "Cargando nivel " + (nivel + 1) + "...",
                Gdx.graphics.getWidth()/2 - 100 * densidad,
                Gdx.graphics.getHeight()/2 - 40 * densidad);

            // Mostrar progreso de monedas
            font.draw(hudBatch, "Monedas en este nivel: " + contarMonedasRecolectadasNivel(),
                Gdx.graphics.getWidth()/2 - 120 * densidad,
                Gdx.graphics.getHeight()/2 - 80 * densidad);
        }

        hudBatch.end();

        if (tiempoCambioNivel >= TIEMPO_ESPERA_CAMBIO) {
            if (nivelManager.esUltimoNivel()) {
                detenerMusica();
                game.setScreen(new PantallaInicio(game));
            } else {
                nivelManager.siguienteNivel();
                cargarNivel(nivelManager.cargarNivelActual());
                cambiandoNivel = false;
                if(nivel == 2){
                    detenerMusica();
                    musicaFondo2.play();
                }
                if(nivel == 3){
                    detenerMusica();
                    musicaFondo3.play();
                }
            }
        }
    }

    // Método para contar monedas recolectadas en el nivel actual
    private int contarMonedasRecolectadasNivel() {
        int count = 0;
        for (int i = 0; i < monedasRecolectadasArray.size; i++) {
            if (monedasRecolectadasArray.get(i)) {
                count++;
            }
        }
        return count;
    }

    private void actualizarPuntuacion() {
        // Puntos por movimiento, para prueba...
        if (Math.abs(koala.velocity.x) > 0.1f) {
            //puntuacion += (int)(Math.abs(koala.velocity.x) * 0.5f);
        }
    }

    private void renderHUD() {
        hudBatch.begin();
        hudBatch.setProjectionMatrix(hudBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        float densidad = Gdx.graphics.getDensity();
        float escalaBase = 0.9f * densidad;

        font.getData().setScale(escalaBase);

        // INFORMACIÓN SUPERIOR
        font.setColor(Color.YELLOW);
        font.draw(hudBatch, "PUNTOS: " + puntuacion, 40 * densidad, Gdx.graphics.getHeight() - 40 * densidad);

        font.setColor(Color.GREEN);
        font.draw(hudBatch, "VIDAS: " + vidas, 150 * densidad, Gdx.graphics.getHeight() - 40 * densidad);

        font.setColor(Color.CYAN);
        font.draw(hudBatch, "MONEDAS: " + monedasRecolectadas, 260 * densidad, Gdx.graphics.getHeight() - 40 * densidad);

        font.setColor(Color.MAGENTA);
        font.draw(hudBatch, "NIVEL: " + nivel, 370 * densidad, Gdx.graphics.getHeight() - 40 * densidad);

        font.setColor(Color.ORANGE);
        font.draw(hudBatch, String.format("TIEMPO: %02d:%02d", (int)tiempoJuego/60, (int)tiempoJuego%60),
            480 * densidad, Gdx.graphics.getHeight() - 40 * densidad);

        // CONTROLES
        font.getData().setScale(escalaBase * 1.8f);
        font.setColor(Color.WHITE);
        font.draw(hudBatch, "<-<-<-<-<-<-<- | ->->->->->->->-> | SALTAR", 40 * densidad, 37 * densidad);

        // estado del personje...
        font.getData().setScale(escalaBase * 0.9f);
        font.setColor(getColorPorEstado());
        font.draw(hudBatch, "ESTADO: " + koala.state.toString(),
            Gdx.graphics.getWidth() - 320 * densidad, Gdx.graphics.getHeight() - 65 * densidad);

        // MENSAJE DE BIENVENIDA
        if (tiempoJuego < 10 && !cambiandoNivel && !perdiendoVida && !gameOver) {
            font.getData().setScale(escalaBase * 1.5f);
            font.setColor(Color.GOLD);
            font.draw(hudBatch, "¡BIENVENIDO! Nivel " + nivel,
                Gdx.graphics.getWidth()/2 - 200 * densidad, Gdx.graphics.getHeight()/2 + 200 * densidad);
        }

        hudBatch.end();
    }

    private Color getColorPorEstado() {
        switch (koala.state) {
            case Standing: return Color.GRAY;
            case Walking: return Color.BLUE;
            case Jumping: return Color.RED;
            default: return Color.WHITE;
        }
    }

    public void detenerMusica() {
        if (musicaFondo != null) {
            musicaFondo.stop();
        }
        if (musicaFondo2 != null) {
            musicaFondo2.stop();
        }
        if (musicaFondo3 != null) {
            musicaFondo3.stop();
        }
    }

    private void updateKoala (float deltaTime) {
        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        koala.stateTime += deltaTime;

        if ((Gdx.input.isKeyPressed(Input.Keys.SPACE) || isTouched(0.5f, 1)) && koala.grounded) {
            koala.velocity.y += Koala.JUMP_VELOCITY;
            koala.state = Koala.State.Jumping;
            koala.grounded = false;
            salto.play(0.8f);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || isTouched(0, 0.25f)) {
            koala.velocity.x = -Koala.MAX_VELOCITY;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || isTouched(0.25f, 0.5f)) {
            koala.velocity.x = Koala.MAX_VELOCITY;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.B))
            debug = !debug;

        koala.velocity.add(0, GRAVITY);

        koala.velocity.x = MathUtils.clamp(koala.velocity.x, -Koala.MAX_VELOCITY, Koala.MAX_VELOCITY);

        if (Math.abs(koala.velocity.x) < 1) {
            koala.velocity.x = 0;
            if (koala.grounded) koala.state = Koala.State.Standing;
        }

        koala.velocity.scl(deltaTime);

        Rectangle koalaRect = rectPool.obtain();
        koalaRect.set(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        int startX, startY, endX, endY;

        if (koala.velocity.x > 0) {
            startX = endX = (int)(koala.position.x + Koala.WIDTH + koala.velocity.x);
        } else {
            startX = endX = (int)(koala.position.x + koala.velocity.x);
        }
        startY = (int)(koala.position.y);
        endY = (int)(koala.position.y + Koala.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.x += koala.velocity.x;
        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                koala.velocity.x = 0;
                break;
            }
        }
        koalaRect.x = koala.position.x;

        if (koala.velocity.y > 0) {
            startY = endY = (int)(koala.position.y + Koala.HEIGHT + koala.velocity.y);
        } else {
            startY = endY = (int)(koala.position.y + koala.velocity.y);
        }
        startX = (int)(koala.position.x);
        endX = (int)(koala.position.x + Koala.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.y += koala.velocity.y;
        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                if (koala.velocity.y > 0) {
                    koala.position.y = tile.y - Koala.HEIGHT;
                    TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
                    layer.setCell((int)tile.x, (int)tile.y, null);
                    puntuacion += 50;
                } else {
                    koala.position.y = tile.y + tile.height;
                    koala.grounded = true;
                }
                koala.velocity.y = 0;
                break;
            }
        }
        rectPool.free(koalaRect);

        koala.position.add(koala.velocity);
        koala.velocity.scl(1 / deltaTime);

        koala.velocity.x *= Koala.DAMPING;
    }

    private boolean isTouched (float startX, float endX) {
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX(i) / (float)Gdx.graphics.getBackBufferWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }

    private void getTiles (int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
        rectPool.freeAll(tiles);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }
            }
        }
    }

    private void renderKoala (float deltaTime) {
        TextureRegion frame = null;
        switch (koala.state) {
            case Standing:
                frame = stand.getKeyFrame(koala.stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(koala.stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(koala.stateTime);
                break;
        }

        Batch batch = renderer.getBatch();
        batch.begin();

        if (koala.facesRight) {
            batch.draw(frame, koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        } else {
            batch.draw(frame, koala.position.x + Koala.WIDTH, koala.position.y, -Koala.WIDTH, Koala.HEIGHT);
        }
        batch.end();
    }

    private void renderDebug () {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeType.Line);

        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);

        // Dibujar monedas en modo debug
        debugRenderer.setColor(Color.YELLOW);
        for (int i = 0; i < monedas.size; i++) {
            if (!monedasRecolectadasArray.get(i)) {
                Vector2 monedaPos = monedas.get(i);
                debugRenderer.circle(monedaPos.x, monedaPos.y, 0.3f, 12);
            }
        }

        // dibujado de la malla de peligro en modo debug
        TiledMapTileLayer peligroLayer = (TiledMapTileLayer)map.getLayers().get("peligro");
        if (peligroLayer != null) {
            debugRenderer.setColor(Color.RED);
            for (int y = 0; y <= peligroLayer.getHeight(); y++) {
                for (int x = 0; x <= peligroLayer.getWidth(); x++) {
                    Cell cell = peligroLayer.getCell(x, y);
                    if (cell != null) {
                        if (camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0))
                            debugRenderer.rect(x, y, 1, 1);
                    }
                }
            }
        }

        debugRenderer.setColor(Color.GREEN);
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
        for (int y = 0; y <= layer.getHeight(); y++) {
            for (int x = 0; x <= layer.getWidth(); x++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    if (camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0))
                        debugRenderer.rect(x, y, 1, 1);
                }
            }
        }
        debugRenderer.end();
    }

    @Override
    public void dispose() {
        if (hudBatch != null) hudBatch.dispose();
        if (font != null) font.dispose();
        if (musicaFondo != null) musicaFondo.dispose();
        if (koalaTexture != null) koalaTexture.dispose();
        if (map != null) map.dispose();
        if (renderer != null) renderer.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if(width <= 0 || height <= 0) return;
    }

    @Override
    public void pause() {
        if (musicaFondo != null && musicaFondo.isPlaying()) {
            musicaFondo.pause();
        }
    }

    @Override
    public void resume() {
        if (musicaFondo != null && !musicaFondo.isPlaying()) {
            musicaFondo.play();
        }
    }

    @Override
    public void hide() {
        if (musicaFondo != null) {
            musicaFondo.pause();
        }
    }
}
