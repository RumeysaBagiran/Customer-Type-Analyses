package com.customeranalyses;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import java.util.*;

import java.util.List;

import org.knowm.xchart.SwingWrapper; // pasta grafiği için


public class CustomerVisualization {  // 🔴 SINIF TANIMI BURADA BAŞLAMALI

    public static void plotClusterAverages(List<String[]> data) {
        List<String> headerList = new ArrayList<>(List.of(data.get(0)));

        // Analiz edilecek özellikler
        List<String> featuresToAnalyze = List.of("Income", "TotalSpent", "Age", "Customer_Since_Days");
        int clusterIndex = headerList.indexOf("Cluster_ID");

        // Sütun indexlerini belirle
        List<Integer> indicesToAnalyze = new ArrayList<>();
        for (String feature : featuresToAnalyze) {
            if (headerList.contains(feature)) {
                indicesToAnalyze.add(headerList.indexOf(feature));
            }
        }

        // Küme verilerini gruplama
        Map<Integer, List<double[]>> clusterDataMap = new HashMap<>();
        for (int i = 1; i < data.size(); i++) {
            int clusterID = Integer.parseInt(data.get(i)[clusterIndex]);
            double[] values = new double[indicesToAnalyze.size()];
            
            for (int j = 0; j < indicesToAnalyze.size(); j++) {
                values[j] = Double.parseDouble(data.get(i)[indicesToAnalyze.get(j)]);
            }

            clusterDataMap.putIfAbsent(clusterID, new ArrayList<>());
            clusterDataMap.get(clusterID).add(values);
        }

        // Küme ortalamalarını hesapla
        List<Integer> clusterIDs = new ArrayList<>(clusterDataMap.keySet());
        Collections.sort(clusterIDs);

        Map<String, List<Double>> featureAverages = new HashMap<>();
        for (String feature : featuresToAnalyze) {
            featureAverages.put(feature, new ArrayList<>());
        }

        for (Integer clusterID : clusterIDs) {
            List<double[]> clusterPoints = clusterDataMap.get(clusterID);
            int clusterSize = clusterPoints.size();

            double[] featureSums = new double[indicesToAnalyze.size()];
            for (double[] point : clusterPoints) {
                for (int j = 0; j < indicesToAnalyze.size(); j++) {
                    featureSums[j] += point[j];
                }
            }

            for (int j = 0; j < indicesToAnalyze.size(); j++) {
                featureAverages.get(featuresToAnalyze.get(j)).add(featureSums[j] / clusterSize);
            }
        }

        // Grafik oluşturma
        for (String feature : featuresToAnalyze) {
            CategoryChart chart = new CategoryChartBuilder()
                    .width(800).height(600)
                    .title("Cluster Averages - " + feature)
                    .xAxisTitle("Cluster ID")
                    .yAxisTitle("Average Value")
                 //   .theme(Styler.ChartTheme.Matlab)
                    .theme(Styler.ChartTheme.GGPlot2)
                    .build();

            chart.addSeries(feature, clusterIDs, featureAverages.get(feature));
         


            // Grafiği göster
            new SwingWrapper<>(chart).displayChart();
        }
    }
    public static void plotOnlineShoppingByCluster() {
        List<String> headerList = new ArrayList<>(List.of(CustomerSegmentation.getData().get(0)));

        int clusterIndex = headerList.indexOf("Cluster_ID");
        int webPurchasesIndex = headerList.indexOf("NumWebPurchases");

        // Küme bazında online alışveriş ortalamalarını hesapla
        Map<Integer, Double> onlineShoppingAverages = new HashMap<>();
        Map<Integer, Integer> clusterCounts = new HashMap<>();

        for (int i = 1; i < CustomerSegmentation.getData().size(); i++) {
            int clusterID = Integer.parseInt(CustomerSegmentation.getData().get(i)[clusterIndex]);
            double webPurchases = Double.parseDouble(CustomerSegmentation.getData().get(i)[webPurchasesIndex]);

            onlineShoppingAverages.put(clusterID, onlineShoppingAverages.getOrDefault(clusterID, 0.0) + webPurchases);
            clusterCounts.put(clusterID, clusterCounts.getOrDefault(clusterID, 0) + 1);
        }

        List<Integer> clusterIDs = new ArrayList<>(onlineShoppingAverages.keySet());
        Collections.sort(clusterIDs);
        List<Double> avgWebPurchases = new ArrayList<>();
        
        for (int clusterID : clusterIDs) {
            avgWebPurchases.add(onlineShoppingAverages.get(clusterID) / clusterCounts.get(clusterID));
        }

        // Grafik oluştur
        CategoryChart chart = new CategoryChartBuilder()
                .width(800).height(600)
                .title("Kümelere Göre Ortalama Online Alışveriş")
                .xAxisTitle("Cluster ID")
                .yAxisTitle("Ortalama Online Alışveriş")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setLabelsVisible(true);


        chart.addSeries("Online Alışveriş", clusterIDs, avgWebPurchases);

        // Grafiği göster
        new SwingWrapper<>(chart).displayChart();
    }
    
 // ✅ Medeni Durumu Küme Bazında Gösteren Pasta Grafikleri
    public static void plotMaritalStatusByCluster() {
        List<String> headerList = new ArrayList<>(List.of(CustomerSegmentation.getData().get(0)));
        int clusterIndex = headerList.indexOf("Cluster_ID");
        int maritalStatusIndex = headerList.indexOf("Marital_Status_Int");

        // Küme bazında medeni durum sayılarını saklamak için harita (Map) oluştur
        Map<Integer, Map<String, Integer>> clusterMaritalData = new HashMap<>();

        for (int i = 1; i < CustomerSegmentation.getData().size(); i++) {
            int clusterID = Integer.parseInt(CustomerSegmentation.getData().get(i)[clusterIndex]);
            int status = Integer.parseInt(CustomerSegmentation.getData().get(i)[maritalStatusIndex]);

            clusterMaritalData.putIfAbsent(clusterID, new HashMap<>());
            Map<String, Integer> maritalCounts = clusterMaritalData.get(clusterID);

            String statusStr = (status == 1) ? "Married" : (status == 0) ? "Single" : "Together";
            maritalCounts.put(statusStr, maritalCounts.getOrDefault(statusStr, 0) + 1);
        }

        // 🎨 Her küme için pasta grafiği oluştur
        for (Map.Entry<Integer, Map<String, Integer>> entry : clusterMaritalData.entrySet()) {
            int clusterID = entry.getKey();
            Map<String, Integer> maritalCounts = entry.getValue();

            PieChart chart = new PieChartBuilder()
                    .width(800)
                    .height(600)
                    .title("Küme " + clusterID + " - Medeni Durum Dağılımı")
                    .build();

            for (Map.Entry<String, Integer> maritalEntry : maritalCounts.entrySet()) {
                chart.addSeries(maritalEntry.getKey(), maritalEntry.getValue());
            }

            // Grafiği göster
            new SwingWrapper<>(chart).displayChart();
        }
    }
}