package com.nicolas.autodelver.domain.strategy;

import java.util.List;
import com.nicolas.autodelver.domain.Combatant;

public class FrontlineStrategy implements TargetingStrategy {

    @Override
    public Combatant selectTarget(List<Combatant> availableEnemies) {
        if (availableEnemies == null || availableEnemies.isEmpty()) {
            throw new IllegalArgumentException("A lista de alvos nao pode estar vazia.");
        }
        // Ataca sempre o primeiro combatente da formacao inimiga
        return availableEnemies.get(0);
    }
}