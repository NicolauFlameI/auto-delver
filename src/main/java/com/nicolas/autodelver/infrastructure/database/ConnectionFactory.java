// OBRIGATORIO: declara o pacote correto; sem esta linha o Java nao acha a classe.
package com.nicolas.autodelver.infrastructure.database;

// Imports do JDBC que estavam faltando (causa dos "cannot find symbol").
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Padrao de Projeto: Factory / Singleton Pattern simplificado.
// Centraliza a criacao de conexoes com o banco de dados.
public class ConnectionFactory {

    // Le a configuracao de variaveis de ambiente; se nao existirem, usa o valor padrao local.
    private static final String URL = getEnv("AUTODELVER_DB_URL",
            "jdbc:postgresql://localhost:5432/autodelver_db");
    private static final String USER = getEnv("AUTODELVER_DB_USER", "delver_admin");
    // Sem valor padrao para a senha: se a variavel nao existir, falha com mensagem clara.
    private static final String PASSWORD = getEnv("AUTODELVER_DB_PASSWORD", null);

    // Busca uma variavel de ambiente, com valor padrao opcional.
    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        if (defaultValue == null) {
            throw new IllegalStateException("Variavel de ambiente obrigatoria nao definida: " + key);
        }
        return defaultValue;
    }

    // Abre uma nova conexao com o PostgreSQL a cada chamada.
    public static Connection getConnection() {
        try {
            // O DriverManager localiza automaticamente o driver do PostgreSQL declarado no pom.xml.
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Converte a excecao checada em excecao de execucao, impedindo o jogo de iniciar sem banco.
            throw new RuntimeException("Erro critico: Nao foi possivel conectar ao PostgreSQL.", e);
        }
    }
}