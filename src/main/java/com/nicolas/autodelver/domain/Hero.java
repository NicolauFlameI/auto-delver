package com.nicolas.autodelver.domain;
import com.nicolas.autodelver.domain.strategy.TargetingStrategy;

// Classe Concreta representando as unidades do jogador.
// Herda todo o controle de ciclo de vida e blindagem de estado de AbstractCombatant.
public class Hero extends AbstractCombatant {

    // Delega a inicializacao e as validacoes defensivas para a superclasse.
    public Hero(String name, int maxHp, int baseAttack, int speed, TargetingStrategy targetingStrategy) {
        super(name, maxHp, baseAttack, speed, targetingStrategy);
    }
}