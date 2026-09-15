package com.nicolas.autodelver.domain;

// Classe Concreta representando as unidades do jogador.
// Herda todo o controle de ciclo de vida e blindagem de estado de AbstractCombatant.
public class Hero extends AbstractCombatant {

    // Delega a inicializacao e as validacoes defensivas para a superclasse.
    public Hero(String name, int maxHp, int baseAttack) {
        super(name, maxHp, baseAttack);
    }
}