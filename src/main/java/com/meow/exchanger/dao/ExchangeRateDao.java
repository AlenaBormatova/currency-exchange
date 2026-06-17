package com.meow.exchanger.dao;

import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import com.meow.exchanger.util.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {

    private static final String SELECT_WITH_CURRENCIES = """
            SELECT
                er.ID AS exchangeRateId,
                er.Rate AS rate,
            
                bc.ID AS baseId,
                bc.Code AS baseCode,
                bc.FullName AS baseName,
                bc.Sign AS baseSign,
            
                tc.ID AS targetId,
                tc.Code AS targetCode,
                tc.FullName AS targetName,
                tc.Sign AS targetSign
            
            FROM ExchangeRates er
            JOIN Currencies bc ON er.BaseCurrencyId = bc.ID
            JOIN Currencies tc ON er.TargetCurrencyId = tc.ID
            """;

    public List<ExchangeRate> findAll() throws SQLException {
        String FIND_ALL_SQL = SELECT_WITH_CURRENCIES + " ORDER BY bc.Code, tc.Code";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            List<ExchangeRate> exchangeRates = new ArrayList<>();

            while (resultSet.next()) {
                exchangeRates.add(mapRow(resultSet));
            }

            return exchangeRates;
        }
    }

    public Optional<ExchangeRate> findByPair(String baseCode, String targetCode) throws SQLException {
        String FIND_BY_PAIR_CODES_SQL = SELECT_WITH_CURRENCIES + """
                WHERE bc.Code = ?
                  AND tc.Code = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_PAIR_CODES_SQL)) {

            statement.setString(1, baseCode);
            statement.setString(2, targetCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public ExchangeRate insert(ExchangeRate exchangeRate) throws SQLException {
        String INSERT_SQL = """
                INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, exchangeRate.getBaseCurrency().getId());
            statement.setInt(2, exchangeRate.getTargetCurrency().getId());
            statement.setBigDecimal(3, exchangeRate.getRate());

            statement.executeUpdate();

            int generatedId = readGeneratedId(statement);

            return new ExchangeRate(
                    generatedId,
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency(),
                    exchangeRate.getRate()
            );
        }
    }

    public Optional<ExchangeRate> updateRate(String baseCode, String targetCode, BigDecimal rate) throws SQLException {
        String UPDATE_RATE_SQL = """
                UPDATE ExchangeRates
                SET Rate = ?
                WHERE BaseCurrencyId = (SELECT ID FROM Currencies WHERE Code = ?)
                  AND TargetCurrencyId = (SELECT ID FROM Currencies WHERE Code = ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_RATE_SQL)) {

            statement.setBigDecimal(1, rate);
            statement.setString(2, baseCode);
            statement.setString(3, targetCode);

            if (statement.executeUpdate() == 0) {
                return Optional.empty();
            }
        }

        return findByPair(baseCode, targetCode);
    }

    private int readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Failed to get generated exchange rate ID");
            }

            return keys.getInt(1);
        }
    }

    private ExchangeRate mapRow(ResultSet resultSet) throws SQLException {
        Currency baseCurrency = new Currency(
                resultSet.getInt("baseId"),
                resultSet.getString("baseCode"),
                resultSet.getString("baseName"),
                resultSet.getString("baseSign")
        );

        Currency targetCurrency = new Currency(
                resultSet.getInt("targetId"),
                resultSet.getString("targetCode"),
                resultSet.getString("targetName"),
                resultSet.getString("targetSign")
        );

        return new ExchangeRate(
                resultSet.getInt("exchangeRateId"),
                baseCurrency,
                targetCurrency,
                resultSet.getBigDecimal("rate")
        );
    }
}