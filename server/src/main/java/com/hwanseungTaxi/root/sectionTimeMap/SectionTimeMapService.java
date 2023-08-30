package com.hwanseungTaxi.root.sectionTimeMap;

import com.hwanseungTaxi.root.kakaoMobility.TaxiInfoEntity;
import com.hwanseungTaxi.root.mockData.entity.Leg;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import com.hwanseungTaxi.root.mockData.entity.Station;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SectionTimeMapService {

    public HashMap<String, Integer> getSectionTimeMap(String destination, MockEntity mockEntity) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        int sectionTime = 0;

        // 첫 출발지 대기 시간은 우선 제외하고 구현하기

        List<Leg> legs = mockEntity.metaData.plan.itineraries.get(0).legs;

        for(Leg leg : legs) {
            if(leg.mode.equals("WALK")) { // 걷기 시간인 경우 전부 더하기로 진행
                sectionTime += leg.sectionTime;
            }else {
                List<Station> stationList = leg.passStopList.stationList;
                for(Station station : stationList) {
                    sectionTime += station.duration;
                    hashMap.put(station.stationName, sectionTime);
                }
            }
        }
        return hashMap;
    }

    public LinkedHashMap<String, Double> getEfficiencyMap(List<TaxiInfoEntity> taxiInfoEntities, HashMap<String, Integer> sectionTimeMap) {
        LinkedHashMap<String, Double> efficiencyMap = new LinkedHashMap<>();
        for(TaxiInfoEntity taxiInfoEntity : taxiInfoEntities) {
            efficiencyMap.put(taxiInfoEntity.getStationName(), taxiInfoEntity.getTaxiFare()/(double)sectionTimeMap.get(taxiInfoEntity.getStationName()));
        }

        // 람다식을 이용하여 값 기준으로 정렬
        List<Map.Entry<String, Double>> entries = new ArrayList<>(efficiencyMap.entrySet());
        entries.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        // 스트림을 사용하여 정렬된 값들을 LinkedHashMap에 재배정
        LinkedHashMap<String, Double> sortedEfficiencyMap = entries.stream()
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        return sortedEfficiencyMap;
    }

}
