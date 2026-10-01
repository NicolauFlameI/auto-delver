package com.nicolas.autodelver;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Enemy;
import com.nicolas.autodelver.domain.Hero;
import com.nicolas.autodelver.domain.TurnLog;
import com.nicolas.autodelver.engine.BattleEngine;

// Ponto de entrada da aplicacao (CLI MVP).
// Responsavel por instanciar as entidades, disparar a simulacao e formatar a saida textual.
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        AUTO-DELVER - SIMULADOR DE COMBATE        ");
        System.out.println("==================================================\n");

        // 1. Instanciacao dos combatentes com estado valido.
        Hero heroi = new Hero("Valerius o Paladino", 100, 18);
        Enemy monstro = new Enemy("Goblin Saqueador", 45, 12);

        System.out.printf("Competidores Inicializados:%n");
        System.out.printf(" - %s [HP: %d/%d | ATK: %d]%n", heroi.getName(), heroi.getCurrentHp(), heroi.getMaxHp(), heroi.getBaseAttack());
        System.out.printf(" - %s [HP: %d/%d | ATK: %d]%n%n", monstro.getName(), monstro.getCurrentHp(), monstro.getMaxHp(), monstro.getBaseAttack());

        // 2. Inicializacao do motor de combate isolado.
        BattleEngine motor = new BattleEngine();

        System.out.println("--- Inicio do Confronto ---");
        Combatant vencedor = motor.simulateEncounter(heroi, monstro);

        // 3. Leitura e exibicao do historico desacoplado (TurnLog).
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

        // 4. Exibicao do resultado final.
        System.out.println("\n==================================================");
        System.out.printf("VENCEDOR DO COMBATE: %s (HP restante: %d)%n", vencedor.getName(), vencedor.getCurrentHp());
        System.out.println("==================================================");
    }
}