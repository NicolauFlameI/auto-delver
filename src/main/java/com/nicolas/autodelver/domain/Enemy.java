package com.nicolas.autodelver.domain;

// Classe Concreta representando ameacas e inimigos gerados pelo sistema na masmorra.
public class Enemy extends AbstractCombatant {

    // Delega a inicializacao e as validacoes defensivas para a superclasse.
    public Enemy(String name, int maxHp, int baseAttack) {
        super(name, maxHp, baseAttack);
    }
}