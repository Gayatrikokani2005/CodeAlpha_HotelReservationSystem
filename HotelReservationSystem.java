import java.io.*;
import java.time.LocalDate;
import java.util.*;

class Room implements Serializable {
    private int roomNumber;
    private String type;
    private boolean isAvailable;

    public Room(int roomNumber, String type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.isAvailable = true;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type + ") - " + (isAvailable ? "Available" : "Booked");
    }
}

class Booking implements Serializable {
    int bookingId;
    int roomNumber;
    String customerName;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    boolean paymentDone;

    public Booking(int bookingId, int roomNumber, String customerName, LocalDate checkIn, LocalDate checkOut, boolean paymentDone) {
        this.bookingId = bookingId;
        this.roomNumber = roomNumber;
        this.customerName = customerName;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.paymentDone = paymentDone;
    }
}

class Hotel {
    private List<Room> rooms;
    private List<Booking> bookings;
    private final String bookingFile = "bookings.dat";

    public Hotel() {
        rooms = new ArrayList<>();
        bookings = new ArrayList<>();
        initializeRooms();
        loadBookings();
    }

    private void initializeRooms() {
        rooms.add(new Room(101, "Standard"));
        rooms.add(new Room(102, "Standard"));
        rooms.add(new Room(201, "Deluxe"));
        rooms.add(new Room(202, "Deluxe"));
        rooms.add(new Room(301, "Suite"));
    }

    @SuppressWarnings("unchecked")
    private void loadBookings() {
        File file = new File(bookingFile);
        if(file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                bookings = (List<Booking>) ois.readObject();
                for (Booking b : bookings) {
                    for (Room r : rooms) {
                        if(r.getRoomNumber() == b.roomNumber) r.setAvailable(false);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading bookings: " + e.getMessage());
            }
        }
    }

    private void saveBookings() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(bookingFile))) {
            oos.writeObject(bookings);
        } catch (Exception e) {
            System.out.println("Error saving bookings: " + e.getMessage());
        }
    }

    public void viewRooms() {
        System.out.println("\n--- Room List ---");
        for (Room r : rooms) System.out.println(r);
    }

    private Room searchRoom(String type) {
        for (Room r : rooms) if(r.getType().equalsIgnoreCase(type) && r.isAvailable()) return r;
        return null;
    }

    public void makeBooking(String customerName, String roomType, LocalDate checkIn, LocalDate checkOut, Scanner sc) {
        Room room = searchRoom(roomType);
        if(room != null) {
            System.out.println("\nRoom found: " + room);

            // Payment simulation with cost
            int cost = switch (roomType.toLowerCase()) {
                case "standard" -> 1000;
                case "deluxe" -> 2000;
                case "suite" -> 3000;
                default -> 1000;
            };
            long days = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            int totalCost = (int)(cost * days);

            System.out.println("Room Type: " + roomType);
            System.out.println("Price per night: " + cost);
            System.out.println("Check-in: " + checkIn);
            System.out.println("Check-out: " + checkOut);
            System.out.println("Total nights: " + days);
            System.out.println("Total amount: " + totalCost);

            System.out.print("Proceed with payment (yes/no): ");
            String payment = sc.nextLine();
            boolean paymentDone = payment.equalsIgnoreCase("yes");

            if(paymentDone) {
                room.setAvailable(false);
                int id = bookings.size() + 1;
                Booking booking = new Booking(id, room.getRoomNumber(), customerName, checkIn, checkOut, true);
                bookings.add(booking);
                saveBookings();
                System.out.println("\nBooking confirmed!");
            } else {
                System.out.println("Booking cancelled. Payment not completed.");
            }

        } else {
            System.out.println("No available rooms of type: " + roomType);
        }
    }

    public void cancelBooking(int bookingId) {
        Booking toCancel = null;
        for(Booking b : bookings) if(b.bookingId == bookingId) { toCancel = b; break; }
        if(toCancel != null) {
            bookings.remove(toCancel);
            for(Room r : rooms) if(r.getRoomNumber() == toCancel.roomNumber) r.setAvailable(true);
            saveBookings();
            System.out.println("Booking canceled: Booking ID " + bookingId);
        } else System.out.println("Booking ID not found.");
    }

    public void viewBookings() {
        System.out.println("\n--- All Bookings ---");
        if(bookings.isEmpty()) {
            System.out.println("No bookings yet.");
        } else {
            for(Booking b : bookings) {
                System.out.println("Booking ID: " + b.bookingId);
                System.out.println("Room: " + b.roomNumber);
                System.out.println("Customer: " + b.customerName);
                System.out.println("Check-in: " + b.checkInDate);
                System.out.println("Check-out: " + b.checkOutDate);
                System.out.print("Payment: ");
                if(b.paymentDone) {
                    System.out.println("Completed");
                } else {
                    System.out.println("Pending");
                }
                System.out.println("---------------------------");
            }
        }
    }
}

public class HotelReservationSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Hotel hotel = new Hotel();

        while(true) {
            System.out.println("\n--- Hotel Reservation System ---");
            System.out.println("1. View Rooms");
            System.out.println("2. Make Booking");
            System.out.println("3. Cancel Booking");
            System.out.println("4. View All Bookings");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch(choice) {
                case 1 -> hotel.viewRooms();
                case 2 -> {
                    System.out.print("Enter your name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter room type (Standard/Deluxe/Suite): ");
                    String type = sc.nextLine();
                    System.out.print("Enter check-in date (YYYY-MM-DD): ");
                    LocalDate checkIn = LocalDate.parse(sc.nextLine());
                    System.out.print("Enter check-out date (YYYY-MM-DD): ");
                    LocalDate checkOut = LocalDate.parse(sc.nextLine());
                    hotel.makeBooking(name, type, checkIn, checkOut, sc);
                }
                case 3 -> {
                    System.out.print("Enter booking ID to cancel: ");
                    int id = sc.nextInt();
                    hotel.cancelBooking(id);
                }
                case 4 -> hotel.viewBookings();
                case 5 -> {
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
