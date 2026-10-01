package com.nicolas.autodelver.domain;
import java.util.List;

// Padrao de Projeto: Interface de Dominio (Tell, Don't Ask).
// Define o contrato de comportamento universal que qualquer combatente precisa atender,
// sem expor atributos internos diretamente para o mundo exterior.
public interface Combatant {

    // Retorna o identificador textual da entidade.
    String getName();

    // Consulta de integridade: retorna a vida restante atual.
    int getCurrentHp();

    // Consulta de integridade maxima permitida para a entidade.
    int getMaxHp();

    // Retorna a forca nominal que a entidade aplica em uma acao ofensiva.
    int getBaseAttack();

    // Retorna a agilidade da entidade, definindo quem ataca primeiro no turno.
    int getSpeed();

    // Metodo de negocio: verifica ativamente se a entidade continua operacional (hp > 0).
    boolean isAlive();

    // Metodologia: Encapsulamento Rigido.
    // Em vez de setHp(), o estado se modifica atraves de uma regra de absorcao de dano.
    void takeDamage(int amount);

    // Metodologia: Tell, Don't Ask.
    // Uma entidade atua diretamente sobre o alvo sem precisar consultar os dados dele previamente.
    void attack(Combatant target);

    // Delega a escolha do alvo para a estrategia interna da entidade
    Combatant chooseTarget(List<Combatant> opponents);
}