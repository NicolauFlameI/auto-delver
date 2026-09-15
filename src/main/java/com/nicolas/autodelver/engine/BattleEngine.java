package com.nicolas.autodelver.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nicolas.autodelver.domain.Combatant;
import com.nicolas.autodelver.domain.TurnLog;

// Modulo de Dominio: Motor de Resolucao de Combates Autonomos.
// Executa o confronto em memoria entre dois combatentes e produz um historico de turnos.
public class BattleEngine {

    // Lista mutavel interna que acumula os eventos gerados a cada acao ofensiva.
    private final List<TurnLog> combatHistory;

    public BattleEngine() {
        this.combatHistory = new ArrayList<>();
    }

    // Metodologia: Execucao de Ciclo Fechado (Headless Simulation).
    // Roda os turnos ate que um dos combatentes tenha seu HP zerado.
    public Combatant simulateEncounter(Combatant fighterA, Combatant fighterB) {
        // Validacao defensiva para impedir simulacao com dados nulos ou combatentes mortos.
        if (fighterA == null || fighterB == null) {
            throw new IllegalArgumentException("Ambos os combatentes devem ser nao-nulos para iniciar.");
        }
        if (!fighterA.isAlive() || !fighterB.isAlive()) {
            throw new IllegalStateException("Ambos os combatentes devem estar vivos para combater.");
        }

        combatHistory.clear();
        int currentTurn = 1;

        // Loop de simulacao: continua enquanto ambos tiverem HP > 0.
        while (fighterA.isAlive() && fighterB.isAlive()) {

            // 1. Acao do Combatente A sobre o Combatente B.
            resolveAction(fighterA, fighterB, currentTurn);

            // 2. Validacao de Ciclo de Vida Ativo:
            // Se B for derrotado pelo ataque de A, ele perde o direito de agir neste turno.
            if (fighterB.isAlive()) {
                resolveAction(fighterB, fighterA, currentTurn);
            }

            currentTurn++;
        }

        // Retorna a entidade sobrevivente como vencedora do confronto.
        return fighterA.isAlive() ? fighterA : fighterB;
    }

    // Processa o disparo de um ataque individual e registra no historico.
    private void resolveAction(Combatant attacker, Combatant target, int turn) {
        int hpBefore = target.getCurrentHp();
        attacker.attack(target);
        int damageActual = hpBefore - target.getCurrentHp();

        // Armazena o registro do turno gerado pela acao.
        combatHistory.add(new TurnLog(
                turn,
                attacker.getName(),
                target.getName(),
                damageActual,
                target.getCurrentHp(),
                !target.isAlive()
        ));
    }

    // Metodologia: Copia Defensiva.
    // Retorna uma visao imutavel da lista para impedir modificacao externa do historico.
    public List<TurnLog> getCombatHistory() {
        return Collections.unmodifiableList(combatHistory);
    }
}