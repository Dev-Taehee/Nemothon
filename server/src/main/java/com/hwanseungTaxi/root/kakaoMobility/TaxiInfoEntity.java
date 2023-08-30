package com.hwanseungTaxi.root.kakaoMobility;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TaxiInfoEntity {

    private String stationName;
    private String lon;
    private String lat;
    private int taxiDuration;
    private int taxiFare;

    @Override
    public String toString() {
        return "TaxiInfoEntity{" +
                "stationName='" + stationName + '\'' +
                ", lon=" + lon + '\'' +
                ", lat=" + lat + '\'' +
                ", taxiDuration=" + taxiDuration + '\'' +
                ", taxiFare=" + taxiFare + '}';
    }

}
