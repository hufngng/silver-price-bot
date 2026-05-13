package com.example.silverprice.service.price;

public interface PriceProvider {
    String fetch() throws Exception;
    String name();
}
