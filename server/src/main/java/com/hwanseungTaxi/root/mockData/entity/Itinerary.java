package com.hwanseungTaxi.root.mockData.entity;

import java.util.List;

public class Itinerary {

    public int totalTime; // 초 단위
    public int transferCount; // 환승 횟수
    public int totalWalkDistance; // m 단위
    public int totalDistance; // m 단위
    public int totalWalkTime; // 초 단위
    public Fare fare;
    public List<Leg> legs;
    public int pathType;

}
