package com.nicolas.autodelver.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.nicolas.autodelver.domain.Hero;
import com.nicolas.autodelver.domain.strategy.FrontlineStrategy;
import com.nicolas.autodelver.domain.strategy.LowestHpStrategy;
import com.nicolas.autodelver.domain.strategy.TargetingStrategy;
import com.nicolas.autodelver.infrastructure.database.ConnectionFactory;

public class HeroDaoJdbc implements HeroDao {

    @Override
    public List<Hero> findAll() {
        String sql = "SELECT name, max_hp, base_attack, speed, strategy_type FROM heroes";
        List<Hero> heroes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                int maxHp = rs.getInt("max_hp");
                int attack = rs.getInt("base_attack");
                int speed = rs.getInt("speed");
                String strategyType = rs.getString("strategy_type");

                heroes.add(new Hero(name, maxHp, attack, speed, toStrategy(name, strategyType)));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar herois no banco de dados.", e);
        }

        return heroes;
    }

    // Mapeia a string do banco para a Strategy. Falha explicitamente se o dado for invalido.
    private TargetingStrategy toStrategy(String heroName, String strategyType) {
        if (strategyType == null) {
            throw new IllegalArgumentException(
                    "Heroi '" + heroName + "' esta sem strategy_type no banco de dados.");
        }
        return switch (strategyType.trim().toUpperCase()) {
            case "LOWEST_HP" -> new LowestHpStrategy();
            case "FRONTLINE" -> new FrontlineStrategy();
            default -> throw new IllegalArgumentException(
                    "Estrategia desconhecida para o heroi '" + heroName + "': " + strategyType);
        };
    }
}