package com.hwanseungTaxi.root.sectionTimeMap;

import com.hwanseungTaxi.root.kakaoMobility.TaxiInfoEntity;
import com.hwanseungTaxi.root.mockData.entity.Leg;
import com.hwanseungTaxi.root.mockData.entity.MockEntity;
import com.hwanseungTaxi.root.mockData.entity.Station;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Service
public class SectionTimeMapService {

    private static final String gyeongiBaseUrl = "http://apis.data.go.kr/6410000/busarrivalservice/getBusArrivalItem?";
    private static final String seoulBaseUrl = "http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRoute?";
    @Value("${dataPortal.apiKey}")
    private String apiKey;

    public HashMap<String, Integer> getSectionTimeMap(String destination, MockEntity mockEntity) throws IOException, JDOMException {
        HashMap<String, Integer> hashMap = new HashMap<>();
        int sectionTime = 0;

        // 첫 출발지 대기 시간은 우선 제외하고 구현하기

        List<Leg> legs = mockEntity.metaData.plan.itineraries.get(0).legs;

        int firstWaitTime = getFirstWaitTime(destination);
        boolean isAddFirstWaitTime = false;
        for(Leg leg : legs) {
            if(leg.mode.equals("WALK")) { // 걷기 시간인 경우 전부 더하기로 진행
                sectionTime += leg.sectionTime;
                if(!isAddFirstWaitTime) {
                    sectionTime += firstWaitTime;
                    isAddFirstWaitTime = true;
                }
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

    private int getFirstWaitTime(String destination) throws IOException, JDOMException {
        LocalTime currentTime = LocalTime.now();

        if(destination.equals("uijeongbu")) {

            String stationId = "113000422";
            String routeId = "111000014";
            return getXMLSeoulBusArrivalInfo(stationId, routeId);

        } else if (destination.equals("330")) {

            String stationId = "111000034";
            String routeId = "111900004";
            return getXMLSeoulBusArrivalInfo(stationId, routeId);

        } else if (destination.equals("gangnam")) {
            // 용인에서 강남 가는 경로 시작 정류장
            String stationId = "228000174";
            // 11시 이전과 이후의 버스 정류장 ID가 다름
            LocalTime elevenAM = LocalTime.of(11, 0);
            String routeId;
            if(currentTime.isBefore(elevenAM)) {
                routeId = "228000389";
            }else {
                routeId = "228000182";
            }
            return getXMLGyeongiBusArrivalInfo(stationId, routeId)*60;
        } else {
            return 600;
        }
    }

    public int getXMLSeoulBusArrivalInfo(String stationId, String routeId) throws IOException, JDOMException {
        // URL 주소 생성
        StringBuilder sb = new StringBuilder();
        sb.append(seoulBaseUrl);
        sb.append("serviceKey=");
        sb.append(apiKey);
        sb.append("&stId="+stationId);
        sb.append("&busRouteId="+routeId);

        // URL 연결
        URL url = new URL(sb.toString());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestProperty("Content-Type", "application/xml");
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(httpURLConnection.getInputStream());

        Element root = document.getRootElement();
        Element msgHeader = root.getChild("msgHeader");
        Element resultMessage = msgHeader.getChild("headerMsg");

        String predictTime = null;

        if(resultMessage.getContent(0).getValue().equals("정상적으로 처리되었습니다.")) {
            Element msgBody = root.getChild("msgBody");
            Element busArrivalItem = msgBody.getChild("busArrivalItem");
            Element predictTime1 = busArrivalItem.getChild("predictTime1");

            predictTime = predictTime1.getContent(0).getValue();
        }

        return predictTime == null ? (stationId.equals(113000422) ? 8 : 15) : Integer.parseInt(predictTime);
    }

    public int getXMLGyeongiBusArrivalInfo(String stationId, String routeId) throws IOException, JDOMException {
        // URL 주소 생성
        StringBuilder sb = new StringBuilder();
        sb.append(gyeongiBaseUrl);
        sb.append("serviceKey=");
        sb.append(apiKey);
        sb.append("&stationId="+stationId);
        sb.append("&routeId="+routeId);

        // URL 연결
        URL url = new URL(sb.toString());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestProperty("Content-Type", "application/xml");
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(httpURLConnection.getInputStream());

        Element root = document.getRootElement();
        Element msgHeader = root.getChild("msgHeader");
        Element resultMessage = msgHeader.getChild("resultMessage");

        String predictTime = null;

        if(resultMessage.getContent(0).getValue().equals("정상적으로 처리되었습니다.")) {
            Element msgBody = root.getChild("msgBody");
            Element busArrivalItem = msgBody.getChild("busArrivalItem");
            Element predictTime1 = busArrivalItem.getChild("predictTime1");

            predictTime = predictTime1.getContent(0).getValue();
        }

        return predictTime == null ? 15 : Integer.parseInt(predictTime);
    }

    private LocalTime getNextArrivalTime (List<LocalTime> localTimes, LocalTime currentTime) {
        for(int i=0; i<localTimes.size()-1; i++) {
            if(localTimes.get(i).isAfter(currentTime) && localTimes.get(i+1).isBefore(currentTime)) {
                return localTimes.get(i+1);
            }
        }
        return currentTime.plusMinutes(10); // 사이 시간이 없는 경우 기본 대기 시간
    }

}
