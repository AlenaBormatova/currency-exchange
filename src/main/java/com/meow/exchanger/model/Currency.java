package com.meow.exchanger.model;

public class Currency {
    private Integer id;
    private String code;
    private String name;
    private String sign;

    public Currency(Integer id, String code, String name, String sign) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSign() {
        return sign;
    }
}