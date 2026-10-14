package com.nicolas.autodelver.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.domain.TurnLog;

/**
 * Responsabilidade Unica: Controlar o estado da reproducao da batalha (linha do tempo,
 * leitura de atalhos do teclado, HP atual exibido na tela e temporizadores de efeitos).
 *
 * Esta classe e desacoplada do OpenGL (nao desenha nada diretamente).
 */
public class ReplayController {

    private static final float SECONDS_PER_ACTION = 0.8f;
    private static final float FLASH_DURATION = 0.15f;
    private static final float DASH_DURATION = 0.25f;
    private static final float DASH_DISTANCE = 14f;
    private static final Color DAMAGE_TEXT_COLOR = Color.valueOf("FF4D4D");

    private final Party heroParty;
    private final Party enemyParty;
    private final List< TurnLog > history;

    // Estado de vida exibido gradualmente na interface (ID do combatente -> HP na tela)
    private final Map< String, Integer > displayedHp = new HashMap< >();
    private final List< FloatingText > floatingTexts = new ArrayList< >();

    // Marcadores de tempo e progresso do replay
    private int nextLogIndex = 0;
    private float actionTimer = 0f;
    private float stateTime = 0f;
    private String currentLogText = "Iniciando simulacao de batalha...";

    // Controles de reproducao (Pausa e Velocidade)
    private boolean paused = false;
    private float speedMultiplier = 1.0f;

    // Efeitos temporarios de combate (Flash, Dash e Screen Shake)
    private float flashTimer = 0f;
    private String flashTargetId = null;

    private float dashTimer = 0f;
    private String dashAttackerId = null;

    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;

    public ReplayController(Party heroParty, Party enemyParty, List< TurnLog > history) {
        this.heroParty = heroParty;
        this.enemyParty = enemyParty;
        this.history = history;
        resetDisplayedHp();
    }

    private void resetDisplayedHp() {
        displayedHp.clear();
        for (Combatant c : heroParty.getAllMembers()) {
            displayedHp.put(c.getId(), c.getMaxHp());
        }
        for (Combatant c : enemyParty.getAllMembers()) {
            displayedHp.put(c.getId(), c.getMaxHp());
        }
    }

    // Reinicia a apresentacao do combate para o instante zero
    public void restartReplay() {
        resetDisplayedHp();
        floatingTexts.clear();
        nextLogIndex = 0;
        actionTimer = 0f;
        flashTimer = 0f;
        flashTargetId = null;
        dashTimer = 0f;
        dashAttackerId = null;
        shakeTimer = 0f;
        paused = false;
        currentLogText = "Replay reiniciado! Iniciando combate...";
    }

    // Processa os atalhos de teclado a cada frame
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            paused = !paused;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            speedMultiplier = 1.0f;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            speedMultiplier = 2.0f;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            speedMultiplier = 4.0f;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            if (speedMultiplier == 1.0f) {
                speedMultiplier = 2.0f;
            } else if (speedMultiplier == 2.0f) {
                speedMultiplier = 4.0f;
            } else {
                speedMultiplier = 1.0f;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartReplay();
        }
    }

    // Atualiza todos os relogios da simulacao baseando-se no delta time do frame
    public void update(float rawDelta) {
        // O relogio de respiracao (stateTime) nunca pausa para manter os sprites vivos
        stateTime += rawDelta;

        // O relogio de combate respeita o pause e o multiplicador de velocidade (1x, 2x, 4x)
        float combatDelta = paused ? 0f : (rawDelta * speedMultiplier);

        if (shakeTimer > 0f) {
            shakeTimer -= combatDelta;
        }
        if (flashTimer > 0f) {
            flashTimer -= combatDelta;
        }
        if (dashTimer > 0f) {
            dashTimer -= combatDelta;
        }

        updateFloatingTexts(combatDelta);

        if (nextLogIndex >= history.size()) {
            return;
        }

        actionTimer += combatDelta;
        if (actionTimer >= SECONDS_PER_ACTION) {
            actionTimer -= SECONDS_PER_ACTION;
            TurnLog log = history.get(nextLogIndex);
            nextLogIndex++;

            // 1. Atualiza o HP visual e ativa o flash vermelho no alvo
            displayedHp.put(log.targetId(), log.targetRemainingHp());
            flashTargetId = log.targetId();
            flashTimer = FLASH_DURATION;

            // 2. Inicia o avanco (Dash) no atacante
            dashAttackerId = log.attackerId();
            dashTimer = DASH_DURATION;

            // 3. Dispara o tremor de tela (Screen Shake) se houve abate ou golpe forte
            if (log.targetDefeated()) {
                shakeIntensity = 3.0f;
                shakeTimer = 0.25f;
            } else if (log.damageDealt() >= 12) {
                shakeIntensity = 1.5f;
                shakeTimer = 0.12f;
            }

            // 4. Formata a mensagem da HUD inferior
            String defeatedText = log.targetDefeated() ? " (Derrotado!)" : "";
            currentLogText = log.attackerName() + " atacou " + log.targetName()
                    + " causando " + log.damageDealt() + " de dano" + defeatedText;

            if (nextLogIndex >= history.size()) {
                boolean heroesWon = isPartyAlive(heroParty);
                currentLogText = heroesWon
                        ? "Vitoria dos Herois! (Aperte R para rever)"
                        : "Derrota! A equipe sucumbiu. (Aperte R para rever)";
            }

            // 5. Instancia o numero de dano flutuante sobre a cabeca do alvo
            if (log.damageDealt() > 0) {
                float[] targetPos = DungeonRenderer.getCombatantCoordinates(log.targetId(), heroParty, enemyParty);
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

    private boolean isPartyAlive(Party party) {
        for (Combatant c : party.getAllMembers()) {
            if (displayedHp.getOrDefault(c.getId(), 0) > 0) {
                return true;
            }
        }
        return false;
    }

    // Calcula o deslocamento horizontal (em pixels) do atacante durante a investida (Dash)
    public float getDashOffsetX(String combatantId, boolean isEnemyTeam) {
        if (dashTimer <= 0f || !combatantId.equals(dashAttackerId)) {
            return 0f;
        }
        float progress = 1f - (dashTimer / DASH_DURATION);
        float offset = (float) Math.sin(progress * Math.PI) * DASH_DISTANCE;
        return isEnemyTeam ? -offset : offset;
    }

    public boolean isFlashing(String combatantId) {
        return flashTimer > 0f && combatantId.equals(flashTargetId);
    }

    public int getDisplayedHp(Combatant c) {
        return displayedHp.getOrDefault(c.getId(), c.getMaxHp());
    }

    public Party getHeroParty() {
        return heroParty;
    }

    public Party getEnemyParty() {
        return enemyParty;
    }

    public float getStateTime() {
        return stateTime;
    }

    public String getCurrentLogText() {
        return currentLogText;
    }

    public boolean isPaused() {
        return paused;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public boolean isShaking() {
        return shakeTimer > 0f;
    }

    public float getShakeIntensity() {
        return shakeIntensity;
    }

    public List< FloatingText > getFloatingTexts() {
        return floatingTexts;
    }

    // Estrutura de dados que representa um numero de dano subindo na tela
    public static class FloatingText {
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

        public String getText() {
            return text;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getAlpha() {
            return Math.max(0f, remainingTime / totalDuration);
        }

        public Color getColor() {
            return color;
        }
    }
}