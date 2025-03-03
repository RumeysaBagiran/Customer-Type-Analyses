package com.customeranalyses;

public class Main {
    public static void main(String[] args) {
        DataCleaning.readData(); // Veriyi oku
        DataCleaning.fillMissingValues(); // Eksik verileri doldur
        DataCleaning.removeUnnecessaryColumns(); // Gereksiz sütunları kaldır
        DataCleaning.calculateAgeAndRemoveYearBirth(); // Yaş hesapla ve Year_Birth sütununu sil
        DataCleaning.calculateCustomerSinceDaysAndRemoveDtCustomer(); // Müşteri kayıt gün sayısını hesapla ve Dt_Customer'ı sil
        DataCleaning.convertCategoricalToNumeric(); // Education ve Marital_Status değişkenlerini sayısal hale getir
        DataCleaning.createTotalSpentFeature(); // Yeni "TotalSpent" özelliğini oluştur
        
        // Temizlenmiş veriyi CustomerSegmentation'a gönder
        CustomerSegmentation.loadData(DataCleaning.getCleanedData());

        // Veri ölçekleme işlemini yap
        CustomerSegmentation.scaleFeatures();

        // İşlenmiş veriyi tekrar al ve kaydet
        DataCleaning.setCleanedData(CustomerSegmentation.getData());
        DataCleaning.saveCleanedData();

        // 📊 Elbow Method ile optimum küme sayısını bul
        CustomerSegmentation.findOptimalClusters();

        // 🔥 K-Means Kümeleme işlemini gerçekleştir (K = 6)
        CustomerSegmentation.performClustering(6);

        // Sonuçları kaydet
        DataCleaning.setCleanedData(CustomerSegmentation.getData());
        DataCleaning.saveCleanedData();



        // 📊 Online alışveriş yapan segmentleri analiz et
        CustomerSegmentation.analyzeOnlineShoppers();
        
     // 📊 Medeni Durum Küme Analizi
        CustomerSegmentation.analyzeMaritalStatusInClusters();

        
        // 📊 Detaylı Küme Analizini Yap
        CustomerSegmentation.detailedSegmentAnalysis();

        // 📊 Online alışveriş segmentlerini görselleştir
        CustomerVisualization.plotOnlineShoppingByCluster();
        CustomerVisualization.plotClusterAverages(CustomerSegmentation.getData());  
        CustomerVisualization.plotMaritalStatusByCluster();

    }
}




	




	




