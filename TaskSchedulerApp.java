import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class TaskSchedulerApp {

    // Fix 1: Use AtomicLong counter instead of System.nanoTime() for reliable FIFO tie-breaking
    private static final AtomicLong insertionCounter = new AtomicLong(0);

    // 1. Task Data Model with Priority and Tie-Breaking Logic
    static class Task implements Comparable<Task> {
        private final String id;
        private final String description;
        private final Priority priority;
        private final long insertionOrder; // Fix 1: Guaranteed unique, monotonically increasing

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
            this.insertionOrder = insertionCounter.getAndIncrement(); // Fix 1: Thread-safe counter
        }

        @Override
        public int compareTo(Task other) {
            // Higher rank means higher urgency (Max-Heap behavior)
            int priorityCompare = Integer.compare(other.priority.getRank(), this.priority.getRank());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            // If priorities match, the older task (lower insertion order) takes precedence
            return Long.compare(this.insertionOrder, other.insertionOrder); // Fix 1: Reliable FIFO
        }

        @Override
        public String toString() {
            return String.format("[ID: %s] | Priority: %s | Description: %s", id, priority, description);
        }
    }

    // 2. Interactive CLI Application Loop
    public static void main(String[] args) {
        PriorityQueue<Task> taskQueue = new PriorityQueue<>();
        System.out.println("=== Amazon Target: Task Priority Manager CLI ===");

        // Fix 3: Use try-with-resources so Scanner is properly managed without closing System.in abruptly
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n1. Add Task | 2. Execute Next Urgent Task | 3. View Next Task | 4. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        System.out.print("Enter Task ID (e.g., T101): ");
                        String id = scanner.nextLine().trim();

                        // Fix 2: Validate that ID is not blank
                        if (id.isEmpty()) {
                            System.out.println(">> Error: Task ID cannot be empty.");
                            break;
                        }

                        System.out.print("Enter Task Description: ");
                        String desc = scanner.nextLine().trim();

                        // Fix 2: Validate that description is not blank
                        if (desc.isEmpty()) {
                            System.out.println(">> Error: Task description cannot be empty.");
                            break;
                        }

                        System.out.print("Enter Priority (HIGH, MEDIUM, LOW): ");
                        String prioInput = scanner.nextLine().trim().toUpperCase();

                        try {
                            Task.Priority priority = Task.Priority.valueOf(prioInput);
                            taskQueue.offer(new Task(id, desc, priority));
                            System.out.println(">> Success: Task scheduled efficiently in O(log N) time.");
                        } catch (IllegalArgumentException e) {
                            System.out.println(">> Error: Invalid priority level. Use HIGH, MEDIUM, or LOW.");
                        }
                        break;

                    case "2":
                        if (taskQueue.isEmpty()) {
                            System.out.println(">> No pending tasks in queue.");
                        } else {
                            Task executedTask = taskQueue.poll();
                            System.out.println(">> Executing Task:\n   " + executedTask);
                        }
                        break;

                    case "3":
                        if (taskQueue.isEmpty()) {
                            System.out.println(">> No pending tasks in queue.");
                        } else {
                            System.out.println(">> Next Up (Highest Priority):\n   " + taskQueue.peek());
                        }
                        break;

                    case "4":
                        System.out.println("Exiting Application. Good luck with Amazon ML School!");
                        return; // Fix 3: scanner.close() handled automatically by try-with-resources

                    default:
                        System.out.println(">> Invalid option. Please try again.");
                }
            }
        }
    }
}
