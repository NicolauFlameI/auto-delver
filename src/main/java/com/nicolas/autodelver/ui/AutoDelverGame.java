package com.nicolas.autodelver.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.domain.TurnLog;

public class AutoDelverGame extends ApplicationAdapter {

    // Gerenciadores de desenho
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont damageFont;
    private BitmapFont logFont;

    // Texturas do cenario (Masmorra)
    private Texture[] floorTiles;
    private Texture wallMid;
    private Texture wallTopMid;
    private Texture wallBannerRed;
    private int[][] floorGrid;

    // Animacoes de Idle para cada arquetipo
    private Animation< Texture > paladinoIdle;
    private Animation< Texture > arqueiraIdle;
    private Animation< Texture > esqueletoIdle;
    private Animation< Texture > goblinIdle;
    private Animation< Texture > orcIdle;

    private final List< Texture > loadedTextures = new ArrayList<>();
    private final List< FloatingText > floatingTexts = new ArrayList<>();

    // Resolucao virtual do mundo (16:9) e tamanho do ladrilho
    private static final float VIRTUAL_WIDTH = 320f;
    private static final float VIRTUAL_HEIGHT = 180f;
    private static final int TILE_SIZE = 16;
    private static final int GRID_COLS = 20; // 320 / 16
    private static final int GRID_ROWS = 12; // Cobre os 180 de altura

    // Paleta de cores
    private static final Color BACKGROUND = Color.valueOf("0F0D1A");
    private static final Color DUNGEON_TINT = Color.valueOf("9A94B8"); // Escurece o chao para destacar os personagens
    private static final Color DEAD_COLOR = Color.valueOf("4B4A6E");
    private static final Color BAR_BACKGROUND = Color.valueOf("2A2540");
    private static final Color HP_GREEN = Color.valueOf("5FB356");
    private static final Color HP_YELLOW = Color.valueOf("F2C94C");
    private static final Color HP_RED = Color.valueOf("C23B3B");
    private static final Color DAMAGE_TEXT_COLOR = Color.valueOf("FF4D4D");
    private static final Color HUD_BG_COLOR = Color.valueOf("181425");
    private static final Color HUD_BORDER_COLOR = Color.valueOf("3A3554");
    private static final Color HUD_TEXT_COLOR = Color.valueOf("E0DEF4");

    // Medidas e posicoes dos combatentes
    private static final float BAR_WIDTH = 24f;
    private static final float BAR_HEIGHT = 3f;
    private static final float ROW_SPACING = 36f;
    private static final float HERO_X = 60f;
    private static final float ENEMY_X = 244f;

    // Medidas do painel de log (HUD)
    private static final float HUD_X = 14f;
    private static final float HUD_Y = 6f;
    private static final float HUD_WIDTH = 292f;
    private static final float HUD_HEIGHT = 20f;

    // Tempos de reproducao e animacao
    private static final float SECONDS_PER_ACTION = 0.8f;
    private static final float FLASH_DURATION = 0.15f;
    private static final float IDLE_FRAME_DURATION = 0.15f;

    private final Party heroParty;
    private final Party enemyParty;
    private final List< TurnLog > history;
    private final Map< String, Integer > displayedHp = new HashMap<>();

    private int nextLogIndex = 0;
    private float actionTimer = 0f;
    private float flashTimer = 0f;
    private String flashTargetId = null;
    private float stateTime = 0f;
    private String currentLogText = "Iniciando simulacao de batalha...";

    public AutoDelverGame(Party heroParty, Party enemyParty, List< TurnLog > history) {
        this.heroParty = heroParty;
        this.enemyParty = enemyParty;
        this.history = history;

        for (Combatant c : heroParty.getAllMembers()) {
            displayedHp.put(c.getId(), c.getMaxHp());
        }
        for (Combatant c : enemyParty.getAllMembers()) {
            displayedHp.put(c.getId(), c.getMaxHp());
        }
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Fonte dos numeros flutuantes
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.42f);
        damageFont.setUseIntegerPositions(false);

        // Fonte da caixa de log inferior
        logFont = new BitmapFont();
        logFont.getData().setScale(0.38f);
        logFont.setUseIntegerPositions(false);

        // Carrega texturas da masmorra
        loadDungeonAssets();

        // Carrega os 4 quadros (f0 a f3) de cada animacao
        paladinoIdle = loadAnimation("knight_m_idle", IDLE_FRAME_DURATION);
        arqueiraIdle = loadAnimation("elf_m_idle", IDLE_FRAME_DURATION);
        esqueletoIdle = loadAnimation("skelet_idle", IDLE_FRAME_DURATION);
        goblinIdle = loadAnimation("goblin_idle", IDLE_FRAME_DURATION);
        orcIdle = loadAnimation("orc_warrior_idle", IDLE_FRAME_DURATION);
    }

    private void loadDungeonAssets() {
        floorTiles = new Texture[8];
        for (int i = 0; i < 8; i++) {
            floorTiles[i] = loadSingleTexture("floor_" + (i + 1) + ".png");
        }
        wallMid = loadSingleTexture("wall_mid.png");
        wallTopMid = loadSingleTexture("wall_top_mid.png");
        wallBannerRed = loadSingleTexture("wall_banner_red.png");

        // Gera o mosaico do piso (75% piso liso floor_1, 25% pisos rachados/variados)
        floorGrid = new int[GRID_ROWS][GRID_COLS];
        Random rng = new Random(42L); // Semente fixa para manter o padrao consistente
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (rng.nextFloat() < 0.75f) {
                    floorGrid[row][col] = 0; // floor_1.png
                } else {
                    floorGrid[row][col] = 1 + rng.nextInt(7); // floor_2 a floor_8
                }
            }
        }
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
        Animation< Texture > animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
        return animation;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;
        updateReplay(delta);
        updateFloatingTexts(delta);

        ScreenUtils.clear(BACKGROUND);
        viewport.apply();

        // 1. CENARIO DE FUNDO E SPRITES DOS PERSONAGENS
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawDungeonBackground();
        drawPartySprites(heroParty, HERO_X, false);
        drawPartySprites(enemyParty, ENEMY_X, true);
        batch.end();

        // 2. FORMAS GEOMETRICAS (Fundo da HUD, bordas e barras de vida por cima do cenario)
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHudPanel();
        drawPartyHpBars(heroParty, HERO_X);
        drawPartyHpBars(enemyParty, ENEMY_X);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawHudBorder();
        shapeRenderer.end();

        // 3. TEXTOS EM PRIMEIRO PLANO (Numeros flutuantes e texto do log na HUD)
        batch.begin();
        drawFloatingTexts();
        drawHudText();
        batch.end();
    }

    private void drawDungeonBackground() {
        batch.setColor(DUNGEON_TINT);

        // Desenha o piso da linha 0 ate a linha 9 (ate Y = 160)
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                Texture tile = floorTiles[floorGrid[row][col]];
                batch.draw(tile, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Desenha a parede de fundo na linha 9 (Y = 144) e a borda superior em Y = 160
        for (int col = 0; col < GRID_COLS; col++) {
            float x = col * TILE_SIZE;
            // Estandartes vermelhos decorativos em colunas simetricas
            if (col == 4 || col == 9 || col == 10 || col == 15) {
                batch.draw(wallBannerRed, x, 9 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            } else {
                batch.draw(wallMid, x, 9 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
            batch.draw(wallTopMid, x, 10 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        batch.setColor(Color.WHITE);
    }

    private void updateReplay(float delta) {
        if (flashTimer > 0f) {
            flashTimer -= delta;
        }
        if (nextLogIndex >= history.size()) {
            return;
        }

        actionTimer += delta;
        if (actionTimer >= SECONDS_PER_ACTION) {
            actionTimer -= SECONDS_PER_ACTION;
            TurnLog log = history.get(nextLogIndex);
            nextLogIndex++;

            displayedHp.put(log.targetId(), log.targetRemainingHp());
            flashTargetId = log.targetId();
            flashTimer = FLASH_DURATION;

            String defeatedText = log.targetDefeated() ? " (Derrotado!)" : "";
            currentLogText = log.attackerName() + " atacou " + log.targetName()
                    + " causando " + log.damageDealt() + " de dano" + defeatedText;

            if (nextLogIndex >= history.size()) {
                boolean heroesWon = isPartyAlive(heroParty);
                currentLogText = heroesWon ? "Vitoria dos Herois! Masmorra concluida." : "Derrota! A equipe sucumbiu.";
            }

            if (log.damageDealt() > 0) {
                float[] targetPos = getCombatantCoordinates(log.targetId());
                floatingTexts.add(new FloatingText(
                        "-" + log.damageDealt(),
                        targetPos[0] + 2f,
                        targetPos[1] + 32f,
                        0.75f,
                        DAMAGE_TEXT_COLOR
                ));
            }
        }
    }

    private boolean isPartyAlive(Party party) {
        for (Combatant c : party.getAllMembers()) {
            if (displayedHp.getOrDefault(c.getId(), 0) > 0) {
                return true;
            }
        }
        return false;
    }

    private void updateFloatingTexts(float delta) {
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.y += 18f * delta;
            ft.remainingTime -= delta;
            if (ft.remainingTime <= 0f) {
                floatingTexts.remove(i);
            }
        }
    }

    private void drawFloatingTexts() {
        for (FloatingText ft : floatingTexts) {
            float alpha = Math.max(0f, ft.remainingTime / ft.totalDuration);
            damageFont.setColor(ft.color.r, ft.color.g, ft.color.b, alpha);
            damageFont.draw(batch, ft.text, ft.x, ft.y);
        }
        damageFont.setColor(Color.WHITE);
    }

    private void drawHudPanel() {
        shapeRenderer.setColor(HUD_BG_COLOR);
        shapeRenderer.rect(HUD_X, HUD_Y, HUD_WIDTH, HUD_HEIGHT);
    }

    private void drawHudBorder() {
        shapeRenderer.setColor(HUD_BORDER_COLOR);
        shapeRenderer.rect(HUD_X, HUD_Y, HUD_WIDTH, HUD_HEIGHT);
    }

    private void drawHudText() {
        logFont.setColor(HUD_TEXT_COLOR);
        logFont.draw(batch, currentLogText, HUD_X + 8f, HUD_Y + 14f);
    }

    private float[] getCombatantCoordinates(String id) {
        List< Combatant > heroes = heroParty.getAllMembers();
        float heroTopY = (VIRTUAL_HEIGHT + 12f) / 2f + ((heroes.size() - 1) * ROW_SPACING) / 2f;
        for (int i = 0; i < heroes.size(); i++) {
            if (heroes.get(i).getId().equals(id)) {
                return new float[] { HERO_X, heroTopY - (i * ROW_SPACING) };
            }
        }

        List< Combatant > enemies = enemyParty.getAllMembers();
        float enemyTopY = (VIRTUAL_HEIGHT + 12f) / 2f + ((enemies.size() - 1) * ROW_SPACING) / 2f;
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).getId().equals(id)) {
                return new float[] { ENEMY_X, enemyTopY - (i * ROW_SPACING) };
            }
        }

        return new float[] { VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f };
    }

    private Animation< Texture > getAnimationForCombatant(Combatant c) {
        String name = c.getName().toLowerCase();
        if (name.contains("paladino")) return paladinoIdle;
        if (name.contains("arqueira")) return arqueiraIdle;
        if (name.contains("esqueleto")) return esqueletoIdle;
        if (name.contains("goblin")) return goblinIdle;
        if (name.contains("orc")) return orcIdle;

        return paladinoIdle;
    }

    private void drawPartySprites(Party party, float x, boolean flipX) {
        List< Combatant > members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = (VIRTUAL_HEIGHT + 12f) / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * ROW_SPACING;
            int hp = displayedHp.getOrDefault(member.getId(), member.getMaxHp());

            if (hp <= 0) {
                batch.setColor(DEAD_COLOR);
            } else if (flashTimer > 0f && member.getId().equals(flashTargetId)) {
                batch.setColor(Color.RED);
            } else {
                batch.setColor(Color.WHITE);
            }

            Animation< Texture > anim = getAnimationForCombatant(member);
            Texture currentFrame = (hp <= 0) ? anim.getKeyFrames()[0] : anim.getKeyFrame(stateTime, true);

            float w = currentFrame.getWidth();
            float h = currentFrame.getHeight();

            batch.draw(currentFrame, x, y, w, h, 0, 0, (int) w, (int) h, flipX, false);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawPartyHpBars(Party party, float x) {
        List< Combatant > members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = (VIRTUAL_HEIGHT + 12f) / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * ROW_SPACING;
            int hp = displayedHp.getOrDefault(member.getId(), member.getMaxHp());

            Animation< Texture > anim = getAnimationForCombatant(member);
            Texture currentFrame = anim.getKeyFrames()[0];

            float barX = x + (currentFrame.getWidth() / 2f) - (BAR_WIDTH / 2f);
            float barY = y + currentFrame.getHeight() + 3f;

            drawHpBar(barX, barY, hp, member.getMaxHp());
        }
    }

    private void drawHpBar(float x, float y, int hp, int maxHp) {
        shapeRenderer.setColor(BAR_BACKGROUND);
        shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

        if (hp <= 0) return;

        float ratio = (float) hp / maxHp;
        float fillWidth = Math.max(1f, Math.round(BAR_WIDTH * ratio));

        shapeRenderer.setColor(hpColor(ratio));
        shapeRenderer.rect(x, y, fillWidth, BAR_HEIGHT);
    }

    private Color hpColor(float ratio) {
        if (ratio > 0.5f) return HP_GREEN;
        if (ratio > 0.25f) return HP_YELLOW;
        return HP_RED;
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        damageFont.dispose();
        logFont.dispose();
        for (Texture tex : loadedTextures) {
            tex.dispose();
        }
    }

    private static class FloatingText {
        private final String text;
        private final float x;
        private float y;
        private float remainingTime;
        private final float totalDuration;
        private final Color color;

        public FloatingText(String text, float x, float y, float duration, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.remainingTime = duration;
            this.totalDuration = duration;
            this.color = color;
        }
    }
}