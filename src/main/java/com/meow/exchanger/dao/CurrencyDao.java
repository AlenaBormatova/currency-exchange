package com.meow.exchanger.dao;

import com.meow.exchanger.model.Currency;
import com.meow.exchanger.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {

    public List<Currency> findAll() throws SQLException {
        String FIND_ALL_SQL = "SELECT ID, Code, FullName, Sign FROM Currencies ORDER BY Code";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            List<Currency> currencies = new ArrayList<>();

            while (resultSet.next()) {
                currencies.add(mapRow(resultSet));
            }

            return currencies;
        }
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        String FIND_BY_CODE_SQL = "SELECT ID, Code, FullName, Sign FROM Currencies WHERE Code = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CODE_SQL)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Currency insert(Currency currency) throws SQLException {
        String INSERT_SQL = "INSERT INTO Currencies (Code, FullName, Sign) VALUES (?, ?, ?)";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getName());
            statement.setString(3, currency.getSign());

            statement.executeUpdate();

            int generatedId = readGeneratedId(statement);

            return new Currency(
                    generatedId,
                    currency.getCode(),
                    currency.getName(),
                    currency.getSign()
            );
        }
    }

    private int readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Failed to get generated currency ID");
            }

            return keys.getInt(1);
        }
    }

    private Currency mapRow(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("ID"),
                resultSet.getString("Code"),
                resultSet.getString("FullName"),
                resultSet.getString("Sign")
        );
    }
}