package com.nicolas.autodelver.engine;

import java.util.Random;

import com.nicolas.autodelver.domain.Enemy;
import com.nicolas.autodelver.domain.EnemyArchetype;

// Padrao de Projeto: Simple Factory.
// Centraliza a geracao pseudoaleatoria de instancias da classe Enemy.
public class EnemyFactory {

    // Instancia do gerador de numeros pseudoaleatorios da JVM.
    private final Random random;

    // Construtor padrao utilizando gerador de sistema.
    public EnemyFactory() {
        this(new Random());
    }

    // Metodologia: Injecao de Dependencia para Testabilidade.
    // Permite injetar um Random com semente fixa em testes de unidade deterministas.
    public EnemyFactory(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("A instancia de Random nao pode ser nula.");
        }
        this.random = random;
    }

    // Cria um inimigo totalmente aleatorio sorteando tipo, vida e forca.
    public Enemy createRandomEnemy() {
        EnemyArchetype[] archetypes = EnemyArchetype.values();
        // Sorteia um dos tipos existentes no enum.
        EnemyArchetype chosenType = archetypes[random.nextInt(archetypes.length)];
        return createFromArchetype(chosenType);
    }

    // Fabrica um inimigo calculando variacoes dentro da faixa permitida pelo arquetipo.
    public Enemy createFromArchetype(EnemyArchetype archetype) {
        if (archetype == null) {
            throw new IllegalArgumentException("O arquetipo nao pode ser nulo.");
        }

        // Formula: min + random(max - min + 1) para intervalo fechado inclusivo.
        int calculatedHp = archetype.getMinHp() + random.nextInt(archetype.getMaxHp() - archetype.getMinHp() + 1);
        int calculatedAttack = archetype.getMinAttack() + random.nextInt(archetype.getMaxAttack() - archetype.getMinAttack() + 1);

        return new Enemy(archetype.getDefaultName(), calculatedHp, calculatedAttack);
    }
}