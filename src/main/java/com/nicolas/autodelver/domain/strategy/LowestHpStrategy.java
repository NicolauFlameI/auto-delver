package com.nicolas.autodelver.domain.strategy;

import java.util.Comparator;
import java.util.List;
import com.nicolas.autodelver.domain.Combatant;

public class LowestHpStrategy implements TargetingStrategy {

    @Override
    public Combatant selectTarget(List<Combatant> availableEnemies) {
        if (availableEnemies == null || availableEnemies.isEmpty()) {
            throw new IllegalArgumentException("A lista de alvos nao pode estar vazia.");
        }

        // Retorna o inimigo que tiver o menor currentHp no momento
        return availableEnemies.stream()
                .min(Comparator.comparingInt(Combatant::getCurrentHp))
                .orElseThrow();
    }
}
