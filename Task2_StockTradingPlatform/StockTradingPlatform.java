/*
 * Author: Awais Ali Shah
 * CodeAlpha Student ID: CA/DF1/301415
 * Internship Domain: Java Programming (Sept 2026 batch)
 */
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** CodeAlpha Task 2: Stock Trading Platform (OOP + File I/O persistence). */
public class StockTradingPlatform {

    static class Stock {
        final String symbol, name; double price, previous;
        Stock(String symbol, String name, double price) {
            this.symbol = symbol; this.name = name; this.price = price; this.previous = price;
        }
        void fluctuate(Random rnd) {
            previous = price;
            double change = (rnd.nextDouble() - 0.5) * 0.10; // -5% .. +5%
            price = Math.max(1.0, Math.round(price * (1 + change) * 100) / 100.0);
        }
        double changePct() { return (price - previous) / previous * 100; }
    }

    static class Transaction {
        final String type, symbol; final int qty; final double price; final int day;
        Transaction(String type, String symbol, int qty, double price, int day) {
            this.type = type; this.symbol = symbol; this.qty = qty; this.price = price; this.day = day;
        }
        @Override public String toString() {
            return String.format("Day %d | %-4s | %-5s | %d @ $%.2f = $%.2f", day, type, symbol, qty, price, qty * price);
        }
    }

    static class Portfolio {
        double cash = 10_000;
        int day = 1;
        final Map<String, Integer> holdings = new TreeMap<>();
        final Map<String, Double> costBasis = new HashMap<>(); // total money spent on current holding
        final List<Transaction> transactions = new ArrayList<>();
        final List<double[]> history = new ArrayList<>();      // {day, totalValue}

        double holdingsValue(Map<String, Stock> market) {
            double v = 0;
            for (var e : holdings.entrySet()) v += e.getValue() * market.get(e.getKey()).price;
            return v;
        }
        double totalValue(Map<String, Stock> market) { return cash + holdingsValue(market); }
    }

    static final double STARTING_CASH = 10_000;
    static final Path FILE = Paths.get("portfolio.txt");
    static final Map<String, Stock> market = new LinkedHashMap<>();
    static final Random rnd = new Random();
    static final Scanner in = new Scanner(System.in);
    static Portfolio pf = new Portfolio();

    public static void main(String[] args) {
        market.put("AAPL", new Stock("AAPL", "Apple Inc.", 180.00));
        market.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 140.00));
        market.put("TSLA", new Stock("TSLA", "Tesla Inc.", 250.00));
        market.put("AMZN", new Stock("AMZN", "Amazon.com", 130.00));
        market.put("MSFT", new Stock("MSFT", "Microsoft Corp.", 330.00));
        load();
        if (pf.history.isEmpty()) pf.history.add(new double[]{pf.day, pf.totalValue(market)});

        while (true) {
            System.out.println("\n=== Stock Trading Platform (Day " + pf.day + ") ===");
            System.out.println("1. View market");
            System.out.println("2. Buy stock");
            System.out.println("3. Sell stock");
            System.out.println("4. View portfolio");
            System.out.println("5. Transaction history");
            System.out.println("6. Advance to next day");
            System.out.println("7. Performance over time");
            System.out.println("8. Save & exit");
            System.out.print("Choose: ");
            switch (in.nextLine().trim()) {
                case "1" -> showMarket();
                case "2" -> trade(true);
                case "3" -> trade(false);
                case "4" -> showPortfolio();
                case "5" -> { if (pf.transactions.isEmpty()) System.out.println("No transactions yet."); pf.transactions.forEach(System.out::println); }
                case "6" -> nextDay();
                case "7" -> showPerformance();
                case "8" -> { save(); System.out.println("Saved. Goodbye!"); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    static void showMarket() {
        System.out.printf("%n%-6s %-18s %10s %9s%n", "Symbol", "Company", "Price", "Change");
        for (Stock s : market.values())
            System.out.printf("%-6s %-18s %10.2f %+8.2f%%%n", s.symbol, s.name, s.price, s.changePct());
    }

    static void trade(boolean buy) {
        showMarket();
        System.out.print("Symbol: ");
        Stock s = market.get(in.nextLine().trim().toUpperCase());
        if (s == null) { System.out.println("Unknown symbol."); return; }
        System.out.print("Quantity: ");
        int qty;
        try { qty = Integer.parseInt(in.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Enter a whole number."); return; }
        if (qty <= 0) { System.out.println("Quantity must be positive."); return; }

        double amount = qty * s.price;
        if (buy) {
            if (amount > pf.cash) { System.out.printf("Insufficient cash. Need $%.2f, have $%.2f.%n", amount, pf.cash); return; }
            pf.cash -= amount;
            pf.holdings.merge(s.symbol, qty, Integer::sum);
            pf.costBasis.merge(s.symbol, amount, Double::sum);
        } else {
            int owned = pf.holdings.getOrDefault(s.symbol, 0);
            if (qty > owned) { System.out.println("You only own " + owned + " share(s) of " + s.symbol + "."); return; }
            double avgCost = pf.costBasis.get(s.symbol) / owned;
            pf.cash += amount;
            pf.costBasis.put(s.symbol, pf.costBasis.get(s.symbol) - avgCost * qty);
            if (qty == owned) { pf.holdings.remove(s.symbol); pf.costBasis.remove(s.symbol); }
            else pf.holdings.put(s.symbol, owned - qty);
        }
        Transaction t = new Transaction(buy ? "BUY" : "SELL", s.symbol, qty, s.price, pf.day);
        pf.transactions.add(t);
        System.out.println("Done: " + t);
    }

    static void showPortfolio() {
        System.out.printf("%n%-6s %5s %10s %10s %12s%n", "Symbol", "Qty", "Avg Cost", "Price", "P/L");
        for (var e : pf.holdings.entrySet()) {
            Stock s = market.get(e.getKey());
            double cost = pf.costBasis.get(e.getKey());
            System.out.printf("%-6s %5d %10.2f %10.2f %+12.2f%n", s.symbol, e.getValue(), cost / e.getValue(), s.price, e.getValue() * s.price - cost);
        }
        if (pf.holdings.isEmpty()) System.out.println("(no holdings)");
        double total = pf.totalValue(market);
        System.out.printf("%nCash: $%.2f%nHoldings value: $%.2f%nTotal value: $%.2f (%+.2f%% since start)%n",
                pf.cash, pf.holdingsValue(market), total, (total - STARTING_CASH) / STARTING_CASH * 100);
    }

    static void nextDay() {
        market.values().forEach(s -> s.fluctuate(rnd));
        pf.day++;
        pf.history.add(new double[]{pf.day, pf.totalValue(market)});
        System.out.println("Advanced to day " + pf.day + ". Market updated.");
        showMarket();
    }

    static void showPerformance() {
        System.out.printf("%n%-5s %12s %9s%n", "Day", "Value", "Return");
        for (double[] h : pf.history) {
            int bars = (int) Math.max(0, Math.min(40, (h[1] - STARTING_CASH * 0.8) / (STARTING_CASH * 0.4) * 40));
            System.out.printf("%-5d %12.2f %+8.2f%% %s%n", (int) h[0], h[1], (h[1] - STARTING_CASH) / STARTING_CASH * 100, "#".repeat(bars));
        }
    }

    // ---- File I/O persistence ----
    static void save() {
        List<String> lines = new ArrayList<>();
        lines.add("P," + pf.cash + "," + pf.day);
        for (var e : pf.holdings.entrySet()) lines.add("H," + e.getKey() + "," + e.getValue() + "," + pf.costBasis.get(e.getKey()));
        for (Transaction t : pf.transactions) lines.add("T," + t.type + "," + t.symbol + "," + t.qty + "," + t.price + "," + t.day);
        for (double[] h : pf.history) lines.add("V," + h[0] + "," + h[1]);
        for (Stock s : market.values()) lines.add("S," + s.symbol + "," + s.price + "," + s.previous);
        try { Files.write(FILE, lines); }
        catch (IOException e) { System.out.println("Could not save: " + e.getMessage()); }
    }

    static void load() {
        if (!Files.exists(FILE)) return;
        try {
            Portfolio p = new Portfolio();
            for (String line : Files.readAllLines(FILE)) {
                String[] f = line.split(",");
                switch (f[0]) {
                    case "P" -> { p.cash = Double.parseDouble(f[1]); p.day = Integer.parseInt(f[2]); }
                    case "H" -> { p.holdings.put(f[1], Integer.parseInt(f[2])); p.costBasis.put(f[1], Double.parseDouble(f[3])); }
                    case "T" -> p.transactions.add(new Transaction(f[1], f[2], Integer.parseInt(f[3]), Double.parseDouble(f[4]), Integer.parseInt(f[5])));
                    case "V" -> p.history.add(new double[]{Double.parseDouble(f[1]), Double.parseDouble(f[2])});
                    case "S" -> { Stock s = market.get(f[1]); if (s != null) { s.price = Double.parseDouble(f[2]); s.previous = Double.parseDouble(f[3]); } }
                }
            }
            pf = p;
            System.out.println("Loaded saved portfolio.");
        } catch (Exception e) { System.out.println("Could not load saved data, starting fresh: " + e.getMessage()); }
    }
}
