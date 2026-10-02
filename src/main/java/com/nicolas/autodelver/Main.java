package com.nicolas.autodelver;

import java.util.List;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Hero;
import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.domain.TurnLog;
import com.nicolas.autodelver.engine.BattleEngine;
import com.nicolas.autodelver.engine.EnemyFactory;
import com.nicolas.autodelver.infrastructure.persistence.HeroDao;
import com.nicolas.autodelver.infrastructure.persistence.HeroDaoJdbc;

public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        AUTO-DELVER - SIMULADOR DE BATALHAS       ");
        System.out.println("==================================================\n");

        // 1. Criacao da equipe do jogador a partir do Banco de Dados
        Party playerParty = new Party("Comitiva da Luz");

        // Instancia a implementacao JDBC escondida atras da interface DAO
        HeroDao heroDao = new HeroDaoJdbc();

        try {
            List<Hero> registeredHeroes = heroDao.findAll();   // <-- corrigido

            if (registeredHeroes.isEmpty()) {
                System.out.println("Aviso: Nenhum heroi encontrado no banco de dados.");
                System.out.println("Insira registros na tabela 'heroes' via DBeaver para iniciar.");
                return;
            }

            // Popula a equipe com as instâncias carregadas da infraestrutura

            for (Hero hero : registeredHeroes) {
                playerParty.addMember(hero);
            }
        } catch (RuntimeException e) {
            System.err.println("Falha critica ao carregar herois: " + e.getMessage());
            return; // Interrompe o jogo se o banco estiver fora do ar ou com credenciais incorretas
        }

        // 2. Criacao da horda inimiga procedimental
        Party enemyParty = new Party("Horda Sombria");
        EnemyFactory fabricaInimigos = new EnemyFactory();
        enemyParty.addMember(fabricaInimigos.createRandomEnemy());
        enemyParty.addMember(fabricaInimigos.createRandomEnemy());
        enemyParty.addMember(fabricaInimigos.createRandomEnemy());

        System.out.printf("--- %s ---%n", playerParty.getPartyName());
        imprimirGrupo(playerParty);

        System.out.printf("--- %s ---%n", enemyParty.getPartyName());
        imprimirGrupo(enemyParty);

        // 3. Execucao da simulacao via motor de regras de negocio
        BattleEngine motor = new BattleEngine();
        Party vencedores = motor.simulateEncounter(playerParty, enemyParty);

        System.out.println("\n--- Registro do Combate ---");
        int turnoAtual = 0;

        // 4. Apresentacao dos logs agrupados por turno
        for (TurnLog log : motor.getCombatHistory()) {
            if (log.turnNumber() != turnoAtual) {
                turnoAtual = log.turnNumber();
                System.out.printf("%n[ INICIO DO TURNO %d ]%n", turnoAtual);
            }

            System.out.printf("  > %s ataca %s causando %d de dano! (HP restante: %d)%n",
                    log.attackerName(),
                    log.targetName(),
                    log.damageDealt(),
                    log.targetRemainingHp()
            );

            if (log.targetDefeated()) {
                System.out.printf("    * %s foi eliminado do combate! *%n", log.targetName());
            }
        }

        // 5. Exibicao do resultado final
        System.out.println("\n==================================================");
        System.out.printf("VENCEDOR: %s%n", vencedores.getPartyName());
        System.out.println("Membros Sobreviventes:");
        for (Combatant sobrevivente : vencedores.getAliveMembers()) {
            System.out.printf(" - %s (HP: %d)%n", sobrevivente.getName(), sobrevivente.getCurrentHp());
        }
        System.out.println("==================================================");
    }

    private static void imprimirGrupo(Party grupo) {
        for (Combatant membro : grupo.getAllMembers()) {
            System.out.printf(" - %s [HP: %d/%d | ATK: %d | SPD: %d]%n",
                    membro.getName(),
                    membro.getCurrentHp(), membro.getMaxHp(),
                    membro.getBaseAttack(), membro.getSpeed());
        }
        System.out.println();
    }
}