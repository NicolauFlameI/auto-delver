package com.nicolas.autodelver.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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

    // Texturas para cada arquetipo
    private Texture paladinoTexture;
    private Texture arqueiraTexture;
    private Texture esqueletoTexture;
    private Texture goblinTexture;
    private Texture orcTexture;

    // Tamanho do "mundo" do jogo
    private static final float VIRTUAL_WIDTH = 320f;
    private static final float VIRTUAL_HEIGHT = 180f;

    // Paleta
    private static final Color BACKGROUND = Color.valueOf("0F0D1A");
    private static final Color DEAD_COLOR = Color.valueOf("4B4A6E");
    private static final Color BAR_BACKGROUND = Color.valueOf("2A2540");
    private static final Color HP_GREEN = Color.valueOf("5FB356");
    private static final Color HP_YELLOW = Color.valueOf("F2C94C");
    private static final Color HP_RED = Color.valueOf("C23B3B");

    // Medidas do desenho
    private static final float SPRITE_SIZE = 16f;
    private static final float BAR_WIDTH = 24f;
    private static final float BAR_HEIGHT = 3f;
    private static final float ROW_SPACING = 40f;
    private static final float HERO_X = 60f;
    private static final float ENEMY_X = 244f;

    // Tempos da reproducao
    private static final float SECONDS_PER_ACTION = 0.8f;
    private static final float FLASH_DURATION = 0.15f;

    private final Party heroParty;
    private final Party enemyParty;
    private final List<TurnLog> history;
    private final Map<String, Integer> displayedHp = new HashMap<>();


    private int nextLogIndex = 0;
    private float actionTimer = 0f;
    private float flashTimer = 0f;
    private String flashTargetId = null;

    public AutoDelverGame(
            Party heroParty,
            Party enemyParty,
            List<TurnLog> history
    ) {
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

        // Carrega as texturas.
        paladinoTexture = new Texture(Gdx.files.internal("assets/frames/knight_m_idle_anim_f0.png"));
        arqueiraTexture = new Texture(Gdx.files.internal("assets/frames/elf_m_idle_anim_f0.png"));
        esqueletoTexture = new Texture(Gdx.files.internal("assets/frames/skelet_idle_anim_f0.png"));
        goblinTexture = new Texture(Gdx.files.internal("assets/frames/goblin_idle_anim_f0.png"));
        orcTexture = new Texture(Gdx.files.internal("assets/frames/orc_warrior_idle_anim_f0.png"));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        updateReplay(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(BACKGROUND);
        viewport.apply();

        // 1. FASE DE DESENHO DAS IMAGENS (SPRITES)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawPartySprites(heroParty, HERO_X, false);
        drawPartySprites(enemyParty, ENEMY_X, true); // true = espelha a imagem para olhar para a esquerda
        batch.end();

        // 2. FASE DE DESENHO DAS FORMAS GEOMETRICAS (BARRAS DE HP)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawPartyHpBars(heroParty, HERO_X);
        drawPartyHpBars(enemyParty, ENEMY_X);
        shapeRenderer.end();
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
        }
    }

    // Mapeamento visual das classes do Dominio
    private Texture getTextureForCombatant(Combatant c) {
        String name = c.getName().toLowerCase();
        if (name.contains("paladino")) return paladinoTexture;
        if (name.contains("arqueira")) return arqueiraTexture;
        if (name.contains("esqueleto")) return esqueletoTexture;
        if (name.contains("goblin")) return goblinTexture;
        if (name.contains("orc")) return orcTexture;

        return paladinoTexture; // Textura de fallback
    }

    private void drawPartySprites(Party party, float x, boolean flipX) {
        List <Combatant> members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = VIRTUAL_HEIGHT / 2f + totalHeight / 2f;

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

            Texture tex = getTextureForCombatant(member);
            float w = tex.getWidth();
            float h = tex.getHeight();

            // Desenha com as dimensoes reais do arquivo PNG (sem distorcao)
            batch.draw(tex, x, y, w, h, 0, 0, (int) w, (int) h, flipX, false);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawPartyHpBars(Party party, float x) {
        List <Combatant> members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = VIRTUAL_HEIGHT / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * ROW_SPACING;
            int hp = displayedHp.getOrDefault(member.getId(), member.getMaxHp());

            Texture tex = getTextureForCombatant(member);
            float barX = x + (tex.getWidth() / 2f) - (BAR_WIDTH / 2f);
            float barY = y + tex.getHeight() + 3f;

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
        paladinoTexture.dispose();
        arqueiraTexture.dispose();
        esqueletoTexture.dispose();
        goblinTexture.dispose();
        orcTexture.dispose();
    }
}