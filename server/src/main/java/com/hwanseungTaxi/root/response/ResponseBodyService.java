package com.hwanseungTaxi.root.response;

import com.hwanseungTaxi.root.kakaoMobility.TaxiInfoEntity;
import com.hwanseungTaxi.root.mockData.entity.*;
import com.hwanseungTaxi.root.response.entity.Info;
import com.hwanseungTaxi.root.response.entity.Position;
import com.hwanseungTaxi.root.response.entity.ResponseBody;
import com.hwanseungTaxi.root.response.entity.Step;
import com.hwanseungTaxi.root.response.entity.Summary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class ResponseBodyService {

    public ResponseBody getResponseBody(MockEntity mockEntity, List<TaxiInfoEntity> adjustTaxiInfoEntities,
                                        HashMap<String, Integer> sectionTimeMap, LinkedHashMap<String, Double> efficiencyMap, List<TaxiInfoEntity> taxiInfoEntities) {

        int maxTaxiFare = getMaxTaxiFare(taxiInfoEntities);

        HashMap<String, Position> stationInfo = getStationInfo(mockEntity);
        List<Info> infoList = getInfoList(mockEntity, adjustTaxiInfoEntities, sectionTimeMap, efficiencyMap, maxTaxiFare);

        return new ResponseBody(stationInfo, infoList);
    }

    private List<Info> getInfoList(MockEntity mockEntity, List<TaxiInfoEntity> taxiInfoEntities,
                                   HashMap<String, Integer> sectionTimeMap, LinkedHashMap<String, Double> efficiencyMap, int maxTaxiFare) {
        List<Info> infoList = new ArrayList<>();
        // Info -> Summary -> taxiFare, wastedTime, savedTime
        // Info -> steps -> step -> mode, sectionTime, route, routeId, stationList
        Itinerary itinerary = mockEntity.metaData.plan.itineraries.get(0);
        List<Leg> legs = itinerary.legs;
        for(String key : efficiencyMap.keySet()) {  // 택시 효율이 좋은 순서대로 응답을 내놓기 위해, key는 택시 목적지 정류소 이름을 뜻한다.
            // Summary 정보 작업
            TaxiInfoEntity taxiInfoEntity = getTaxiInfoEntity(taxiInfoEntities, key);
            int savedTime = sectionTimeMap.get(key) - taxiInfoEntity.getTaxiDuration();
            Summary summary = new Summary(taxiInfoEntity.getTaxiFare(), itinerary.totalTime-savedTime, savedTime, maxTaxiFare-taxiInfoEntity.getTaxiFare());

            // Steps 정보 작업
            List<Step> steps = new ArrayList<>();
            // 택시 정보 제일 먼저 넣기
            Step step = new Step("TAXI", taxiInfoEntity.getTaxiDuration(), null, null, List.of("출발지", taxiInfoEntity.getStationName()));
            steps.add(step);
            boolean isCheck = false; // 택시 목적지를 통과했는지 확인 여부
            int arrivalNum = 1; // 걷기 도착지명 정보가 모두 "도착지" 이기에 구분을 위한 숫자
            for(Leg leg : legs) {
                List<String> responseStationList = new ArrayList<>();
                int duration = 0;
                if(!leg.mode.equals("WALK")) { // 첫번째 걷기는 택시로 치환되니까
                    List<Station> stationList = leg.passStopList.stationList;
                    for(Station station : stationList) {
                        if(station.stationName.equals(taxiInfoEntity.getStationName())) { // 택시 목적지와 같은 정류소를 만났다면
                            isCheck = true;
                        }
                        if(isCheck) { // 택시 목적지와 같은 정류소를 만난 이후
                            duration += station.duration;
                            responseStationList.add(station.stationName);
                        }
                    }
                    if(responseStationList.size()!=0) {
                        Step step1 = new Step(leg.mode, duration,leg.route, leg.routeId, responseStationList);
                        steps.add(step1);
                    }
                }
                if(isCheck && leg.mode.equals("WALK")) { // 첫번째 걷기가 아닌 경우
                    Step step1 = new Step("WALK", leg.sectionTime, null, null, List.of(leg.start.name, leg.end.name));
                    steps.add(step1);
                }
            }
            Info info = new Info(summary, steps);
            infoList.add(info);
        }

        return infoList;
    }

    private TaxiInfoEntity getTaxiInfoEntity(List<TaxiInfoEntity> taxiInfoEntities, String key) {
        for(TaxiInfoEntity taxiInfoEntity : taxiInfoEntities) {
            if(taxiInfoEntity.getStationName().equals(key)) {
                return taxiInfoEntity;
            }
        }
        return null;
    }

    private HashMap<String, Position> getStationInfo(MockEntity mockEntity) {
        HashMap<String, Position> stationInfo = new HashMap<>();
        List<Leg> legs = mockEntity.metaData.plan.itineraries.get(0).legs;

        int arrivalNum = 1;

        for(Leg leg : legs) {
            if(leg.mode.equals("WALK")) {
                Position startPosition = new Position(String.valueOf(leg.start.lon), String.valueOf(leg.start.lat), null);
                stationInfo.put(leg.start.name, startPosition);
                Position endPosition = new Position(String.valueOf(leg.end.lon), String.valueOf(leg.end.lat), null);
                stationInfo.put(leg.end.name, startPosition);
            }else {
                List<Station> stationList = leg.passStopList.stationList;
                for(Station station : stationList) {
                    Position position = new Position(station.lon, station.lat, station.stationID);
                    stationInfo.put(station.stationName, position);
                }
            }
        }
        return stationInfo;
    }

    private  int getMaxTaxiFare(List<TaxiInfoEntity> taxiInfoEntities) {
        int maxValue = Integer.MIN_VALUE;

        for(TaxiInfoEntity taxiInfoEntity : taxiInfoEntities) {
            if(maxValue < taxiInfoEntity.getTaxiFare()) maxValue = taxiInfoEntity.getTaxiFare();
        }

        return maxValue;
    }

}
