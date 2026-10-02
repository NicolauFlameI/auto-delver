package com.nicolas.autodelver.infrastructure.persistence;

import java.util.List;
import com.nicolas.autodelver.domain.Hero;

// Padrão DAO: Oculta a complexidade das operacoes de banco de dados.
public interface HeroDao {

    // Busca todos os herois cadastrados no banco para montar a equipe.
    List<Hero> findAll();   // <-- corrigido
}