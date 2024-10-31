package com.pluralsight;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ContractFileManager {

    private final String CONTRACT_FILE_PATH = "contracts.csv";

//    public static void main(String[] args) {
//        ContractFileManager contractFileManager = new ContractFileManager();
//
//        Vehicle vehicleLease = new Vehicle(37846, 2021, "Chevrolet", "Silverado", "truck", "Black", 2750, 31995.00);
//        LeaseContract leaseContract = new LeaseContract("20210928", "Zachary Westly", "zach@texas.com", vehicleLease, 15997.50, 7.0);
//
//        Vehicle vehicleSales = new Vehicle(10112, 1993, "Ford", "Explorer", "SUV", "Red", 525123, 995.00);
//        SalesContract salesContract = new SalesContract("20210928", "Dana Wyatt", "dana@texas.com", vehicleSales, 49.75, 100.00, 295.00, false);
//
//        contractFileManager.saveContract(salesContract);
//        contractFileManager.saveContract(leaseContract);
//    }

    public void saveContract(Contract contract) {
        boolean isSalesContract = contract instanceof SalesContract ? true : false;
        Vehicle vehicleInContract = contract.getVehicleSold();

        // build general contract information
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((isSalesContract ? "SALE" : "LEASE") + "|");
        stringBuilder.append(contract.getContractDate() + "|" + contract.getCustomerName() + "|" + contract.getCustomerEmail() + "|" + vehicleInContract.getVin() + "|" + vehicleInContract.getYear() + "|" + vehicleInContract.getMake() + "|" + vehicleInContract.getModel() + "|" + vehicleInContract.getVehicleType() + "|" + vehicleInContract.getColor() + "|" + vehicleInContract.getOdometer() + "|" + String.format("%.2f", vehicleInContract.getPrice()) + "|");

        // append contract properties specific to a subclass
        if (isSalesContract) {
            double salesTax = ((SalesContract) contract).getSalesTax();
            double recordingFee = ((SalesContract) contract).getRecordingFee();
            double processingFee = ((SalesContract) contract).getProcessingFee();
            double totalPrice = contract.getTotalPrice();
            double monthlyPayment = contract.getMonthlyPayment();

            stringBuilder.append(String.format("%.2f", salesTax) + "|" + String.format("%.2f", recordingFee) + "|" + String.format("%.2f", processingFee) + "|" + String.format("%.2f", totalPrice) + "|" + (((SalesContract) contract).isFinanced() ? "YES" : "NO") + "|" + String.format("%.2f", monthlyPayment));
        } else {
            // if it is not a sales contract, then it is a lease contract
            double expectedEndingValue = ((LeaseContract) contract).getExpectedEndingValue();
            double leaseFeeAmount = (((LeaseContract) contract).getLeaseFee() / 100 * vehicleInContract.getPrice());
            double totalPrice = expectedEndingValue + leaseFeeAmount;
            double monthlyPayment = contract.getMonthlyPayment();

            stringBuilder.append(String.format("%.2f", expectedEndingValue) + "|" + String.format("%.2f", leaseFeeAmount) + "|" + String.format("%.2f", totalPrice) + "|" + String.format("%.2f", monthlyPayment));
        }
        stringBuilder.append("\n");

        // log the files to csv
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(CONTRACT_FILE_PATH, true))) {
            bufferedWriter.append(stringBuilder.toString());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}