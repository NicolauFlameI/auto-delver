package com.nicolas.autodelver.domain;

// Padrao de Projeto: Enum com Metadados de Dominio.
// Define os perfis de monstros disponiveis na masmorra e os seus limites de atributos.
public enum EnemyArchetype {

    GOBLIN("Goblin Saqueador", 30, 45, 8, 12),
    ESQUELETO("Guerreiro Esqueleto", 40, 60, 10, 15),
    ORC("Orc Brutamontes", 65, 90, 14, 20);

    private final String defaultName;
    private final int minHp;
    private final int maxHp;
    private final int minAttack;
    private final int maxAttack;

    EnemyArchetype(String defaultName, int minHp, int maxHp, int minAttack, int maxAttack) {
        this.defaultName = defaultName;
        this.minHp = minHp;
        this.maxHp = maxHp;
        this.minAttack = minAttack;
        this.maxAttack = maxAttack;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public int getMinHp() {
        return minHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMinAttack() {
        return minAttack;
    }

    public int getMaxAttack() {
        return maxAttack;
    }
}