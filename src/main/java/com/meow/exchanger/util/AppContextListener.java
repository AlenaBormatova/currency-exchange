package com.meow.exchanger.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConnectionFactory.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionFactory.shutdown();
    }
}