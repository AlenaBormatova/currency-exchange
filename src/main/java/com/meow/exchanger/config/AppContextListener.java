package com.meow.exchanger.config;

import com.meow.exchanger.dao.CurrencyDao;
import com.meow.exchanger.dao.ExchangeRateDao;
import com.meow.exchanger.dao.impl.CurrencyDaoImpl;
import com.meow.exchanger.dao.impl.ExchangeRateDaoImpl;
import com.meow.exchanger.service.CurrencyService;
import com.meow.exchanger.service.ExchangeRateService;
import com.meow.exchanger.service.ExchangeService;
import com.meow.exchanger.service.impl.CurrencyServiceImpl;
import com.meow.exchanger.service.impl.ExchangeRateServiceImpl;
import com.meow.exchanger.service.impl.ExchangeServiceImpl;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConnectionFactory.init();

        CurrencyDao currencyDao = new CurrencyDaoImpl();
        ExchangeRateDao exchangeRateDao = new ExchangeRateDaoImpl();

        CurrencyService currencyService = new CurrencyServiceImpl(currencyDao);
        ExchangeRateService exchangeRateService = new ExchangeRateServiceImpl(exchangeRateDao, currencyDao);
        ExchangeService exchangeService = new ExchangeServiceImpl(exchangeRateService);

        ServletContext context = sce.getServletContext();

        context.setAttribute("currencyService", currencyService);
        context.setAttribute("exchangeRateService", exchangeRateService);
        context.setAttribute("exchangeService", exchangeService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionFactory.shutdown();
    }
}