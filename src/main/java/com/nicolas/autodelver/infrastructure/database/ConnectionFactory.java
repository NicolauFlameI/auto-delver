package com.nicolas.autodelver.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Padrao de Projeto: Factory / Singleton Pattern simplificado.
// Centraliza a criacao de conexoes com o banco de dados.
public class ConnectionFactory {

    // Credenciais de acesso ao PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/autodelver_db";
    private static final String USER = "delver_admin";
    private static final String PASSWORD = "1234"; // Altere se tiver escolhido outra senha

    public static Connection getConnection() {
        try {
            // O DriverManager do Java localiza automaticamente o driver do PostgreSQL no pom.xml
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Converte a excecao checada do SQL para uma excecao de tempo de execucao,
            // impedindo que a aplicacao inicie se o banco estiver fora do ar.
            throw new RuntimeException("Erro critico: Nao foi possivel conectar ao PostgreSQL.", e);
        }
    }
}