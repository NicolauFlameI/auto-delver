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

    private final List<TurnLog> combatHistory;

    public BattleEngine() {
        this.combatHistory = new ArrayList<>();
    }

    // Processa a simulacao completa ate que uma das equipes seja completamente derrotada.
    public Party simulateEncounter(Party partyA, Party partyB) {
        if (partyA == null || partyB == null) {
            throw new IllegalArgumentException("As equipes nao podem ser nulas.");
        }
        if (partyA.isDefeated() || partyB.isDefeated()) {
            throw new IllegalStateException("Ambas as equipes devem ter membros vivos para iniciar o combate.");
        }

        combatHistory.clear();
        int currentTurn = 1;

        // O laco principal roda enquanto ambas as equipes tiverem sobreviventes
        while (!partyA.isDefeated() && !partyB.isDefeated()) {

            // 1. Determina a ordem de iniciativa global do turno atual
            List<Combatant> turnOrder = new ArrayList<>();
            turnOrder.addAll(partyA.getAliveMembers());
            turnOrder.addAll(partyB.getAliveMembers());

            // Ordena a lista decrescente com base na velocidade (maior age primeiro)
            turnOrder.sort((c1, c2) -> Integer.compare(c2.getSpeed(), c1.getSpeed()));

            // 2. Executa a acao de cada combatente na ordem estabelecida
            for (Combatant attacker : turnOrder) {
                // Validacao de Ciclo de Vida: se o atacante foi morto neste mesmo turno por alguem mais rapido, ele nao age.
                if (!attacker.isAlive()) {
                    continue;
                }

                // Interrompe o turno imediatamente se o combate ja foi decidido (uma equipe inteira caiu).
                if (partyA.isDefeated() || partyB.isDefeated()) {
                    break;
                }

                // Identifica aliados e inimigos com base na origem do atacante
                Party enemyParty = partyA.getAllMembers().contains(attacker) ? partyB : partyA;

                // Regra de Foco de Ataque (Auto-battler MVP): ataca a linha de frente (primeiro inimigo vivo)
                Combatant target = enemyParty.getAliveMembers().get(0);

                resolveAction(attacker, target, currentTurn);
            }

            currentTurn++;
        }

        // Retorna a equipe que sobrou viva
        return partyA.isDefeated() ? partyB : partyA;
    }

    private void resolveAction(Combatant attacker, Combatant target, int turn) {
        int hpBefore = target.getCurrentHp();
        attacker.attack(target);
        int damageActual = hpBefore - target.getCurrentHp();

        combatHistory.add(new TurnLog(
                turn,
                attacker.getName(),
                target.getName(),
                damageActual,
                target.getCurrentHp(),
                !target.isAlive()
        ));
    }

    public List<TurnLog> getCombatHistory() {
        return Collections.unmodifiableList(combatHistory);
    }
}