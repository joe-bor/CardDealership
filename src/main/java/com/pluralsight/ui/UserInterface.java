package com.pluralsight.ui;

import com.pluralsight.*;
import com.pluralsight.data_access.DealershipDAO;
import com.pluralsight.data_access.LeaseContractDAO;
import com.pluralsight.data_access.SalesContractDAO;
import com.pluralsight.data_access.VehicleDAO;
import com.pluralsight.model.Dealership;
import com.pluralsight.model.Vehicle;
import com.pluralsight.model.contract.Contract;
import com.pluralsight.model.contract.LeaseContract;
import com.pluralsight.model.contract.SalesContract;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserInterface {

    private Dealership dealership;
    private static final Scanner SCANNER = new Scanner(System.in);
    private final VehicleDAO VEHICLE_DAO = new VehicleDAO();
    private final DealershipDAO DEALERSHIP_DAO = new DealershipDAO();
    private final SalesContractDAO SALES_CONTRACT_DAO = new SalesContractDAO();
    private final LeaseContractDAO LEASE_CONTRACT_DAO = new LeaseContractDAO();

    public void display() {
        init();

        boolean isRunning = true;
        do {
            System.out.println(String.format("""
                    
                    ========================================================
                                    MAIN MENU -  %s
                    ========================================================
                    What would you to do?
                    1 - Find vehicles within a price range
                    2 - Find vehicles by make/model
                    3 - Find vehicles by year range
                    4 - Find vehicles by color
                    5 - Find vehicles by mileage range
                    6 - Find vehicles by type (car, truck, SUV, van)
                    7 - List ALL Vehicles
                    8 - Add a vehicle
                    9 - Remove a vehicle
                    10 - Buy or Lease a vehicle
                    88 - Switch Dealership
                    99 - Quit
                    00 - Admin
                    
                    """, this.dealership.getName()));
            String answer = SCANNER.nextLine().trim();

            switch (answer) {
                case "1" -> processGetByPriceRequest();
                case "2" -> processGetByMakeModelRequest();
                case "3" -> processGetByYearRequest();
                case "4" -> processGetByColorRequest();
                case "5" -> processGetByMileageRequest();
                case "6" -> processGetByVehicleTypeRequest();
                case "7" -> processAllVehiclesRequest();
                case "8" -> processAddVehicleRequest();
                case "9" -> processRemoveVehicleRequest();
                case "10" -> processVehicleContract();
                case "88" -> init();
                case "99" -> {
                    isRunning = false;
                    System.out.println("Terminating...");
                }
                case "00" -> processAdminRequest();
                default -> System.err.println("Invalid Option. Try again!");
            }

        } while (isRunning);
        SCANNER.close();
    }

    private void processAdminRequest() {
        AdminInterface adminInterface = new AdminInterface();
        adminInterface.display();
    }

    private void processVehicleContract() {
        System.out.print("\nPlease provide the VIN of the vehicle you would like to buy/lease (a vehicle over 3 yrs old can't be leased) \n");
        int vin = SCANNER.nextInt();
        SCANNER.nextLine();

        Optional<Vehicle> matchingVehicle = this.dealership.getInventory().stream().filter(vehicle -> vehicle.getVin() == vin).findFirst();
        matchingVehicle.ifPresentOrElse(vehicle -> {
                    System.out.println("Matching vehicle found");

                    int vehicleAge = LocalDate.now().getYear() - vehicle.getYear();
                    System.out.printf("Would you like to buy %sthis vehicle? \n", vehicleAge > 3 ? "" : "or rent ");

                    String answer = SCANNER.nextLine();
                    System.out.print("What is your name? \n");
                    String name = SCANNER.nextLine();
                    System.out.print("What is your email? \n");
                    String email = SCANNER.nextLine();
                    String todaysDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                    Contract contract = null;
                    if (answer.equalsIgnoreCase("buy")) {
                        System.out.print("Would you like to finance? (true or false) \n");
                        boolean financed = SCANNER.nextBoolean();
                        SCANNER.nextLine();

                        final double SALES_TAX = 5.0;
                        final double RECORDING_FEE = 100.0;
                        final double PROCESSING_FEE = vehicle.getPrice() < 10_000 ? 295.0 : 495.0;

                        contract = new SalesContract(todaysDate, name, email, vehicle, SALES_TAX, RECORDING_FEE, PROCESSING_FEE, financed);
                        SALES_CONTRACT_DAO.createSalesContract((SalesContract) contract);
                    } else {
                        final double EXPECTED_ENDING_VALUE = vehicle.getPrice() * .5;
                        final double LEASE_FEE = 7;

                        contract = new LeaseContract(todaysDate, name, email, vehicle, EXPECTED_ENDING_VALUE, LEASE_FEE);
                        LEASE_CONTRACT_DAO.createLeaseContract((LeaseContract) contract);
                    }

                    processRemoveVehicleRequest2(vehicle);

                }
                , () -> System.out.printf("Vehicle with vin %d not found\n", vin));
    }

    public void processGetByPriceRequest() {
        System.out.print("What is the min price? ");
        double minPrice = SCANNER.nextDouble();
        SCANNER.nextLine();
        System.out.print("What is the max price? ");
        double maxPrice = SCANNER.nextDouble();
        SCANNER.nextLine();

        displayVehicles(VEHICLE_DAO.getVehicleByPriceRange(minPrice, maxPrice));
    }

    public void processGetByMakeModelRequest() {
        System.out.print("What is the make? ");
        String make = SCANNER.nextLine();
        System.out.print("What is the model? ");
        String model = SCANNER.nextLine();

        displayVehicles(VEHICLE_DAO.getVehicleByMakeModel(make, model));
    }

    public void processGetByYearRequest() {
        System.out.print("What is the min year? ");
        int minYear = SCANNER.nextInt();
        SCANNER.nextLine();
        System.out.print("What is the max year? ");
        int maxYear = SCANNER.nextInt();
        SCANNER.nextLine();

        displayVehicles(VEHICLE_DAO.getVehicleByYearRange(minYear, maxYear));
    }

    public void processGetByColorRequest() {
        System.out.print("What is the color? ");
        String color = SCANNER.nextLine();

        displayVehicles(VEHICLE_DAO.getVehicleByColor(color));
    }

    public void processGetByMileageRequest() {
        System.out.print("What is the min mileage? ");
        int minMileage = SCANNER.nextInt();
        SCANNER.nextLine();
        System.out.print("What is the max mileage? ");
        int maxMileage = SCANNER.nextInt();
        SCANNER.nextLine();

        displayVehicles(VEHICLE_DAO.getVehicleByMileageRange(minMileage, maxMileage));
    }

    public void processGetByVehicleTypeRequest() {
        System.out.print("What is the vehicle type? ");
        String vehicleType = SCANNER.nextLine();

        displayVehicles(VEHICLE_DAO.getVehicleByType(vehicleType));
    }

    public void processGetAllVehiclesRequest() {
        displayVehicles(this.dealership.getAllVehicles());
    }

    public void processAddVehicleRequest() {
        System.out.print("What is the vehicle vin? ");
        int vin = SCANNER.nextInt();
        SCANNER.nextLine();
        System.out.print("What is the vehicle year? ");
        int year = SCANNER.nextInt();
        SCANNER.nextLine();
        System.out.print("What is the vehicle make? ");
        String make = SCANNER.nextLine();
        System.out.print("What is the vehicle model? ");
        String model = SCANNER.nextLine();
        System.out.print("What is the vehicle type? ");
        String vehicleType = SCANNER.nextLine();
        System.out.print("What is the vehicle color? ");
        String color = SCANNER.nextLine();
        System.out.print("What is the vehicle mileage? ");
        int mileage = SCANNER.nextInt();
        SCANNER.nextLine();
        System.out.print("What is the vehicle price? ");
        double price = SCANNER.nextDouble();
        SCANNER.nextLine();

        System.out.println("Adding new vehicle to inventory... ");
        VEHICLE_DAO.addVehicle(vin, year, make, model, vehicleType, color, mileage, price);
        System.out.println("Successfully added new vehicle!\n");
    }

    public void processRemoveVehicleRequest() {
        System.out.println("Provide the VIN of the vehicle you want to remove");
        int vin = SCANNER.nextInt();
        SCANNER.nextLine();
        VEHICLE_DAO.removeVehicle(vin);
    }

    public void processRemoveVehicleRequest2(Vehicle vehicle) {
        this.dealership.getInventory().remove(vehicle);
    }

    private void init() {
        this.dealership = pickDealership();
    }

    private void displayVehicles(List<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            System.out.println(v);
        }
    }

    public void processAllVehiclesRequest() {
        displayVehicles(this.dealership.getAllVehicles());
    }

    private Dealership pickDealership() {
        var dealerships = DEALERSHIP_DAO.getAllDealerships();

        System.out.println("Pick one from the list of dealerships:");
        dealerships.forEach(dealership -> {
            System.out.printf("""
                    [%d] - %s
                    """, dealership.getId(), dealership.getName());
        });

        int id = SCANNER.nextInt();
        SCANNER.nextLine();

        return DEALERSHIP_DAO.getDealershipByID(id);
    }
}
