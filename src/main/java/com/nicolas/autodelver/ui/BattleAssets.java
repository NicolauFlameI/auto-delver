package com.nicolas.autodelver.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

import com.nicolas.autodelver.domain.Combatant;

/**
 * Responsabilidade Unica: Carregar, fornecer e descartar da memoria da GPU (VRAM)
 * todos os recursos visuais (Texturas, Animacoes e Fontes) da tela de batalha.
 *
 * IMPORTANTE: Esta classe deve ser instanciada apenas dentro do metodo create()
 * do LibGDX, pois precisa do contexto OpenGL ativo para carregar imagens.
 */
public class BattleAssets implements Disposable {

    private static final float IDLE_FRAME_DURATION = 0.15f;

    // Lista centralizada para garantir que nenhuma textura fique vazando memoria (memory leak)
    private final List< Texture > loadedTextures = new ArrayList< >();

    // Texturas do cenario (Masmorra)
    private final Texture[] floorTiles;
    private final Texture wallMid;
    private final Texture wallTopMid;
    private final Texture wallBannerRed;

    // Animacoes de respiracao (Idle) dos personagens
    private final Animation< Texture > paladinoIdle;
    private final Animation< Texture > arqueiraIdle;
    private final Animation< Texture > esqueletoIdle;
    private final Animation< Texture > goblinIdle;
    private final Animation< Texture > orcIdle;

    // Fontes tipograficas
    private final BitmapFont damageFont;
    private final BitmapFont logFont;
    private final BitmapFont statusFont;

    public BattleAssets() {
        // 1. Configura as fontes em escalas adequadas para a resolucao virtual 320x180
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.42f);
        damageFont.setUseIntegerPositions(false);

        logFont = new BitmapFont();
        logFont.getData().setScale(0.38f);
        logFont.setUseIntegerPositions(false);

        statusFont = new BitmapFont();
        statusFont.getData().setScale(0.33f);
        statusFont.setUseIntegerPositions(false);

        // 2. Carrega os ladrilhos (tiles) de piso e parede da masmorra
        floorTiles = new Texture[8];
        for (int i = 0; i < 8; i++) {
            floorTiles[i] = loadSingleTexture("floor_" + (i + 1) + ".png");
        }
        wallMid = loadSingleTexture("wall_mid.png");
        wallTopMid = loadSingleTexture("wall_top_mid.png");
        wallBannerRed = loadSingleTexture("wall_banner_red.png");

        // 3. Carrega os 4 quadros (f0 a f3) das animacoes de cada combatente
        paladinoIdle = loadAnimation("knight_m_idle", IDLE_FRAME_DURATION);
        arqueiraIdle = loadAnimation("elf_m_idle", IDLE_FRAME_DURATION);
        esqueletoIdle = loadAnimation("skelet_idle", IDLE_FRAME_DURATION);
        goblinIdle = loadAnimation("goblin_idle", IDLE_FRAME_DURATION);
        orcIdle = loadAnimation("orc_warrior_idle", IDLE_FRAME_DURATION);
    }

    private Texture loadSingleTexture(String fileName) {
        Texture tex = new Texture(Gdx.files.internal("assets/frames/" + fileName));
        loadedTextures.add(tex);
        return tex;
    }

    private Animation< Texture > loadAnimation(String prefix, float frameDuration) {
        Texture[] frames = new Texture[4];
        for (int i = 0; i < 4; i++) {
            frames[i] = loadSingleTexture(prefix + "_anim_f" + i + ".png");
        }
        Animation< Texture > animation = new Animation< >(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    // Mapeia o nome do combatente do Dominio para a sua respectiva animacao visual
    public Animation< Texture > getAnimationForCombatant(Combatant c) {
        String name = c.getName().toLowerCase();
        if (name.contains("paladino")) return paladinoIdle;
        if (name.contains("arqueira")) return arqueiraIdle;
        if (name.contains("esqueleto")) return esqueletoIdle;
        if (name.contains("goblin")) return goblinIdle;
        if (name.contains("orc")) return orcIdle;

        return paladinoIdle; // Fallback seguro
    }

    public Texture getFloorTile(int index) {
        return floorTiles[index];
    }

    public Texture getWallMid() {
        return wallMid;
    }

    public Texture getWallTopMid() {
        return wallTopMid;
    }

    public Texture getWallBannerRed() {
        return wallBannerRed;
    }

    public BitmapFont getDamageFont() {
        return damageFont;
    }

    public BitmapFont getLogFont() {
        return logFont;
    }

    public BitmapFont getStatusFont() {
        return statusFont;
    }

    @Override
    public void dispose() {
        damageFont.dispose();
        logFont.dispose();
        statusFont.dispose();
        for (Texture tex : loadedTextures) {
            tex.dispose();
        }
    }
}