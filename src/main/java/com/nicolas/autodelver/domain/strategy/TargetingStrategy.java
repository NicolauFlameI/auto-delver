package com.nicolas.autodelver.domain.strategy;

import java.util.List;
import com.nicolas.autodelver.domain.Combatant;

// Padrao Strategy: Define uma familia de algoritmos de selecao de alvo,
// encapsula cada um deles e torna-os intercambiaveis dinamicamente.
public interface TargetingStrategy {

    // Recebe a lista de inimigos vivos e decide qual deles sera atacado.
    Combatant selectTarget(List<Combatant> availableEnemies);
}

