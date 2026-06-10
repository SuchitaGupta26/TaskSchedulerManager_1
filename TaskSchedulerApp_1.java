import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class TaskSchedulerApp {

    private static final AtomicLong insertionCounter = new AtomicLong(0);

    static class Task implements Comparable<Task> {
        private final String id;
        private final String description;
        private final Priority priority;
        private final long insertionOrder;

        public enum Priority {
            HIGH(3), MEDIUM(2), LOW(1);

            private final int rank;
            Priority(int rank) { this.rank = rank; }
            public int getRank() { return rank; }
        }

        public Task(String id, String description, Priority priority) {
            this.id = id;
            this.description = description;
            this.priority = priority;
            this.insertionOrder = insertionCounter.getAndIncrement();
        }

        @Override
        public int compareTo(Task other) {
            int priorityCompare = Integer.compare(other.priority.getRank(), this.priority.getRank());
            if (priorityCompare != 0) return priorityCompare;
            return Long.compare(this.insertionOrder, other.insertionOrder);
        }

        @Override
        public String toString() {
            return String.format("[%s] %-8s %s", id, priority, description);
        }
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║        TASK PRIORITY SCHEDULER           ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("  ┌─────────────────────────────────┐");
        System.out.println("  │  1  Add a new task               │");
        System.out.println("  │  2  Execute highest priority task │");
        System.out.println("  │  3  Peek at next task             │");
        System.out.println("  │  4  View all pending tasks        │");
        System.out.println("  │  5  Exit                          │");
        System.out.println("  └─────────────────────────────────┘");
        System.out.print("  Choose › ");
    }

    private static void printDivider() {
        System.out.println("  ──────────────────────────────────────────");
    }

    private static void printSuccess(String msg) {
        System.out.println("  ✔  " + msg);
    }

    private static void printError(String msg) {
        System.out.println("  ✘  " + msg);
    }

    private static void printInfo(String msg) {
        System.out.println("  ℹ  " + msg);
    }

    private static void printTask(int index, Task task) {
        String badge = switch (task.priority) {
            case HIGH   -> "[ HIGH   ]";
            case MEDIUM -> "[ MEDIUM ]";
            case LOW    -> "[ LOW    ]";
        };
        System.out.printf("  %2d. %s  %-8s  %s%n", index, badge, task.id, task.description);
    }

    // ── Main loop ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        PriorityQueue<Task> taskQueue = new PriorityQueue<>();

        printBanner();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                printMenu();
                String choice = scanner.nextLine().trim();
                System.out.println();

                switch (choice) {

                    case "1" -> {
                        System.out.print("  Task ID      › ");
                        String id = scanner.nextLine().trim();
                        if (id.isEmpty()) { printError("Task ID cannot be empty."); break; }

                        System.out.print("  Description  › ");
                        String desc = scanner.nextLine().trim();
                        if (desc.isEmpty()) { printError("Description cannot be empty."); break; }

                        System.out.print("  Priority (HIGH / MEDIUM / LOW) › ");
                        String prioInput = scanner.nextLine().trim().toUpperCase();

                        try {
                            Task.Priority priority = Task.Priority.valueOf(prioInput);
                            taskQueue.offer(new Task(id, desc, priority));
                            printDivider();
                            printSuccess("Task \"" + id + "\" added with " + priority + " priority.");
                        } catch (IllegalArgumentException e) {
                            printError("Invalid priority. Use HIGH, MEDIUM, or LOW.");
                        }
                    }

                    case "2" -> {
                        if (taskQueue.isEmpty()) {
                            printInfo("No tasks in the queue.");
                        } else {
                            Task t = taskQueue.poll();
                            printDivider();
                            printSuccess("Executed task:");
                            System.out.println("  " + t);
                            printInfo(taskQueue.size() + " task(s) remaining.");
                        }
                    }

                    case "3" -> {
                        if (taskQueue.isEmpty()) {
                            printInfo("No tasks in the queue.");
                        } else {
                            Task t = taskQueue.peek();
                            printDivider();
                            printInfo("Next up (will not be removed):");
                            System.out.println("  " + t);
                        }
                    }

                    case "4" -> {
                        if (taskQueue.isEmpty()) {
                            printInfo("No tasks in the queue.");
                        } else {
                            printDivider();
                            System.out.println("  Pending tasks (" + taskQueue.size() + "):");
                            System.out.println();
                            PriorityQueue<Task> copy = new PriorityQueue<>(taskQueue);
                            int i = 1;
                            while (!copy.isEmpty()) {
                                printTask(i++, copy.poll());
                            }
                        }
                    }

                    case "5" -> {
                        printDivider();
                        System.out.println("  Goodbye!");
                        System.out.println();
                        return;
                    }

                    default -> printError("Invalid option. Enter a number from 1 to 5.");
                }

                System.out.println();
            }
        }
    }
}
