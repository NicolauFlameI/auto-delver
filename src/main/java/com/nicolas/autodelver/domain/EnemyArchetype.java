package com.nicolas.autodelver.domain;

// Padrao de Projeto: Enum com Metadados de Dominio.
// Define os perfis de monstros disponiveis na masmorra e os seus limites de atributos.
public enum EnemyArchetype {

    // Parametros: Nome, minHp, maxHp, minAtk, maxAtk, minSpeed, maxSpeed
    GOBLIN("Goblin Saqueador", 30, 45, 8, 12, 14, 20),
    ESQUELETO("Guerreiro Esqueleto", 40, 60, 10, 15, 5, 9),
    ORC("Orc Brutamontes", 65, 90, 14, 20, 8, 12);

    private final String defaultName;
    private final int minHp, maxHp;
    private final int minAttack, maxAttack;
    private final int minSpeed, maxSpeed;

    EnemyArchetype(String defaultName, int minHp, int maxHp, int minAttack, int maxAttack, int minSpeed, int maxSpeed) {
        this.defaultName = defaultName;
        this.minHp = minHp;
        this.maxHp = maxHp;
        this.minAttack = minAttack;
        this.maxAttack = maxAttack;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
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

    public int getMinSpeed() {
        return minSpeed; }

    public int getMaxSpeed() {
        return maxSpeed; }
}
