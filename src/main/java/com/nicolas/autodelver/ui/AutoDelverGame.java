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

    // Gerenciadores de desenho e camera do LibGDX
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

    // Animacoes de Idle (respiracao) para cada arquetipo
    private Animation< Texture > paladinoIdle;
    private Animation< Texture > arqueiraIdle;
    private Animation< Texture > esqueletoIdle;
    private Animation< Texture > goblinIdle;
    private Animation< Texture > orcIdle;

    // Listas para gerenciar memoria da GPU e textos dinamicos na tela
    private final List< Texture > loadedTextures = new ArrayList<>();
    private final List< FloatingText > floatingTexts = new ArrayList<>();
    private final Random effectRng = new Random(); // Gerador aleatorio para o tremor de tela

    // Resolucao virtual do mundo (16:9) e tamanho do ladrilho (16x16 pixels)
    private static final float VIRTUAL_WIDTH = 320f;
    private static final float VIRTUAL_HEIGHT = 180f;
    private static final int TILE_SIZE = 16;
    private static final int GRID_COLS = 20; // 320 / 16 = 20 colunas
    private static final int GRID_ROWS = 12; // Cobre os 180 de altura

    // Paleta de cores em hexadecimal
    private static final Color BACKGROUND = Color.valueOf("0F0D1A");
    private static final Color DUNGEON_TINT = Color.valueOf("9A94B8"); // Escurece o chao para destacar os herois
    private static final Color DEAD_COLOR = Color.valueOf("4B4A6E");
    private static final Color BAR_BACKGROUND = Color.valueOf("2A2540");
    private static final Color HP_GREEN = Color.valueOf("5FB356");
    private static final Color HP_YELLOW = Color.valueOf("F2C94C");
    private static final Color HP_RED = Color.valueOf("C23B3B");
    private static final Color DAMAGE_TEXT_COLOR = Color.valueOf("FF4D4D");
    private static final Color HUD_BG_COLOR = Color.valueOf("181425");
    private static final Color HUD_BORDER_COLOR = Color.valueOf("3A3554");
    private static final Color HUD_TEXT_COLOR = Color.valueOf("E0DEF4");

    // Medidas e posicoes das equipes
    private static final float BAR_WIDTH = 24f;
    private static final float BAR_HEIGHT = 3f;
    private static final float ROW_SPACING = 36f;
    private static final float HERO_X = 60f;
    private static final float ENEMY_X = 244f;

    // Medidas do painel de log (HUD inferior)
    private static final float HUD_X = 14f;
    private static final float HUD_Y = 6f;
    private static final float HUD_WIDTH = 292f;
    private static final float HUD_HEIGHT = 20f;

    // Constantes de tempo (em segundos) e intensidade de animacao
    private static final float SECONDS_PER_ACTION = 0.8f;
    private static final float FLASH_DURATION = 0.15f;
    private static final float IDLE_FRAME_DURATION = 0.15f;
    private static final float DASH_DURATION = 0.25f;  // Duracao do avanco de ataque
    private static final float DASH_DISTANCE = 14f;    // Quantos pixels o atacante avanca para frente

    private final Party heroParty;
    private final Party enemyParty;
    private final List< TurnLog > history;
    private final Map< String, Integer > displayedHp = new HashMap<>();

    // Estado da reproducao dos turnos
    private int nextLogIndex = 0;
    private float actionTimer = 0f;
    private float stateTime = 0f;
    private String currentLogText = "Iniciando simulacao de batalha...";

    // Estado do efeito de piscar (Flash) no alvo atingido
    private float flashTimer = 0f;
    private String flashTargetId = null;

    // Estado do efeito de investida (Dash) no atacante
    private float dashTimer = 0f;
    private String dashAttackerId = null;

    // Estado do efeito de tremor de tela (Screen Shake) na camera
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;

    public AutoDelverGame(Party heroParty, Party enemyParty, List< TurnLog > history) {
        this.heroParty = heroParty;
        this.enemyParty = enemyParty;
        this.history = history;

        // Inicializa o mapa visual com o HP maximo de todos antes da luta comecar
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

        // Fonte para os numeros de dano subindo
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.42f);
        damageFont.setUseIntegerPositions(false);

        // Fonte para o texto narrativo na HUD inferior
        logFont = new BitmapFont();
        logFont.getData().setScale(0.38f);
        logFont.setUseIntegerPositions(false);

        loadDungeonAssets();

        // Carrega os 4 quadros (f0 a f3) da animacao de cada classe/monstro
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

        // Monta a matriz do piso misturando ladrilhos lisos (75%) e rachados (25%)
        floorGrid = new int[GRID_ROWS][GRID_COLS];
        Random rng = new Random(42L); // Semente fixa gera sempre o mesmo padrao visual
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (rng.nextFloat() < 0.75f) {
                    floorGrid[row][col] = 0;
                } else {
                    floorGrid[row][col] = 1 + rng.nextInt(7);
                }
            }
        }
    }

    private Texture loadSingleTexture(String fileName) {
        Texture tex = new Texture(Gdx.files.internal("assets/frames/" + fileName));
        loadedTextures.add(tex); // Registra na lista para limpar da memoria no dispose()
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

        // Atualiza a posicao da camera aplicando tremor (Screen Shake) se estiver ativo
        updateCameraShake(delta);

        // 1. DESENHO DO CENARIO E DOS SPRITES (SpriteBatch)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawDungeonBackground();
        drawPartySprites(heroParty, HERO_X, false);
        drawPartySprites(enemyParty, ENEMY_X, true);
        batch.end();

        // 2. DESENHO DAS FORMAS GEOMETRICAS (ShapeRenderer: HUD e Barras de HP)
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHudPanel();
        drawPartyHpBars(heroParty, HERO_X, false);
        drawPartyHpBars(enemyParty, ENEMY_X, true);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawHudBorder();
        shapeRenderer.end();

        // 3. DESENHO DOS TEXTOS POR CIMA DE TUDO (SpriteBatch)
        batch.begin();
        drawFloatingTexts();
        drawHudText();
        batch.end();
    }

    // Controla o deslocamento aleatorio da camera para criar o efeito de impacto na tela
    private void updateCameraShake(float delta) {
        // Ponto central padrao da camera (metade da largura e altura virtuais)
        float baseX = VIRTUAL_WIDTH / 2f;
        float baseY = VIRTUAL_HEIGHT / 2f;

        if (shakeTimer > 0f) {
            shakeTimer -= delta;
            // Gera um desvio aleatorio entre -shakeIntensity e +shakeIntensity
            float offsetX = (effectRng.nextFloat() * 2f - 1f) * shakeIntensity;
            float offsetY = (effectRng.nextFloat() * 2f - 1f) * shakeIntensity;
            camera.position.set(baseX + offsetX, baseY + offsetY, 0f);
        } else {
            // Quando o tempo acaba, trava a camera de volta no centro exato
            camera.position.set(baseX, baseY, 0f);
        }
        camera.update(); // Recalcula a matriz da camera com a nova posicao
    }

    private void drawDungeonBackground() {
        batch.setColor(DUNGEON_TINT);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                Texture tile = floorTiles[floorGrid[row][col]];
                batch.draw(tile, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        for (int col = 0; col < GRID_COLS; col++) {
            float x = col * TILE_SIZE;
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
        // Atualiza os cronometros das animacoes curtas de combate
        if (flashTimer > 0f) {
            flashTimer -= delta;
        }
        if (dashTimer > 0f) {
            dashTimer -= delta;
        }

        if (nextLogIndex >= history.size()) {
            return;
        }

        actionTimer += delta;
        if (actionTimer >= SECONDS_PER_ACTION) {
            actionTimer -= SECONDS_PER_ACTION;
            TurnLog log = history.get(nextLogIndex);
            nextLogIndex++;

            // 1. Atualiza HP e ativa o flash vermelho no alvo
            displayedHp.put(log.targetId(), log.targetRemainingHp());
            flashTargetId = log.targetId();
            flashTimer = FLASH_DURATION;

            // 2. Ativa o avanco (Dash) no atacante
            dashAttackerId = log.attackerId();
            dashTimer = DASH_DURATION;

            // 3. Ativa o tremor de tela (Screen Shake) se o alvo morreu ou levou dano alto (>= 12)
            if (log.targetDefeated()) {
                shakeIntensity = 3.0f; // Tremor forte para abate
                shakeTimer = 0.25f;
            } else if (log.damageDealt() >= 12) {
                shakeIntensity = 1.5f; // Tremor leve para golpe pesado
                shakeTimer = 0.12f;
            }

            // 4. Atualiza o texto narrativo na HUD
            String defeatedText = log.targetDefeated() ? " (Derrotado!)" : "";
            currentLogText = log.attackerName() + " atacou " + log.targetName()
                    + " causando " + log.damageDealt() + " de dano" + defeatedText;

            if (nextLogIndex >= history.size()) {
                boolean heroesWon = isPartyAlive(heroParty);
                currentLogText = heroesWon ? "Vitoria dos Herois! Masmorra concluida." : "Derrota! A equipe sucumbiu.";
            }

            // 5. Cria o numero de dano flutuante
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

    // Calcula quantos pixels o atacante deve ser desenhado a frente durante o Dash
    private float getDashOffsetX(String combatantId, boolean isEnemyTeam) {
        if (dashTimer <= 0f || !combatantId.equals(dashAttackerId)) {
            return 0f;
        }
        // Progresso vai de 0.0 (inicio do golpe) ate 1.0 (fim do movimento)
        float progress = 1f - (dashTimer / DASH_DURATION);
        // Math.sin(progresso * PI) faz uma curva suave de ida (0 -> 1) e volta (1 -> 0)
        float offset = (float) Math.sin(progress * Math.PI) * DASH_DISTANCE;
        // Herois avancam para a direita (+offset); inimigos avancam para a esquerda (-offset)
        return isEnemyTeam ? -offset : offset;
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

    private void drawPartySprites(Party party, float baseX, boolean flipX) {
        List< Combatant > members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = (VIRTUAL_HEIGHT + 12f) / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * ROW_SPACING;
            int hp = displayedHp.getOrDefault(member.getId(), member.getMaxHp());

            // Soma a posicao X base com o deslocamento do Dash (se este combatente estiver atacando)
            float x = baseX + getDashOffsetX(member.getId(), flipX);

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

    private void drawPartyHpBars(Party party, float baseX, boolean isEnemyTeam) {
        List< Combatant > members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = (VIRTUAL_HEIGHT + 12f) / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * ROW_SPACING;
            int hp = displayedHp.getOrDefault(member.getId(), member.getMaxHp());

            // A barra de vida acompanha o movimento horizontal do personagem durante o Dash
            float x = baseX + getDashOffsetX(member.getId(), isEnemyTeam);

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