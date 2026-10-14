package com.nicolas.autodelver.ui;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Party;

/**
 * Responsabilidade Unica: Renderizar o ambiente fisico da masmorra (piso, paredes,
 * estandartes) e posicionar/desenhar os sprites dos combatentes na arena.
 */
public class DungeonRenderer {

    // Dimensoes virtuais e coordenadas da arena compartilhadas com a UI
    public static final float VIRTUAL_WIDTH = 320f;
    public static final float VIRTUAL_HEIGHT = 180f;
    public static final float HERO_X = 60f;
    public static final float ENEMY_X = 244f;
    public static final float ROW_SPACING = 36f;

    private static final int TILE_SIZE = 16;
    private static final int GRID_COLS = 20; // 320 / 16
    private static final int GRID_ROWS = 12;

    private static final Color DUNGEON_TINT = Color.valueOf("9A94B8");
    private static final Color DEAD_COLOR = Color.valueOf("4B4A6E");

    private final int[][] floorGrid;

    public DungeonRenderer() {
        // Sorteia a matriz do chao na inicializacao (75% piso liso, 25% pisos rachados)
        floorGrid = new int[GRID_ROWS][GRID_COLS];
        Random rng = new Random(42L); // Semente fixa para manter o padrao visual consistente
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

    // Desenha os ladrilhos de pedra, a parede do fundo e os estandartes vermelhos
    public void drawBackground(SpriteBatch batch, BattleAssets assets) {
        batch.setColor(DUNGEON_TINT);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                Texture tile = assets.getFloorTile(floorGrid[row][col]);
                batch.draw(tile, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        for (int col = 0; col < GRID_COLS; col++) {
            float x = col * TILE_SIZE;
            if (col == 4 || col == 9 || col == 10 || col == 15) {
                batch.draw(assets.getWallBannerRed(), x, 9 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            } else {
                batch.draw(assets.getWallMid(), x, 9 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
            batch.draw(assets.getWallTopMid(), x, 10 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        batch.setColor(Color.WHITE);
    }

    // Desenha ambas as equipes (herois virados para a direita, inimigos espelhados para a esquerda)
    public void drawParties(SpriteBatch batch, BattleAssets assets, ReplayController controller) {
        drawPartySprites(batch, assets, controller, controller.getHeroParty(), HERO_X, false);
        drawPartySprites(batch, assets, controller, controller.getEnemyParty(), ENEMY_X, true);
    }

    private void drawPartySprites(
            SpriteBatch batch,
            BattleAssets assets,
            ReplayController controller,
            Party party,
            float baseX,
            boolean flipX
    ) {
        List< Combatant > members = party.getAllMembers();
        float totalHeight = (members.size() - 1) * ROW_SPACING;
        float topY = (VIRTUAL_HEIGHT + 12f) / 2f + totalHeight / 2f;

        for (int i = 0; i < members.size(); i++) {
            Combatant member = members.get(i);
            float y = topY - i * ROW_SPACING;
            int hp = controller.getDisplayedHp(member);

            // Aplica o avanco horizontal (Dash) caso este combatente esteja atacando
            float x = baseX + controller.getDashOffsetX(member.getId(), flipX);

            if (hp <= 0) {
                batch.setColor(DEAD_COLOR);
            } else if (controller.isFlashing(member.getId())) {
                batch.setColor(Color.RED);
            } else {
                batch.setColor(Color.WHITE);
            }

            Animation< Texture > anim = assets.getAnimationForCombatant(member);
            Texture currentFrame = (hp <= 0)
                    ? anim.getKeyFrames()[0]
                    : anim.getKeyFrame(controller.getStateTime(), true);

            float w = currentFrame.getWidth();
            float h = currentFrame.getHeight();

            batch.draw(currentFrame, x, y, w, h, 0, 0, (int) w, (int) h, flipX, false);
        }
        batch.setColor(Color.WHITE);
    }

    // Localiza a coordenada (X, Y) de um combatente especifico pelo seu ID
    public static float[] getCombatantCoordinates(String id, Party heroParty, Party enemyParty) {
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
}