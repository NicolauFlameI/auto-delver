package com.nicolas.autodelver;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Enemy;
import com.nicolas.autodelver.domain.Hero;
import com.nicolas.autodelver.domain.TurnLog;
import com.nicolas.autodelver.engine.BattleEngine;
import com.nicolas.autodelver.engine.EnemyFactory;

// Ponto de entrada da aplicacao (CLI MVP).
// Responsavel por instanciar as entidades, disparar a simulacao e formatar a saida textual.
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        AUTO-DELVER - SIMULADOR DE COMBATE        ");
        System.out.println("==================================================\n");

        // 1. Criacao manual do Heroi do jogador.
        Hero heroi = new Hero("Valerius o Paladino", 100, 18);

        // 2. Criacao procedimental do Inimigo atraves da Fabrica.
        EnemyFactory fabricaInimigos = new EnemyFactory();
        Enemy monstro = fabricaInimigos.createRandomEnemy();

        System.out.println("Encontro Gerado na Masmorra:");
        System.out.printf(" - Heroi: %s [HP: %d/%d | ATK: %d]%n",
                heroi.getName(), heroi.getCurrentHp(), heroi.getMaxHp(), heroi.getBaseAttack());
        System.out.printf(" - Ameaca: %s [HP: %d/%d | ATK: %d]%n%n",
                monstro.getName(), monstro.getCurrentHp(), monstro.getMaxHp(), monstro.getBaseAttack());

        // 3. Inicializacao do motor de combate isolado.
        BattleEngine motor = new BattleEngine();

        System.out.println("--- Inicio do Confronto ---");
        Combatant vencedor = motor.simulateEncounter(heroi, monstro);

        // 4. Leitura e exibicao do historico desacoplado (TurnLog).
        for (TurnLog log : motor.getCombatHistory()) {
            System.out.printf("[Turno %02d] %s desfere golpe em %s causando %d de dano! (HP restante: %d)%n",
                    log.turnNumber(),
                    log.attackerName(),
                    log.targetName(),
                    log.damageDealt(),
                    log.targetRemainingHp()
            );

            if (log.targetDefeated()) {
                System.out.printf("  -> %s sucumbiu aos ferimentos!%n", log.targetName());
            }
        }

        // 5. Exibicao do resultado final.
        System.out.println("\n==================================================");
        System.out.printf("VENCEDOR DO COMBATE: %s (HP restante: %d)%n", vencedor.getName(), vencedor.getCurrentHp());
        System.out.println("==================================================");
    }
}