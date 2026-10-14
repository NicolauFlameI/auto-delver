package com.nicolas.autodelver.ui;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.domain.TurnLog;

/**
 * Responsabilidade Unica: Orquestrar o ciclo de vida do LibGDX (create, render, resize, dispose),
 * gerenciar a camera/viewport e coordenar a interacao entre os subsistemas de UI.
 */
public class AutoDelverGame extends ApplicationAdapter {

    private static final Color BACKGROUND = Color.valueOf("0F0D1A");

    // Ferramentas graficas base do LibGDX
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private final Random shakeRng = new Random();

    // Subsistemas especializados da camada de UI (SRP)
    private BattleAssets assets;
    private final ReplayController replayController;
    private final DungeonRenderer dungeonRenderer;
    private final HudRenderer hudRenderer;

    public AutoDelverGame(Party heroParty, Party enemyParty, List< TurnLog > history) {
        if (heroParty == null || enemyParty == null || history == null) {
            throw new IllegalArgumentException("Equipes e historico nao podem ser nulos.");
        }
        // Os controladores e renderizadores que nao dependem de OpenGL na criacao ja sao iniciados aqui
        this.replayController = new ReplayController(heroParty, enemyParty, history);
        this.dungeonRenderer = new DungeonRenderer();
        this.hudRenderer = new HudRenderer();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(
                DungeonRenderer.VIRTUAL_WIDTH,
                DungeonRenderer.VIRTUAL_HEIGHT,
                camera
        );
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Carrega texturas e fontes na GPU (exige contexto OpenGL ativo)
        assets = new BattleAssets();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // 1. Entrada e atualizacao de estado logico
        replayController.handleInput();
        replayController.update(Gdx.graphics.getDeltaTime());

        // 2. Preparacao da tela e camera
        ScreenUtils.clear(BACKGROUND);
        viewport.apply();
        updateCameraPosition();

        // 3. Renderizacao do mundo (Piso, Paredes e Sprites)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        dungeonRenderer.drawBackground(batch, assets);
        dungeonRenderer.drawParties(batch, assets, replayController);
        batch.end();

        // 4. Renderizacao das formas da interface (Painel da HUD e Barras de HP)
        shapeRenderer.setProjectionMatrix(camera.combined);
        hudRenderer.drawShapes(shapeRenderer, assets, replayController);

        // 5. Renderizacao dos textos da interface (Danos flutuantes, Log e Status)
        batch.begin();
        hudRenderer.drawTexts(batch, assets, replayController);
        batch.end();
    }

    // Aplica o deslocamento de tremor de tela (Screen Shake) na camera quando ativo
    private void updateCameraPosition() {
        float baseX = DungeonRenderer.VIRTUAL_WIDTH / 2f;
        float baseY = DungeonRenderer.VIRTUAL_HEIGHT / 2f;

        if (replayController.isShaking()) {
            float intensity = replayController.getShakeIntensity();
            float offsetX = (shakeRng.nextFloat() * 2f - 1f) * intensity;
            float offsetY = (shakeRng.nextFloat() * 2f - 1f) * intensity;
            camera.position.set(baseX + offsetX, baseY + offsetY, 0f);
        } else {
            camera.position.set(baseX, baseY, 0f);
        }
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (assets != null) {
            assets.dispose();
        }
    }
}