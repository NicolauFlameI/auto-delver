package com.nicolas.autodelver.domain;

// Metodologia: Heranca Abstrata (DRY - Don't Repeat Yourself).
// Centraliza regras de integridade e calculo de vida comuns a herois e monstros.
public abstract class AbstractCombatant implements Combatant {

    // Atributos imutaveis (final) que nao mudam apos a instanciacao.
    private final String name;
    private final int maxHp;
    private final int baseAttack;

    // Atributo mutavel controlado exclusivamente por metodos internos de negocio.
    private int currentHp;

    // Construtor protegido: apenas subclasses concretas podem invocar.
    protected AbstractCombatant(String name, int maxHp, int baseAttack) {
        // Metodologia: Programacao Defensiva (Fail-Fast).
        // Impede a criacao de combatentes em estado inconsistente na memoria.
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do combatente nao pode ser vazio ou nulo.");
        }
        if (maxHp <= 0) {
            throw new IllegalArgumentException("O HP maximo inicial deve ser maior que zero.");
        }
        if (baseAttack < 0) {
            throw new IllegalArgumentException("O valor de ataque base nao pode ser negativo.");
        }

        this.name = name.trim();
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.baseAttack = baseAttack;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCurrentHp() {
        return currentHp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    @Override
    public int getBaseAttack() {
        return baseAttack;
    }

    @Override
    public boolean isAlive() {
        // Regra de negocio: vivo estritamente enquanto tiver pelo menos 1 ponto de HP.
        return this.currentHp > 0;
    }

    @Override
    public void takeDamage(int amount) {
        // Ignora valores nulos ou negativos para evitar cura acidental via takeDamage.
        if (amount <= 0) {
            return;
        }

        // Metodologia: Clamping de Valor.
        // Math.max garante que a vida nunca fique negativa (ex: -3 HP), travando em 0.
        this.currentHp = Math.max(0, this.currentHp - amount);
    }

    @Override
    public void attack(Combatant target) {
        // Validacao de Ciclo de Vida: combatente derrotado perde o direito de agir.
        if (!this.isAlive()) {
            return;
        }

        if (target == null) {
            throw new IllegalArgumentException("O alvo do ataque nao pode ser nulo.");
        }

        // Executa o dano no alvo apenas se o alvo tambem estiver ativo.
        if (target.isAlive()) {
            target.takeDamage(this.baseAttack);
        }
    }
}