package com.nicolas.autodelver.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.domain.TurnLog;

// Tela de batalha. Nao conhece regras de combate: apenas REPRODUZ o historico que o
// BattleEngine ja calculou, mostrando as barras de vida descendo golpe a golpe.
public class AutoDelverGame extends ApplicationAdapter {

    // Tamanho do "mundo" do jogo, em unidades virtuais. O libGDX amplia isso para a janela real.
    private static final float VIRTUAL_WIDTH = 320f;
    private static final float VIRTUAL_HEIGHT = 180f;

    // Paleta (tons iniciais; vamos ajusta-la as cores dos sprites no passo 3b).
    private static final Color BACKGROUND = Color.valueOf("0F0D1A");
    private static final Color HERO_COLOR = Color.valueOf("3B82C4");
    private static final Color ENEMY_COLOR = Color.valueOf("C23B3B");
    private static final Color DEAD_COLOR = Color.valueOf("4B4A6E");
    private static final Color BAR_BACKGROUND = Color.valueOf("2A2540");
    private static final Color HP_GREEN = Color.valueOf("5FB356");
    private static final Color HP_YELLOW = Color.valueOf("F2C94C");
    private static final Color HP_RED = Color.valueOf("C23B3B");

    // Medidas do desenho, todas em unidades virtuais.
    private static final float SPRITE_SIZE = 16f;
    private static final float BAR_WIDTH = 24f;
    private static final float BAR_HEIGHT = 3f;
    private static final float ROW_SPACING = 40f;
    private static final float HERO_X = 60f;
    private static final float ENEMY_X = 244f;

    // Tempos da reproducao, em segundos.
    private static final float SECONDS_PER_ACTION = 0.8f;
    private static final float FLASH_DURATION = 0.15f;

    private final Party heroParty;
    private final Party enemyParty;
    private final List<TurnLog> history;

    // HP que a TELA mostra (id do combatente -> HP). Comeca no maximo e cai a cada TurnLog.
    // Nao usamos getCurrentHp() porque o dominio ja terminou a luta e mostraria o HP final.
    private final Map<String, Integer> displayedHp = new HashMap<>();

    // Estado da reproducao.
    private int nextLogIndex = 0;       // proximo golpe do historico a ser mostrado
    private float actionTimer = 0f;     // tempo acumulado desde o ultimo golpe mostrado
    private float flashTimer = 0f;      // tempo restante do flash branco no alvo
    private String flashTargetId = null;

    // Objetos do libGDX (criados em create(), quando o OpenGL ja existe).
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;

    public AutoDelverGame(Party heroParty, Party enemyParty, List<TurnLog> history) {
        if (heroParty == null || enemyParty == null || history == null) {
            throw new IllegalArgumentException("Equipes e historico nao podem ser nulos.");
        }
        this.heroParty = heroParty;
        this.enemyParty = enemyParty;
        this.history = history;

        // Todos comecam exibindo a vida cheia.
        for (Combatant c : heroParty.getAllMembers()) {
            displayedHp.put(c.getId(), c.getMaxHp());
        }
        for (Combatant c : enemyParty.getAllMembers()) {
            displayedHp.put(c.getId(), c.getMaxHp());
        }
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        // FitViewport mantem a proporcao 16:9; sobra de espaco vira faixa preta nas bordas.
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        shapeRenderer = new ShapeRenderer();
    }

    // Chamado quando a janela muda de tamanho: o viewport recalcula a escala.
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true = centraliza a camera
    }

    @Override
    public void render() {
        updateReplay(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(BACKGROUND);
        viewport.apply();
        // Faz o desenho usar as coordenadas virtuais (320x180) em vez de pixels da janela.
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawParty(heroParty, HERO_X, HERO_COLOR);
        drawParty(enemyParty, ENEMY_X, ENEMY_COLOR);
        shapeRenderer.end();
    }

    // Avanca o historico: a cada SECONDS_PER_ACTION, "acontece" o proximo golpe.
    private void updateReplay(float delta) {
        if (flashTimer > 0f) {
            flashTimer -= delta;
        }
        if (nextLogIndex >= history.size()) {
            return; // historico acabou; a tela fica parada no estado final
        }

        actionTimer += delta;
        if (actionTimer >= SECONDS_PER_ACTION) {
            // Subtrai (em vez de zerar) para nao perder o resto de tempo e manter o ritmo estavel.
            actionTimer -= SECONDS_PER_ACTION;

            TurnLog log = history.get(nextLogIndex);
            nextLogIndex++;

            displayedHp.put(log.targetId(), log.targetRemainingHp());
            flashTargetId = log.targetId();
            flashTimer = FLASH_DURATION;
        }
    }

    // Desenha uma coluna de combatentes (heroes a esquerda ou inimigos a direita).
    private void drawParty(Party party, float x, Color teamColor) {
        List<Combatant> members = party.getAllMembers(); // vivos E mortos: os mortos continuam na tela
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = VIRTUAL_HEIGHT / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * ROW_SPACING;

            int hp = displayedHp.getOrDefault(member.getId(), member.getMaxHp());

            // Cor do corpo: cinza se morto, branco no flash de dano, cor do time no resto.
            Color body = teamColor;
            if (hp <= 0) {
                body = DEAD_COLOR;
            } else if (flashTimer > 0f && member.getId().equals(flashTargetId)) {
                body = Color.WHITE;
            }
            shapeRenderer.setColor(body);
            shapeRenderer.rect(x, y, SPRITE_SIZE, SPRITE_SIZE);

            drawHpBar(x + SPRITE_SIZE / 2f - BAR_WIDTH / 2f, y + SPRITE_SIZE + 3f,
                    hp, member.getMaxHp());
        }
    }

    // Barra de HP: fundo escuro + preenchimento proporcional, colorido pela faixa de vida.
    private void drawHpBar(float x, float y, int hp, int maxHp) {
        shapeRenderer.setColor(BAR_BACKGROUND);
        shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

        if (hp <= 0) {
            return; // sem vida, so o fundo
        }
        float ratio = (float) hp / maxHp;
        // Arredonda para pixel inteiro (visual 8-bit) e garante ao menos 1 pixel enquanto vivo.
        float fillWidth = Math.max(1f, Math.round(BAR_WIDTH * ratio));

        shapeRenderer.setColor(hpColor(ratio));
        shapeRenderer.rect(x, y, fillWidth, BAR_HEIGHT);
    }

    // Verde acima de 50%, amarelo ate 25%, vermelho abaixo disso.
    private Color hpColor(float ratio) {
        if (ratio > 0.5f) {
            return HP_GREEN;
        }
        if (ratio > 0.25f) {
            return HP_YELLOW;
        }
        return HP_RED;
    }

    @Override
    public void dispose() {
        // ShapeRenderer usa memoria da GPU; precisa ser liberado manualmente.
        shapeRenderer.dispose();
    }
}