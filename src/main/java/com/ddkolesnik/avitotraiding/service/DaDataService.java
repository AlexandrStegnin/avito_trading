package com.ddkolesnik.avitotraiding.service;

import com.ddkolesnik.avitotraiding.model.TradingEntity;
import org.springframework.stereotype.Service;
import ru.redcom.lib.integration.api.client.dadata.DaDataClient;
import ru.redcom.lib.integration.api.client.dadata.dto.Address;

/**
 * @author Alexandr Stegnin
 */

@Service
public class DaDataService {

    private final DaDataClient daDataClient;

    public DaDataService(DaDataClient daDataClient) {
        this.daDataClient = daDataClient;
    }

    public void cleanData(TradingEntity tradingEntity) {
        Address address = daDataClient.cleanAddress(tradingEntity.getAddress());
        String streetWithType = address.getStreetWithType() == null ? "" : address.getStreetWithType();
        String houseType = address.getHouseType() == null ? "" : address.getHouseType();
        String house = address.getHouse() == null ? "" : address.getHouse();
        String blockType = address.getBlockType() == null ? "" : address.getBlockType();
        String block = address.getBlock() == null ? "" : address.getBlock();
        String cleanAddress = streetWithType + " " + houseType + " " + house + " " + blockType + " " + block;
        tradingEntity.setCleanAddress(cleanAddress.trim());
        tradingEntity.setLatitude(address.getGeoLat());
        tradingEntity.setLongitude(address.getGeoLon());
        tradingEntity.setAddress(address.getResult());
    }

}
