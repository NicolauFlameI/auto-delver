package com.nicolas.autodelver.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

import com.nicolas.autodelver.domain.Combatant;

/**
 * Responsabilidade Unica: Carregar, fornecer e descartar da memoria (VRAM e OpenAL)
 * todos os recursos visuais (Texturas, Animacoes, Fontes) e sonoros (Sound) da batalha.
 *
 * IMPORTANTE: Deve ser instanciada apenas dentro do metodo create() do LibGDX.
 */
public class BattleAssets implements Disposable {

    private static final float IDLE_FRAME_DURATION = 0.15f;

    // Listas centralizadas para garantir descarte completo no dispose()
    private final List< Texture > loadedTextures = new ArrayList< >();
    private final List< Sound > loadedSounds = new ArrayList< >();
    private final Random pitchRng = new Random();

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

    // Efeitos sonoros (carregados inteiramente em memoria RAM para baixa latencia)
    private final Sound hitSound;
    private final Sound killSound;
    private final Sound victorySound;
    private final Sound defeatSound;

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

        // 4. Carrega os efeitos sonoros 8-bit da pasta assets/sounds/
        hitSound = loadSingleSound("hit.wav");
        killSound = loadSingleSound("kill.wav");
        victorySound = loadSingleSound("victory.wav");
        defeatSound = loadSingleSound("defeat.wav");
    }

    private Texture loadSingleTexture(String fileName) {
        Texture tex = new Texture(Gdx.files.internal("assets/frames/" + fileName));
        loadedTextures.add(tex);
        return tex;
    }

    private Sound loadSingleSound(String fileName) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/" + fileName));
        loadedSounds.add(sound);
        return sound;
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

    public Animation< Texture > getAnimationForCombatant(Combatant c) {
        String name = c.getName().toLowerCase();
        if (name.contains("paladino")) return paladinoIdle;
        if (name.contains("arqueira")) return arqueiraIdle;
        if (name.contains("esqueleto")) return esqueletoIdle;
        if (name.contains("goblin")) return goblinIdle;
        if (name.contains("orc")) return orcIdle;

        return paladinoIdle;
    }

    // Toca o som de golpe variando levemente o pitch (tom) entre 0.9x e 1.15x para soar mais natural
    public void playHitSound() {
        float randomPitch = 0.9f + pitchRng.nextFloat() * 0.25f;
        // Parametros do play: (volume, pitch, pan)
        hitSound.play(0.6f, randomPitch, 0f);
    }

    public void playKillSound() {
        killSound.play(0.75f);
    }

    public void playVictorySound() {
        victorySound.play(0.8f);
    }

    public void playDefeatSound() {
        defeatSound.play(0.8f);
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
        // Sons no OpenAL tambem ocupam memoria nativa fora da JVM e precisam de dispose()
        for (Sound sound : loadedSounds) {
            sound.dispose();
        }
    }
}