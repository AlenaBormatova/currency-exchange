package com.meow.exchanger.dao.impl;

import com.meow.exchanger.config.ConnectionFactory;
import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.DatabaseException;
import com.meow.exchanger.model.Currency;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDaoImpl implements CurrencyDao {

    @Override
    public List<Currency> findAll() {
        String findAllSql = "SELECT ID, Code, FullName, Sign FROM Currencies ORDER BY Code";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(findAllSql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Currency> currencies = new ArrayList<>();

            while (resultSet.next()) {
                currencies.add(mapRow(resultSet));
            }

            return currencies;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to load currencies", e);
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        String findByCodeSql = "SELECT ID, Code, FullName, Sign FROM Currencies WHERE Code = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(findByCodeSql)) {

            statement.setString(1, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new DatabaseException("Failed to find currency", e);
        }
    }

    @Override
    public Currency insert(Currency currency) {
        String insertSql = "INSERT INTO Currencies (Code, FullName, Sign) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

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

        } catch (SQLException e) {
            if (e instanceof SQLiteException sqliteException
                    && sqliteException.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                throw new ConflictException("Currency with this code already exists");
            }

            throw new DatabaseException("Failed to insert currency", e);
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