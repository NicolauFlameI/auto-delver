package com.nicolas.autodelver.domain;

import com.nicolas.autodelver.domain.strategy.FrontlineStrategy;
import com.nicolas.autodelver.domain.strategy.LowestHpStrategy;
import com.nicolas.autodelver.domain.strategy.TargetingStrategy;

public enum EnemyArchetype {

    // Parametros: Nome, minHp, maxHp, minAtk, maxAtk, minSpeed, maxSpeed, estrategia de alvo
    GOBLIN("Goblin Saqueador", 30, 45, 8, 12, 14, 20, new LowestHpStrategy()),
    ESQUELETO("Guerreiro Esqueleto", 40, 60, 10, 15, 5, 9, new FrontlineStrategy()),
    ORC("Orc Brutamontes", 65, 90, 14, 20, 8, 12, new FrontlineStrategy());

    private final String defaultName;
    private final int minHp, maxHp;
    private final int minAttack, maxAttack;
    private final int minSpeed, maxSpeed;
    private final TargetingStrategy targetingStrategy;

    EnemyArchetype(String defaultName, int minHp, int maxHp, int minAttack, int maxAttack,
                   int minSpeed, int maxSpeed, TargetingStrategy targetingStrategy) {
        this.defaultName = defaultName;
        this.minHp = minHp;
        this.maxHp = maxHp;
        this.minAttack = minAttack;
        this.maxAttack = maxAttack;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.targetingStrategy = targetingStrategy;
    }

    public String getDefaultName() {
        return defaultName; }

    public int getMinHp() {
        return minHp; }

    public int getMaxHp() {
        return maxHp; }

    public int getMinAttack() {
        return minAttack; }

    public int getMaxAttack() {
        return maxAttack; }

    public int getMinSpeed() {
        return minSpeed; }

    public int getMaxSpeed() {
        return maxSpeed; }

    public TargetingStrategy getTargetingStrategy() {
        return targetingStrategy; }
}