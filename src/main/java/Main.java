import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Main {
    private static final int ARRAY_SIZE = 10000;
    private static final int SEARCH_COUNT = 100;
    private static final int DELETE_COUNT = 1000;

    private static final Random random = new Random(42);

    public static void main(String[] args) {
        BStarTree tree = new BStarTree();

        int[] data = generateRandomArray(ARRAY_SIZE);

        List<ExperimentResult> insertResults = new ArrayList<>();
        List<ExperimentResult> searchResults = new ArrayList<>();
        List<ExperimentResult> deleteResults = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            OperationResult result = tree.add(data[i]);

            insertResults.add(new ExperimentResult(
                    "insert",
                    data[i],
                    result.isSuccess(),
                    result.getTime(),
                    result.getOperations()
            ));
        }

        int[] searchValues = chooseRandomValues(data, SEARCH_COUNT);

        for (int i = 0; i < searchValues.length; i++) {
            OperationResult result = tree.contains(searchValues[i]);

            searchResults.add(new ExperimentResult(
                    "search",
                    searchValues[i],
                    result.isSuccess(),
                    result.getTime(),
                    result.getOperations()
            ));
        }

        int[] deleteValues = chooseRandomValues(data, DELETE_COUNT);

        for (int i = 0; i < deleteValues.length; i++) {
            OperationResult result = tree.remove(deleteValues[i]);

            deleteResults.add(new ExperimentResult(
                    "delete",
                    deleteValues[i],
                    result.isSuccess(),
                    result.getTime(),
                    result.getOperations()
            ));
        }

        try {
            saveResults("insert_results.csv", insertResults);
            saveResults("search_results.csv", searchResults);
            saveResults("delete_results.csv", deleteResults);
            saveSummary("summary.csv", insertResults, searchResults, deleteResults);
        } catch (IOException e) {
            System.out.println("Ошибка при записи файлов: " + e.getMessage());
        }

        printSummary(insertResults, searchResults, deleteResults);
    }

    private static int[] generateRandomArray(int size) {
        List<Integer> numbers = new ArrayList<>();

        for (int i = 1; i <= size * 10; i++) {
            numbers.add(i);
        }

        Collections.shuffle(numbers, random);

        int[] result = new int[size];

        for (int i = 0; i < size; i++) {
            result[i] = numbers.get(i);
        }

        return result;
    }

    private static int[] chooseRandomValues(int[] data, int count) {
        List<Integer> values = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            values.add(data[i]);
        }

        Collections.shuffle(values, random);

        int[] result = new int[count];

        for (int i = 0; i < count; i++) {
            result[i] = values.get(i);
        }

        return result;
    }

    private static void saveResults(String fileName, List<ExperimentResult> results) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(fileName));

        writer.println("operation;value;success;time_ns;operations");

        for (int i = 0; i < results.size(); i++) {
            ExperimentResult result = results.get(i);

            writer.println(
                    result.getOperationType() + ";" +
                            result.getValue() + ";" +
                            result.isSuccess() + ";" +
                            result.getTime() + ";" +
                            result.getOperations()
            );
        }

        writer.close();
    }

    private static void saveSummary(
            String fileName,
            List<ExperimentResult> insertResults,
            List<ExperimentResult> searchResults,
            List<ExperimentResult> deleteResults
    ) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(fileName));

        writer.println("operation;average_time_ns;average_operations");

        writer.printf(Locale.US, "insert;%.2f;%.2f%n",
                averageTime(insertResults),
                averageOperations(insertResults)
        );

        writer.printf(Locale.US, "search;%.2f;%.2f%n",
                averageTime(searchResults),
                averageOperations(searchResults)
        );

        writer.printf(Locale.US, "delete;%.2f;%.2f%n",
                averageTime(deleteResults),
                averageOperations(deleteResults)
        );

        writer.close();
    }

    private static void printSummary(
            List<ExperimentResult> insertResults,
            List<ExperimentResult> searchResults,
            List<ExperimentResult> deleteResults
    ) {
        System.out.println("Средние результаты:");
        System.out.println();

        System.out.printf(Locale.US,
                "Вставка: среднее время = %.2f нс, среднее количество операций = %.2f%n",
                averageTime(insertResults),
                averageOperations(insertResults)
        );

        System.out.printf(Locale.US,
                "Поиск: среднее время = %.2f нс, среднее количество операций = %.2f%n",
                averageTime(searchResults),
                averageOperations(searchResults)
        );

        System.out.printf(Locale.US,
                "Удаление: среднее время = %.2f нс, среднее количество операций = %.2f%n",
                averageTime(deleteResults),
                averageOperations(deleteResults)
        );
    }

    private static double averageTime(List<ExperimentResult> results) {
        long sum = 0;

        for (int i = 0; i < results.size(); i++) {
            sum += results.get(i).getTime();
        }

        return (double) sum / results.size();
    }

    private static double averageOperations(List<ExperimentResult> results) {
        long sum = 0;

        for (int i = 0; i < results.size(); i++) {
            sum += results.get(i).getOperations();
        }

        return (double) sum / results.size();
    }
}