package com.customeranalyses;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import java.util.*;

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import java.util.ArrayList;
import java.util.List;

import smile.clustering.KMeans;
import java.util.ArrayList;
import java.util.List;

public class CustomerSegmentation {
    private static List<String[]> data = new ArrayList<>();

    public static void loadData(List<String[]> cleanedData) {
        data = cleanedData;
    }

    public static void scaleFeatures() {
        List<String> headerList = new ArrayList<>(Arrays.asList(data.get(0)));

        // Ölçeklenecek sütunlar (Gelir, Harcama, Yaş, vb.)
        List<String> columnsToScale = Arrays.asList("Income", "TotalSpent", "Age", "Customer_Since_Days");

        // İlgili sütunların indexlerini bul
        List<Integer> indicesToScale = new ArrayList<>();
        for (String col : columnsToScale) {
            if (headerList.contains(col)) {
                indicesToScale.add(headerList.indexOf(col));
            }
        }

        if (indicesToScale.isEmpty()) {
            System.out.println("⚠️ Ölçeklenecek sütun bulunamadı, işlem yapılmadı.");
            return;
        }

        // Veri içeriğini (başlık hariç) sayısal değerlere çevirme
        double[][] values = new double[data.size() - 1][indicesToScale.size()];
        for (int i = 1; i < data.size(); i++) {
            for (int j = 0; j < indicesToScale.size(); j++) {
                values[i - 1][j] = Double.parseDouble(data.get(i)[indicesToScale.get(j)]);
            }
        }

        // Ortalama ve Standart Sapma Hesaplama
        for (int j = 0; j < indicesToScale.size(); j++) {
            double[] columnValues = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                columnValues[i] = values[i][j];
            }

            double mean = new Mean().evaluate(columnValues);
            double stdDev = new StandardDeviation().evaluate(columnValues);

            // Veriyi ölçekleme (StandardScaler: (x - mean) / std)
            for (int i = 0; i < values.length; i++) {
                if (stdDev != 0) {
                    values[i][j] = (columnValues[i] - mean) / stdDev;
                } else {
                    values[i][j] = 0; // Eğer standart sapma 0 ise, tüm değerleri 0 yap.
                }
            }
        }

        // Güncellenmiş değerleri tekrar data listesine ekleyelim
        for (int i = 1; i < data.size(); i++) {
            for (int j = 0; j < indicesToScale.size(); j++) {
                data.get(i)[indicesToScale.get(j)] = String.valueOf(values[i - 1][j]);
            }
        }

        System.out.println("✅ Sayısal veriler başarıyla ölçeklendi (StandardScaler kullanıldı).");
    }

    public static List<String[]> getData() {
        return data;
    }
    
    public static void findOptimalClusters() {
        List<String> headerList = new ArrayList<>(List.of(data.get(0)));

        // Kümeleme için kullanılacak sütunları seçelim
        List<String> featuresToCluster = List.of("Income", "TotalSpent", "Age", "Customer_Since_Days", "Marital_Status_Int");


        // Sütun indexlerini belirleyelim
        List<Integer> indicesToCluster = new ArrayList<>();
        for (String feature : featuresToCluster) {
            if (headerList.contains(feature)) {
                indicesToCluster.add(headerList.indexOf(feature));
            }
        }

        if (indicesToCluster.isEmpty()) {
            System.out.println("⚠️ Kümeleme için kullanılacak sütunlar bulunamadı, işlem yapılamadı.");
            return;
        }

        // Veriyi kümeleme için uygun formata çevir
        double[][] clusterData = new double[data.size() - 1][indicesToCluster.size()];
        for (int i = 1; i < data.size(); i++) {
            for (int j = 0; j < indicesToCluster.size(); j++) {
                clusterData[i - 1][j] = Double.parseDouble(data.get(i)[indicesToCluster.get(j)]);
            }
        }

        // Elbow Method: Farklı küme sayıları için "distortion score" hesaplayacağız
        System.out.println("🔍 Optimum küme sayısını belirlemek için Elbow Method uygulanıyor...");
        for (int k = 2; k <= 10; k++) { // Küme sayısını 2'den 10'a kadar deneyelim
            KMeans km = KMeans.fit(clusterData, k);
            System.out.println("K = " + k + " için Distortion Score: " + km.distortion);
        }

        System.out.println("📊 En iyi K değerini seçmek için en düşük noktada kırılma (Elbow Point) olup olmadığına bakın!");
    }
    
    //k-means algoritmasını çalıştırıyoruz.
    
    public static void performClustering(int k) {
        List<String> headerList = new ArrayList<>(List.of(data.get(0)));

        // Kümeleme için kullanılacak sütunları seçelim
        List<String> featuresToCluster = List.of("Income", "TotalSpent", "Age", "Customer_Since_Days");

        // Sütun indexlerini belirleyelim
        List<Integer> indicesToCluster = new ArrayList<>();
        for (String feature : featuresToCluster) {
            if (headerList.contains(feature)) {
                indicesToCluster.add(headerList.indexOf(feature));
            }
        }

        if (indicesToCluster.isEmpty()) {
            System.out.println("⚠️ Kümeleme için kullanılacak sütunlar bulunamadı, işlem yapılamadı.");
            return;
        }

        // Veriyi kümeleme için uygun formata çevir
        double[][] clusterData = new double[data.size() - 1][indicesToCluster.size()];
        for (int i = 1; i < data.size(); i++) {
            for (int j = 0; j < indicesToCluster.size(); j++) {
                clusterData[i - 1][j] = Double.parseDouble(data.get(i)[indicesToCluster.get(j)]);
            }
        }

        // K-Means ile K = 6 kullanarak kümeleri belirleyelim
        KMeans km = KMeans.fit(clusterData, k);
        int[] clusterLabels = km.y; // Müşterilere atanan küme numaraları

        // Yeni sütunu başlığa ekleyelim
        headerList.add("Cluster_ID");
        data.set(0, headerList.toArray(new String[0]));

        // Her müşteriye ait Cluster ID'yi ekleyelim
        for (int i = 1; i < data.size(); i++) {
            List<String> row = new ArrayList<>(List.of(data.get(i)));
            row.add(String.valueOf(clusterLabels[i - 1])); // Cluster ID ekleme
            data.set(i, row.toArray(new String[0]));
        }

        System.out.println("✅ K-Means Kümeleme tamamlandı! Müşterilere Cluster_ID atandı.");
    }
    // Her kümedeki müşteri sayısını bulacağız.
   //  Her küme için ortalama gelir, yaş, toplam harcama gibi istatistikleri hesaplayacağız.
   //  Bu sayede her kümenin müşteri profiline dair bilgi sahibi olacağız.
    public static void analyzeClusters() {
        List<String> headerList = new ArrayList<>(List.of(data.get(0)));

        // Hangi özellikleri analiz edeceğiz?
        List<String> featuresToAnalyze = List.of("Income", "TotalSpent", "Age", "Customer_Since_Days");
        int clusterIndex = headerList.indexOf("Cluster_ID");

        // Sütun indexlerini belirle
        List<Integer> indicesToAnalyze = new ArrayList<>();
        for (String feature : featuresToAnalyze) {
            if (headerList.contains(feature)) {
                indicesToAnalyze.add(headerList.indexOf(feature));
            }
        }

        // Küme sayılarını bul
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

        // Küme istatistiklerini hesapla
        System.out.println("\n📊 **Küme Analizi Başlıyor...**");
        for (Map.Entry<Integer, List<double[]>> entry : clusterDataMap.entrySet()) {
            int clusterID = entry.getKey();
            List<double[]> clusterPoints = entry.getValue();
            int clusterSize = clusterPoints.size();

            double[] featureSums = new double[indicesToAnalyze.size()];
            
            for (double[] point : clusterPoints) {
                for (int j = 0; j < indicesToAnalyze.size(); j++) {
                    featureSums[j] += point[j];
                }
            }

            // Ortalama değerleri hesapla
            System.out.println("\n🔹 **Küme " + clusterID + " (Müşteri Sayısı: " + clusterSize + ")**");
            for (int j = 0; j < indicesToAnalyze.size(); j++) {
                double mean = featureSums[j] / clusterSize;
                System.out.println("   - " + featuresToAnalyze.get(j) + " (Ortalama): " + String.format("%.2f", mean));
            }
        }
        System.out.println("\n✅ Küme analizi tamamlandı!");
    }

    public static void detailedSegmentAnalysis() {
        List<String> headerList = new ArrayList<>(List.of(data.get(0)));

        // Analiz edilecek sütunlar
        List<String> featuresToAnalyze = List.of("Income", "TotalSpent", "Age", "Customer_Since_Days", 
                                                  "NumWebPurchases", "NumStorePurchases", "NumDealsPurchases");

        int clusterIndex = headerList.indexOf("Cluster_ID");

        // Sütun indexlerini belirleyelim
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

        // Küme analizini yap
        System.out.println("\n📊 **Detaylı Küme Analizi Başlıyor...**");
        for (Map.Entry<Integer, List<double[]>> entry : clusterDataMap.entrySet()) {
            int clusterID = entry.getKey();
            List<double[]> clusterPoints = entry.getValue();
            int clusterSize = clusterPoints.size();

            double[] featureSums = new double[indicesToAnalyze.size()];
            
            for (double[] point : clusterPoints) {
                for (int j = 0; j < indicesToAnalyze.size(); j++) {
                    featureSums[j] += point[j];
                }
            }

            System.out.println("\n🔹 **Küme " + clusterID + " (Müşteri Sayısı: " + clusterSize + ")**");
            for (int j = 0; j < indicesToAnalyze.size(); j++) {
                double mean = featureSums[j] / clusterSize;
                System.out.println("   - " + featuresToAnalyze.get(j) + " (Ortalama): " + String.format("%.2f", mean));
            }

            // ** Ekstra yorum ekleme **
            double avgWebPurchases = featureSums[featuresToAnalyze.indexOf("NumWebPurchases")] / clusterSize;
            double avgStorePurchases = featureSums[featuresToAnalyze.indexOf("NumStorePurchases")] / clusterSize;
            double avgDeals = featureSums[featuresToAnalyze.indexOf("NumDealsPurchases")] / clusterSize;

            if (avgWebPurchases > avgStorePurchases) {
                System.out.println("   📌 **Bu küme online alışverişi daha fazla tercih ediyor.**");
            } else if (avgWebPurchases == avgStorePurchases) {
                System.out.println("   🔄 **Bu küme hem online hem mağaza alışverişini eşit seviyede yapıyor.**");
            } else {
                System.out.println("   🏬 **Bu küme mağaza alışverişini daha fazla tercih ediyor.**");
            }


            if (avgDeals > 2) {
                System.out.println("   🎯 **Bu küme kampanya indirimlerini aktif olarak kullanıyor.**");
            }
        }
        System.out.println("\n✅ Detaylı küme analizi tamamlandı!");
    }

    public static void analyzeOnlineShoppers() {
        List<String> headerList = new ArrayList<>(List.of(data.get(0)));

        int clusterIndex = headerList.indexOf("Cluster_ID");
        int webPurchasesIndex = headerList.indexOf("NumWebPurchases");

        // Küme bazında online alışveriş ortalamalarını hesapla
        Map<Integer, Double> onlineShoppingAverages = new HashMap<>();
        Map<Integer, Integer> clusterCounts = new HashMap<>();

        for (int i = 1; i < data.size(); i++) {
            int clusterID = Integer.parseInt(data.get(i)[clusterIndex]);
            double webPurchases = Double.parseDouble(data.get(i)[webPurchasesIndex]);

            onlineShoppingAverages.put(clusterID, onlineShoppingAverages.getOrDefault(clusterID, 0.0) + webPurchases);
            clusterCounts.put(clusterID, clusterCounts.getOrDefault(clusterID, 0) + 1);
        }

        // Ortalama hesapla ve kümeleri online alışverişe göre sırala
        List<Map.Entry<Integer, Double>> sortedClusters = new ArrayList<>(onlineShoppingAverages.entrySet());
        sortedClusters.sort((a, b) -> Double.compare(b.getValue() / clusterCounts.get(b.getKey()), a.getValue() / clusterCounts.get(a.getKey())));

        System.out.println("\n📊 **En Fazla Online Alışveriş Yapan Segmentler:**");
        for (Map.Entry<Integer, Double> entry : sortedClusters) {
            int clusterID = entry.getKey();
            double avgWebPurchases = entry.getValue() / clusterCounts.get(clusterID);

            System.out.println("🔹 Küme " + clusterID + " → Ortalama Online Alışveriş: " + String.format("%.2f", avgWebPurchases));
        }
    }
    
    public static void analyzeMaritalStatusInClusters() {
        List<String> headerList = new ArrayList<>(Arrays.asList(data.get(0)));
        
        int clusterIndex = headerList.indexOf("Cluster_ID");
        int maritalIndex = headerList.indexOf("Marital_Status_Int");

        if (clusterIndex == -1 || maritalIndex == -1) {
            System.out.println("⚠️ Küme veya Medeni Durum sütunu bulunamadı!");
            return;
        }

        // Küme ID'lerine göre medeni durumları gruplayalım
        Map<Integer, Map<Integer, Integer>> clusterMaritalMap = new HashMap<>();

        for (int i = 1; i < data.size(); i++) {
            int clusterID = Integer.parseInt(data.get(i)[clusterIndex]);
            int maritalStatus = Integer.parseInt(data.get(i)[maritalIndex]);

            clusterMaritalMap.putIfAbsent(clusterID, new HashMap<>());
            clusterMaritalMap.get(clusterID).put(maritalStatus, 
                clusterMaritalMap.get(clusterID).getOrDefault(maritalStatus, 0) + 1);
        }

        // Analiz Sonuçlarını Yazdır
        System.out.println("\n📊 **Medeni Durum ve Küme Analizi**");
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : clusterMaritalMap.entrySet()) {
            int clusterID = entry.getKey();
            Map<Integer, Integer> maritalCounts = entry.getValue();

            System.out.println("\n🔹 **Küme " + clusterID + " (Toplam Müşteri: " + data.size() + ")**");
            System.out.println("   - 👫 Married: " + maritalCounts.getOrDefault(1, 0));
            System.out.println("   - 💔 Single: " + maritalCounts.getOrDefault(0, 0));
            System.out.println("   - 💞 Together: " + maritalCounts.getOrDefault(2, 0));
        }

        System.out.println("\n✅ Medeni Durum Analizi Tamamlandı!");
    }


    
}

