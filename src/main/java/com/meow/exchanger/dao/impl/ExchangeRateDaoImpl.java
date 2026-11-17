package com.meow.exchanger.dao.impl;

import com.meow.exchanger.config.ConnectionFactory;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.exception.ConflictException;
import com.meow.exchanger.exception.StorageException;
import com.meow.exchanger.model.Currency;
import com.meow.exchanger.model.ExchangeRate;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDaoImpl implements ExchangeRateDao {

    private static final String SELECT_WITH_CURRENCIES = """
            SELECT
                er.id AS exchangeRateId,
                er.rate AS rate,
            
                bc.id AS baseId,
                bc.code AS baseCode,
                bc.full_name AS baseName,
                bc.sign AS baseSign,
            
                tc.id AS targetId,
                tc.code AS targetCode,
                tc.full_name AS targetName,
                tc.sign AS targetSign
            
            FROM exchange_rates er
            JOIN currencies bc ON er.base_currency_id = bc.id
            JOIN currencies tc ON er.target_currency_id = tc.id
            """;

    private static final String FIND_ALL_SQL =
            SELECT_WITH_CURRENCIES + """
                    ORDER BY bc.code, tc.code
                    """;

    private static final String FIND_BY_PAIR_CODES_SQL = SELECT_WITH_CURRENCIES + """
            WHERE bc.code = ?
              AND tc.code = ?
            """;

    private static final String INSERT_SQL = """
            INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;

    private static final String UPDATE_RATE_SQL = """
            UPDATE exchange_rates
            SET rate = ?
            WHERE base_currency_id = (SELECT id FROM currencies WHERE code = ?)
              AND target_currency_id = (SELECT id FROM currencies WHERE code = ?)
            """;

    @Override
    public List<ExchangeRate> findAll() {

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            List<ExchangeRate> exchangeRates = new ArrayList<>();

            while (resultSet.next()) {
                exchangeRates.add(mapRow(resultSet));
            }

            return exchangeRates;

        } catch (SQLException e) {
            throw new StorageException("Failed to load exchange rates", e);
        }
    }

    @Override
    public Optional<ExchangeRate> findByPair(String baseCode, String targetCode) {

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_PAIR_CODES_SQL)) {

            statement.setString(1, baseCode);
            statement.setString(2, targetCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new StorageException("Failed to find exchange rate", e);
        }
    }

    @Override
    public ExchangeRate insert(ExchangeRate exchangeRate) {

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

        } catch (SQLException e) {
            if (e instanceof SQLiteException sqliteException
                    && sqliteException.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                throw new ConflictException("Exchange rate already exists");
            }

            throw new StorageException("Failed to insert exchange rate", e);
        }
    }

    @Override
    public Optional<ExchangeRate> updateRate(String baseCode, String targetCode, BigDecimal rate) {

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_RATE_SQL)) {

            statement.setBigDecimal(1, rate);
            statement.setString(2, baseCode);
            statement.setString(3, targetCode);

            if (statement.executeUpdate() == 0) {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to update exchange rate", e);
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