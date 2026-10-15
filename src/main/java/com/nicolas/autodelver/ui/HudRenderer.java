package com.nicolas.autodelver.ui;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.ui.ReplayController.FloatingText;

/**
 * Responsabilidade Unica: Renderizar todos os elementos de Interface de Usuario (HUD):
 * barras de vida, caixa de log narrativo inferior, barra de status de controles no topo
 * e os textos de dano flutuantes.
 */
public class HudRenderer {

    // Cores da interface e barras de HP
    private static final Color BAR_BACKGROUND = Color.valueOf("2A2540");
    private static final Color HP_GREEN = Color.valueOf("5FB356");
    private static final Color HP_YELLOW = Color.valueOf("F2C94C");
    private static final Color HP_RED = Color.valueOf("C23B3B");
    private static final Color HUD_BG_COLOR = Color.valueOf("181425");
    private static final Color HUD_BORDER_COLOR = Color.valueOf("3A3554");
    private static final Color HUD_TEXT_COLOR = Color.valueOf("E0DEF4");
    private static final Color STATUS_TEXT_COLOR = Color.valueOf("F2C94C");

    // Dimensoes das barras de HP e do painel inferior
    private static final float BAR_WIDTH = 24f;
    private static final float BAR_HEIGHT = 3f;
    private static final float HUD_X = 14f;
    private static final float HUD_Y = 6f;
    private static final float HUD_WIDTH = 292f;
    private static final float HUD_HEIGHT = 20f;

    // Desenha todas as formas geometricas da interface (fundo da HUD, borda e barras de HP)
    public void drawShapes(ShapeRenderer shapeRenderer, BattleAssets assets, ReplayController controller) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHudPanel(shapeRenderer);
        drawPartyHpBars(shapeRenderer, assets, controller, controller.getHeroParty(), DungeonRenderer.HERO_X, false);
        drawPartyHpBars(shapeRenderer, assets, controller, controller.getEnemyParty(), DungeonRenderer.ENEMY_X, true);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawHudBorder(shapeRenderer);
        shapeRenderer.end();
    }

    // Desenha todos os elementos de texto por cima do jogo
    public void drawTexts(SpriteBatch batch, BattleAssets assets, ReplayController controller) {
        drawFloatingTexts(batch, assets.getDamageFont(), controller.getFloatingTexts());
        drawHudText(batch, assets.getLogFont(), controller.getCurrentLogText());
        drawControlStatus(batch, assets.getStatusFont(), controller);
    }

    private void drawHudPanel(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(HUD_BG_COLOR);
        shapeRenderer.rect(HUD_X, HUD_Y, HUD_WIDTH, HUD_HEIGHT);
    }

    private void drawHudBorder(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(HUD_BORDER_COLOR);
        shapeRenderer.rect(HUD_X, HUD_Y, HUD_WIDTH, HUD_HEIGHT);
    }

    private void drawPartyHpBars(
            ShapeRenderer shapeRenderer,
            BattleAssets assets,
            ReplayController controller,
            Party party,
            float baseX,
            boolean isEnemyTeam
    ) {
        List< Combatant > members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * DungeonRenderer.ROW_SPACING;
        float topY = (DungeonRenderer.VIRTUAL_HEIGHT + 12f) / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * DungeonRenderer.ROW_SPACING;
            int hp = controller.getDisplayedHp(member);

            float x = baseX + controller.getDashOffsetX(member.getId(), isEnemyTeam);

            Animation< Texture > anim = assets.getAnimationForCombatant(member);
            Texture currentFrame = anim.getKeyFrames()[0];

            float barX = x + (currentFrame.getWidth() / 2f) - (BAR_WIDTH / 2f);
            float barY = y + currentFrame.getHeight() + 3f;

            drawHpBar(shapeRenderer, barX, barY, hp, member.getMaxHp());
        }
    }

    private void drawHpBar(ShapeRenderer shapeRenderer, float x, float y, int hp, int maxHp) {
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

    private void drawFloatingTexts(SpriteBatch batch, BitmapFont damageFont, List< FloatingText > floatingTexts) {
        for (FloatingText ft : floatingTexts) {
            Color c = ft.getColor();
            damageFont.setColor(c.r, c.g, c.b, ft.getAlpha());
            damageFont.draw(batch, ft.getText(), ft.getX(), ft.getY());
        }
        damageFont.setColor(Color.WHITE);
    }

    private void drawHudText(SpriteBatch batch, BitmapFont logFont, String currentLogText) {
        logFont.setColor(HUD_TEXT_COLOR);
        logFont.draw(batch, currentLogText, HUD_X + 8f, HUD_Y + 14f);
    }

    private void drawControlStatus(SpriteBatch batch, BitmapFont statusFont, ReplayController controller) {
        String stateLabel = controller.isPaused() ? "PAUSADO" : "RODANDO";
        String soundLabel = controller.isMuted() ? "MUDO" : "SOM";
        int speedInt = (int) controller.getSpeedMultiplier();
        String statusText = "[" + speedInt + "x] [" + stateLabel + "] [" + soundLabel + "]  Espaco: Pausa | V: Vel | M: Som | R: Reset";

        statusFont.setColor(STATUS_TEXT_COLOR);
        statusFont.draw(batch, statusText, 10f, DungeonRenderer.VIRTUAL_HEIGHT - 5f);
    }
}