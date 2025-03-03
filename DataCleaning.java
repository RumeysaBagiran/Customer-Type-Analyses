package com.customeranalyses;

import com.opencsv.*;
import java.io.*;
import java.util.*;

import java.time.Year; // Yıl hesaplaması için

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DataCleaning {
    private static final String INPUT_FILE = "src/main/resources/superstore_data.csv";
    private static final String OUTPUT_FILE = "src/main/resources/cleaned_data.csv";
    private static List<String[]> data = new ArrayList<>();

    public static void main(String[] args) {
        readData(); // Veriyi oku
        fillMissingValues(); // Eksik verileri doldur
        removeUnnecessaryColumns(); // Gereksiz sütunları kaldır
        saveCleanedData(); // Temizlenmiş veriyi kaydet
    }

    // 📌 1️⃣ CSV dosyasını oku ve bellekte tut
    public static void readData() {
        try (BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE));
             CSVReader reader = new CSVReader(br)) {
            data = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 📌 2️⃣ Eksik verileri doldur (sadece bellekte değiştir)
    public static void fillMissingValues() {
        int incomeIndex = Arrays.asList(data.get(0)).indexOf("Income");
        double totalIncome = 0;
        int incomeCount = 0;

        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (!row[incomeIndex].isEmpty()) {
                totalIncome += Double.parseDouble(row[incomeIndex]);
                incomeCount++;
            }
        }

        double meanIncome = totalIncome / incomeCount;
        
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row[incomeIndex].isEmpty()) {
                row[incomeIndex] = String.valueOf(meanIncome);
            }
        }
    }

    // 📌 3️⃣ Gereksiz sütunları kaldır (bellekte değiştir)
    public static void removeUnnecessaryColumns() {
        List<String> headerList = new ArrayList<>(Arrays.asList(data.get(0)));

        int responseIndex = headerList.indexOf("Response");
        int complainIndex = headerList.indexOf("Complain");

        if (responseIndex != -1) headerList.remove(responseIndex);
        if (complainIndex != -1) headerList.remove(complainIndex - (responseIndex < complainIndex ? 1 : 0));

        List<String[]> cleanedData = new ArrayList<>();
        cleanedData.add(headerList.toArray(new String[0]));

        for (int i = 1; i < data.size(); i++) {
            List<String> row = new ArrayList<>(Arrays.asList(data.get(i)));
            if (responseIndex != -1) row.remove(responseIndex);
            if (complainIndex != -1) row.remove(complainIndex - (responseIndex < complainIndex ? 1 : 0));
            cleanedData.add(row.toArray(new String[0]));
        }

        data = cleanedData;
    }
   

    public static void calculateAgeAndRemoveYearBirth() {
        List<String> headerList = new ArrayList<>(Arrays.asList(data.get(0)));

        // "Year_Birth" sütununun indeksini bul
        int yearBirthIndex = headerList.indexOf("Year_Birth");

        if (yearBirthIndex == -1) {
            System.out.println("⚠️ 'Year_Birth' sütunu bulunamadı, işlem yapılmadı.");
            return;
        }

        // Yeni başlığa "Age" ekleyelim ve "Year_Birth" sütununu silelim
        headerList.add("Age");
        headerList.remove(yearBirthIndex);

        List<String[]> updatedData = new ArrayList<>();
        updatedData.add(headerList.toArray(new String[0]));

        int currentYear = Year.now().getValue(); // Mevcut yılı al

        // Satırları güncelle
        for (int i = 1; i < data.size(); i++) {
            List<String> row = new ArrayList<>(Arrays.asList(data.get(i)));
            
            // Yaşı hesapla
            int birthYear = Integer.parseInt(row.get(yearBirthIndex));
            int age = currentYear - birthYear;
            
            // "Age" sütununu ekleyelim
            row.add(String.valueOf(age));
            
            // "Year_Birth" sütununu kaldır
            row.remove(yearBirthIndex);
            
            updatedData.add(row.toArray(new String[0]));
        }

        data = updatedData; // Güncellenmiş veriyi bellekte sakla
    }
    


    public static void calculateCustomerSinceDaysAndRemoveDtCustomer() {
        List<String> headerList = new ArrayList<>(Arrays.asList(data.get(0)));

        // "Dt_Customer" sütununun indeksini bul
        int dtCustomerIndex = headerList.indexOf("Dt_Customer");

        if (dtCustomerIndex == -1) {
            System.out.println("⚠️ 'Dt_Customer' sütunu bulunamadı, işlem yapılmadı.");
            return;
        }

        // Yeni başlığa "Customer_Since_Days" ekleyelim ve "Dt_Customer" sütununu silelim
        headerList.add("Customer_Since_Days");
        headerList.remove(dtCustomerIndex);

        List<String[]> updatedData = new ArrayList<>();
        updatedData.add(headerList.toArray(new String[0]));

        // Tarih formatı: "6/16/2014" gibi olduğundan bunu Java formatına çevireceğiz
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        LocalDate today = LocalDate.now(); // Bugünün tarihi (2025-02-23)

        // Satırları güncelle
        for (int i = 1; i < data.size(); i++) {
            List<String> row = new ArrayList<>(Arrays.asList(data.get(i)));

            // Tarihi gün cinsine çevir
            LocalDate customerDate = LocalDate.parse(row.get(dtCustomerIndex), formatter);
            long daysSince = ChronoUnit.DAYS.between(customerDate, today);

            // "Customer_Since_Days" sütununu ekleyelim
            row.add(String.valueOf(daysSince));

            // "Dt_Customer" sütununu kaldır
            row.remove(dtCustomerIndex);

            updatedData.add(row.toArray(new String[0]));
        }

        data = updatedData; // Güncellenmiş veriyi bellekte sakla
    }

    public static void convertCategoricalToNumeric() {
        List<String> headerList = new ArrayList<>(Arrays.asList(data.get(0)));

        // "Marital_Status" ve "Education" sütunlarının indekslerini bul
        int maritalStatusIndex = headerList.indexOf("Marital_Status");
        int educationIndex = headerList.indexOf("Education");

        if (maritalStatusIndex == -1 || educationIndex == -1) {
            System.out.println("⚠️ 'Marital_Status' veya 'Education' sütunu bulunamadı, işlem yapılmadı.");
            return;
        }

        // Yeni başlıkları en sona ekleyelim
        headerList.add("Marital_Status_Int");
        headerList.add("Education_Int");

        List<String[]> updatedData = new ArrayList<>();
        
        // Yeni başlığı ekleyip eski sütunları kaldırmadan önce tutalım
        List<String> cleanedHeader = new ArrayList<>(headerList);
        cleanedHeader.remove("Marital_Status");
        cleanedHeader.remove("Education");
        updatedData.add(cleanedHeader.toArray(new String[0]));

        // Satırları güncelle
        for (int i = 1; i < data.size(); i++) {
            List<String> row = new ArrayList<>(Arrays.asList(data.get(i)));

            // Marital_Status dönüşümü
            String maritalStatus = row.get(maritalStatusIndex);
            int maritalStatusInt;
            if (maritalStatus.equalsIgnoreCase("Married")) {
                maritalStatusInt = 1;
            } else if (maritalStatus.equalsIgnoreCase("Together")) {
                maritalStatusInt = 2; // In a Relationship
            } else {
                maritalStatusInt = 0; // Single (Yolo, Widow, Divorced)
            }

            // Education dönüşümü
            String education = row.get(educationIndex);
            int educationInt;
            switch (education) {
                case "Basic":
                    educationInt = 0;
                    break;
                case "Graduation":
                    educationInt = 1;
                    break;
                case "Master":
                    educationInt = 2;
                    break;
                case "PhD":
                    educationInt = 3;
                    break;
                case "2n Cycle":
                    educationInt = 4;
                    break;
                default:
                    educationInt = -1; // Bilinmeyen değer
            }

            // Yeni sütunları ekleyelim
            row.add(String.valueOf(maritalStatusInt));
            row.add(String.valueOf(educationInt));

            // **ÖNEMLİ**: Şimdi eski sütunları kaldıracağız ama önce hangi indexin değişeceğini iyi takip edelim
            if (educationIndex > maritalStatusIndex) {
                row.remove(educationIndex); // Önce Education kaldırılırsa index kaymaz
                row.remove(maritalStatusIndex);
            } else {
                row.remove(maritalStatusIndex); // Önce Marital_Status kaldırılırsa index kaymaz
                row.remove(educationIndex);
            }

            updatedData.add(row.toArray(new String[0]));
        }

        data = updatedData; // Güncellenmiş veriyi bellekte sakla
    }
    public static void createTotalSpentFeature() {
        List<String> headerList = new ArrayList<>(Arrays.asList(data.get(0)));

        // Toplam harcama sütunlarını belirleyelim
        int mntWinesIndex = headerList.indexOf("MntWines");
        int mntFruitsIndex = headerList.indexOf("MntFruits");
        int mntMeatIndex = headerList.indexOf("MntMeatProducts");
        int mntFishIndex = headerList.indexOf("MntFishProducts");
        int mntSweetIndex = headerList.indexOf("MntSweetProducts");
        int mntGoldIndex = headerList.indexOf("MntGoldProds");

        if (mntWinesIndex == -1 || mntFruitsIndex == -1 || mntMeatIndex == -1 || 
            mntFishIndex == -1 || mntSweetIndex == -1 || mntGoldIndex == -1) {
            System.out.println("⚠️ Harcama sütunları eksik, işlem yapılmadı.");
            return;
        }

        // Yeni başlığa "TotalSpent" ekleyelim
        headerList.add("TotalSpent");

        List<String[]> updatedData = new ArrayList<>();
        updatedData.add(headerList.toArray(new String[0]));

        // Satırları güncelle
        for (int i = 1; i < data.size(); i++) {
            List<String> row = new ArrayList<>(Arrays.asList(data.get(i)));

            // Toplam harcamayı hesapla
            double totalSpent = Double.parseDouble(row.get(mntWinesIndex)) +
                                Double.parseDouble(row.get(mntFruitsIndex)) +
                                Double.parseDouble(row.get(mntMeatIndex)) +
                                Double.parseDouble(row.get(mntFishIndex)) +
                                Double.parseDouble(row.get(mntSweetIndex)) +
                                Double.parseDouble(row.get(mntGoldIndex));

            // Yeni "TotalSpent" sütununu ekleyelim
            row.add(String.valueOf(totalSpent));

            updatedData.add(row.toArray(new String[0]));
        }

        data = updatedData; // Güncellenmiş veriyi bellekte sakla
    }

    public static List<String[]> getCleanedData() {
        return data;
    }

    public static void setCleanedData(List<String[]> updatedData) {
        data = updatedData;
    }

    // 📌 4️⃣ Tüm işlemler bittikten sonra tek seferde temizlenmiş veriyi kaydet
    public static void saveCleanedData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_FILE));
             CSVWriter writer = new CSVWriter(bw)) {
            writer.writeAll(data);
            System.out.println("✅ Tüm veri temizleme işlemleri tamamlandı ve dosya başarıyla kaydedildi!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
