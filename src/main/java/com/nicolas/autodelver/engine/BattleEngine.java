package com.nicolas.autodelver.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.Party;
import com.nicolas.autodelver.domain.TurnLog;

// Modulo de Dominio: Motor de Resolucao de Combates Autonomos.
// Executa o confronto em turnos entre duas equipes e gera o historico de eventos.
public class BattleEngine {

    // Limite de seguranca: se a luta passar disso, consideramos empate tecnico.
    // Evita laco infinito quando ninguem consegue causar dano (ex: todos com ataque 0).
    private static final int MAX_TURNS = 1000;

    // Historico de golpes da ultima simulacao (campo restaurado).
    private final List<TurnLog> combatHistory;

    // Construtor restaurado: sem ele a lista seria null e causaria NullPointerException.
    public BattleEngine() {
        this.combatHistory = new ArrayList<>();
    }

    // Processa a simulacao completa ate que uma das equipes seja derrotada.
    public Party simulateEncounter(Party partyA, Party partyB) {
        if (partyA == null || partyB == null) {
            throw new IllegalArgumentException("As equipes nao podem ser nulas.");
        }
        if (partyA.isDefeated() || partyB.isDefeated()) {
            throw new IllegalStateException("Ambas as equipes devem ter membros vivos para iniciar o combate.");
        }

        // Limpa o historico de uma simulacao anterior, caso o motor seja reutilizado.
        combatHistory.clear();
        int currentTurn = 1;

        while (!partyA.isDefeated() && !partyB.isDefeated()) {

            // Guarda anti-loop: aborta a simulacao se o combate nao termina.
            if (currentTurn > MAX_TURNS) {
                throw new IllegalStateException(
                        "Combate abortado: limite de " + MAX_TURNS + " turnos atingido (empate tecnico).");
            }

            // Monta a ordem de iniciativa com todos os combatentes vivos.
            List<Combatant> turnOrder = new ArrayList<>();
            turnOrder.addAll(partyA.getAliveMembers());
            turnOrder.addAll(partyB.getAliveMembers());

            // Maior velocidade age primeiro. O sort e estavel: em empate, a Party A age antes.
            turnOrder.sort((c1, c2) -> Integer.compare(c2.getSpeed(), c1.getSpeed()));

            for (Combatant attacker : turnOrder) {
                // Quem morreu durante este turno nao age.
                if (!attacker.isAlive()) {
                    continue;
                }
                // Se uma equipe inteira caiu, o combate ja acabou.
                if (partyA.isDefeated() || partyB.isDefeated()) {
                    break;
                }

                // Descobre qual e a equipe adversaria do atacante.
                Party enemyParty = partyA.getAllMembers().contains(attacker) ? partyB : partyA;

                // O motor entrega os alvos vivos; o atacante escolhe usando a propria estrategia.
                List<Combatant> availableTargets = enemyParty.getAliveMembers();
                Combatant target = attacker.chooseTarget(availableTargets);

                resolveAction(attacker, target, currentTurn);
            }

            currentTurn++;
        }

        // Retorna a equipe que sobrou viva.
        return partyA.isDefeated() ? partyB : partyA;
    }

    // Executa um ataque e registra no historico quanto dano foi realmente causado.
    private void resolveAction(Combatant attacker, Combatant target, int turn) {
        int hpBefore = target.getCurrentHp();
        attacker.attack(target);
        int damageActual = hpBefore - target.getCurrentHp();

        combatHistory.add(new TurnLog(
                turn,
                attacker.getId(),
                attacker.getName(),
                target.getId(),
                target.getName(),
                damageActual,
                target.getCurrentHp(),
                !target.isAlive()
        ));
    }

    // Devolve o historico como lista somente leitura, protegendo o estado interno.
    public List<TurnLog> getCombatHistory() {
        return Collections.unmodifiableList(combatHistory);
    }
}