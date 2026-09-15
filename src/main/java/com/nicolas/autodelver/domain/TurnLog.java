package com.nicolas.autodelver.domain;

// Padrao de Projeto: DTO Imutavel (Java Record).
// Transporta o diagnostico de cada golpe realizado na simulacao
// sem expor metodos modificadores ou regras de negocio

public record TurnLog(
        int turnNumber,
        String attackerName,
        String targetName,
        int damageDealt,
        int targetRemainingHp,
        boolean targetDefeated
) {
    // Construtor compacto para validacao defensiva dos dados do registro.
    public TurnLog{
        if (turnNumber <= 0){
            throw new IllegalArgumentException("O numero do turno deve ser positivo.");
        }
        if (damageDealt < 0){
            throw new IllegalArgumentException("O dano registrado nao pode ser negativo.");
        }
    }
}
