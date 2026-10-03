package com.nicolas.autodelver.ui;

import java.util.List;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import com.nicolas.autodelver.domain.Hero;
import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.domain.TurnLog;
import com.nicolas.autodelver.engine.BattleEngine;
import com.nicolas.autodelver.engine.EnemyFactory;
import com.nicolas.autodelver.infrastructure.persistence.HeroDaoJdbc;

// Ponto de entrada da versao grafica: monta as equipes, simula a luta e abre a janela.
public class DesktopLauncher {

    public static void main(String[] args) {
        // 1. Heroes vindos do banco (precisa da variavel AUTODELVER_DB_PASSWORD definida).
        Party heroParty = new Party("Comitiva da Luz");
        try {
            List<Hero> heroes = new HeroDaoJdbc().findAll();
            if (heroes.isEmpty()) {
                System.out.println("Nenhum heroi encontrado na tabela 'heroes'.");
                return;
            }
            for (Hero hero : heroes) {
                heroParty.addMember(hero);
            }
        } catch (RuntimeException e) {
            System.err.println("Falha ao carregar herois: " + e.getMessage());
            return;
        }

        // 2. Inimigos sorteados.
        Party enemyParty = new Party("Horda Sombria");
        EnemyFactory factory = new EnemyFactory();
        for (int i = 0; i < 3; i++) {
            enemyParty.addMember(factory.createRandomEnemy());
        }

        // 3. Simula a luta INTEIRA antes de abrir a janela; a tela so reproduz o historico.
        BattleEngine engine = new BattleEngine();
        try {
            engine.simulateEncounter(heroParty, enemyParty);
        } catch (IllegalStateException e) {
            System.err.println("Combate interrompido: " + e.getMessage());
            return;
        }
        // Copia o historico (o motor devolve uma visao interna da lista dele).
        List<TurnLog> history = List.copyOf(engine.getCombatHistory());

        // 4. Janela 960x540 = 3x o mundo virtual de 320x180.
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Auto-Delver");
        config.setWindowedMode(960, 540);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new AutoDelverGame(heroParty, enemyParty, history), config);
    }
}