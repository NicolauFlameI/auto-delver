package com.nicolas.autodelver.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Padrão de Projeto: First-Class Collection.
// Encapsula a lista de combatentes e fornece métodos de negócio focados no grupo.
public class Party {

    private final String partyName;
    private final List<Combatant> members;   // <-- corrigido

    public Party(String partyName) {
        if (partyName == null || partyName.isBlank()) {
            throw new IllegalArgumentException("O nome do grupo nao pode ser vazio.");
        }
        this.partyName = partyName.trim();
        this.members = new ArrayList<>();
    }

    public String getPartyName() {
        return partyName;
    }

    public void addMember(Combatant combatant) {
        if (combatant == null) {
            throw new IllegalArgumentException("Nao e possivel adicionar um combatente nulo ao grupo.");
        }
        this.members.add(combatant);
    }

    // Metodologia: Retorno Filtrado via Streams.
    // Facilita o trabalho do motor ao entregar apenas as entidades que ainda podem lutar.
    public List<Combatant> getAliveMembers() {   // <-- corrigido
        return members.stream()
                .filter(Combatant::isAlive)
                .toList(); // Retorna uma lista imutavel nativa do Java 16+
    }

    // Regra de negocio: o grupo perde quando não há mais membros vivos.
    public boolean isDefeated() {
        return getAliveMembers().isEmpty();
    }

    // Retorna todos os membros originais (vivos e mortos) para estatísticas de fim de batalha.
    public List<Combatant> getAllMembers() {   // <-- corrigido
        return Collections.unmodifiableList(members);
    }
}