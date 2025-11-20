package io.github.game_izzy_magordito2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class NivelManager {
    private String[] niveles = {"level1.tmx", "level2.tmx", "level3.tmx"};
    private int nivelActual = 0;
    private TmxMapLoader mapLoader;

    public NivelManager() {
        mapLoader = new TmxMapLoader();
    }

    public TiledMap cargarNivelActual() {
        try {
            Gdx.app.log("NIVEL", "Intentando cargar: " + niveles[nivelActual]);
            TiledMap mapa = mapLoader.load(niveles[nivelActual]);
            Gdx.app.log("NIVEL", "Nivel cargado exitosamente: " + niveles[nivelActual]);
            return mapa;
        } catch (Exception e) {
            Gdx.app.error("NIVEL", "ERROR cargando nivel: " + niveles[nivelActual] + " - " + e.getMessage());
            return null;
        }
    }

    public void siguienteNivel() {
        nivelActual++;
        if (nivelActual >= niveles.length) {
            nivelActual = niveles.length - 1;
            Gdx.app.log("NIVEL", "Último nivel alcanzado");
        }
    }

    public int getNivelActual() {
        return nivelActual + 1;
    }

    public String getNombreNivelActual() {
        return niveles[nivelActual];
    }

    public boolean esUltimoNivel() {
        return nivelActual == niveles.length - 1;
    }

    public boolean hayMasNiveles() {
        return nivelActual < niveles.length - 1;
    }
}
