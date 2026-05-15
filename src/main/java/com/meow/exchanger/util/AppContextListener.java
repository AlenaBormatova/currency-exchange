package com.meow.exchanger.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    private static final String DATABASE_PATH_IN_WEB_APP = "/WEB-INF/classes/database/currency_exchange.db";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        String databasePath = servletContext.getRealPath(DATABASE_PATH_IN_WEB_APP);

        if (databasePath == null) {
            throw new IllegalStateException("Failed to resolve database path");
        }

        ConnectionFactory.init(databasePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionFactory.shutdown();
    }
}