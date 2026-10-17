/*
 * Author: Awais Ali Shah
 * CodeAlpha Student ID: CA/DF1/301415
 * Internship Domain: Java Programming (Sept 2026 batch)
 */
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.util.*;

/** CodeAlpha Task 4: Hotel Reservation System (OOP + File I/O). */
public class HotelReservationSystem {

    enum Category { STANDARD, DELUXE, SUITE }

    static class Room {
        final int number; final Category category; final double pricePerNight;
        Room(int number, Category category, double price) {
            this.number = number; this.category = category; this.pricePerNight = price;
        }
    }

    static class Booking {
        final int id; final String guest; final int roomNumber;
        final LocalDate checkIn, checkOut; final double total; boolean active = true;
        Booking(int id, String guest, int room, LocalDate in, LocalDate out, double total) {
            this.id = id; this.guest = guest; this.roomNumber = room;
            this.checkIn = in; this.checkOut = out; this.total = total;
        }
        String toCsv() { return id + "," + guest + "," + roomNumber + "," + checkIn + "," + checkOut + "," + total + "," + active; }
        static Booking fromCsv(String line) {
            String[] p = line.split(",");
            Booking b = new Booking(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]),
                    LocalDate.parse(p[3]), LocalDate.parse(p[4]), Double.parseDouble(p[5]));
            b.active = Boolean.parseBoolean(p[6]);
            return b;
        }
        @Override public String toString() {
            return String.format("#%d | %s | Room %d | %s -> %s | $%.2f | %s",
                    id, guest, roomNumber, checkIn, checkOut, total, active ? "CONFIRMED" : "CANCELLED");
        }
    }

    private static final Path FILE = Paths.get("bookings.csv");
    private static final List<Room> rooms = new ArrayList<>();
    private static final List<Booking> bookings = new ArrayList<>();
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        for (int i = 101; i <= 105; i++) rooms.add(new Room(i, Category.STANDARD, 50));
        for (int i = 201; i <= 203; i++) rooms.add(new Room(i, Category.DELUXE, 90));
        for (int i = 301; i <= 302; i++) rooms.add(new Room(i, Category.SUITE, 180));
        load();

        while (true) {
            System.out.println("\n=== Hotel Reservation System ===");
            System.out.println("1. Search available rooms");
            System.out.println("2. Book a room");
            System.out.println("3. Cancel a reservation");
            System.out.println("4. View booking details");
            System.out.println("5. Exit");
            System.out.print("Choose: ");
            switch (in.nextLine().trim()) {
                case "1" -> search();
                case "2" -> book();
                case "3" -> cancel();
                case "4" -> view();
                case "5" -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void search() {
        LocalDate[] d = readDates();
        if (d == null) return;
        Category cat = readCategory();
        System.out.println("\nAvailable rooms:");
        boolean any = false;
        for (Room r : rooms) {
            if ((cat == null || r.category == cat) && isFree(r.number, d[0], d[1])) {
                System.out.printf("Room %d | %s | $%.2f/night%n", r.number, r.category, r.pricePerNight);
                any = true;
            }
        }
        if (!any) System.out.println("None available for those dates.");
    }

    private static void book() {
        System.out.print("Guest name: ");
        String guest = in.nextLine().trim().replace(",", " ");
        if (guest.isEmpty()) { System.out.println("Name required."); return; }
        LocalDate[] d = readDates();
        if (d == null) return;
        System.out.print("Room number: ");
        Room room = findRoom(in.nextLine().trim());
        if (room == null) { System.out.println("Room not found."); return; }
        if (!isFree(room.number, d[0], d[1])) { System.out.println("Room not available for those dates."); return; }

        long nights = ChronoUnit.DAYS.between(d[0], d[1]);
        double total = nights * room.pricePerNight;
        System.out.printf("Total for %d night(s): $%.2f%n", nights, total);
        System.out.print("Confirm and pay? (y/n): ");
        if (!in.nextLine().trim().equalsIgnoreCase("y")) { System.out.println("Booking aborted."); return; }

        System.out.println("Processing payment... Payment successful (simulated).");
        int id = bookings.stream().mapToInt(b -> b.id).max().orElse(1000) + 1;
        Booking b = new Booking(id, guest, room.number, d[0], d[1], total);
        bookings.add(b);
        save();
        System.out.println("Booked! " + b);
    }

    private static void cancel() {
        System.out.print("Booking ID: ");
        Booking b = findBooking(in.nextLine().trim());
        if (b == null || !b.active) { System.out.println("No active booking with that ID."); return; }
        b.active = false;
        save();
        System.out.printf("Cancelled. Refund of $%.2f simulated.%n", b.total);
    }

    private static void view() {
        System.out.print("Booking ID (or press Enter for all): ");
        String s = in.nextLine().trim();
        if (s.isEmpty()) {
            if (bookings.isEmpty()) System.out.println("No bookings yet.");
            bookings.forEach(System.out::println);
            return;
        }
        Booking b = findBooking(s);
        System.out.println(b == null ? "Not found." : b);
    }

    private static boolean isFree(int roomNo, LocalDate in, LocalDate out) {
        for (Booking b : bookings)
            if (b.active && b.roomNumber == roomNo && in.isBefore(b.checkOut) && b.checkIn.isBefore(out)) return false;
        return true;
    }

    private static LocalDate[] readDates() {
        try {
            System.out.print("Check-in (YYYY-MM-DD): ");
            LocalDate a = LocalDate.parse(in.nextLine().trim());
            System.out.print("Check-out (YYYY-MM-DD): ");
            LocalDate b = LocalDate.parse(in.nextLine().trim());
            if (!b.isAfter(a) || a.isBefore(LocalDate.now())) {
                System.out.println("Check-out must be after check-in, and check-in cannot be in the past.");
                return null;
            }
            return new LocalDate[]{a, b};
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            return null;
        }
    }

    private static Category readCategory() {
        System.out.print("Category (standard/deluxe/suite, Enter for all): ");
        String s = in.nextLine().trim().toUpperCase();
        if (s.isEmpty()) return null;
        try { return Category.valueOf(s); }
        catch (IllegalArgumentException e) { System.out.println("Unknown category, showing all."); return null; }
    }

    private static Room findRoom(String s) {
        try { int n = Integer.parseInt(s); for (Room r : rooms) if (r.number == n) return r; }
        catch (NumberFormatException ignored) {}
        return null;
    }

    private static Booking findBooking(String s) {
        try { int id = Integer.parseInt(s); for (Booking b : bookings) if (b.id == id) return b; }
        catch (NumberFormatException ignored) {}
        return null;
    }

    private static void save() {
        try {
            List<String> lines = new ArrayList<>();
            for (Booking b : bookings) lines.add(b.toCsv());
            Files.write(FILE, lines);
        } catch (IOException e) { System.out.println("Could not save: " + e.getMessage()); }
    }

    private static void load() {
        if (!Files.exists(FILE)) return;
        try {
            for (String line : Files.readAllLines(FILE))
                if (!line.isBlank()) bookings.add(Booking.fromCsv(line));
        } catch (Exception e) { System.out.println("Could not load bookings: " + e.getMessage()); }
    }
}
