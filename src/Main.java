import java.util.Arrays;
/**
 * Simple test runner for the Clinic Queue Management System.
 * Used to validate system functionality.
 */
public class Main {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testEmptySystem();
        testDoctorEnterLeave();
        testPatientEnterSpecificDoctor();
        testPatientLeaveOperations();
        testRangeQueries();
        testLeastBusyDoctor();
        testAutoAssignLeastBusyDoctor();
        testTopKBusyDoctors();
        testTopKBusyDoctorsEdgeCases();
        testStructureAfterMixedOperations();

        System.out.println();
        System.out.println("====================================");
        System.out.println("TOTAL PASSED: " + passed);
        System.out.println("TOTAL FAILED: " + failed);
        System.out.println("====================================");
    }

    // =========================
    // Assertion helpers
    // =========================

    private static void expectEquals(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) ||
                (expected != null && expected.equals(actual))) {
            passed++;
            System.out.println("[PASS] " + testName + " | expected = " + expected + ", actual = " + actual);
        } else {
            failed++;
            System.out.println("[FAIL] " + testName + " | expected = " + expected + ", actual = " + actual);
        }
    }

    private static void expectArrayEquals(String testName, String[] expected, String[] actual) {
        if (Arrays.equals(expected, actual)) {
            passed++;
            System.out.println("[PASS] " + testName + " | expected = " + Arrays.toString(expected)
                    + ", actual = " + Arrays.toString(actual));
        } else {
            failed++;
            System.out.println("[FAIL] " + testName + " | expected = " + Arrays.toString(expected)
                    + ", actual = " + Arrays.toString(actual));
        }
    }

    private static void expectTrue(String testName, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + testName);
        } else {
            failed++;
            System.out.println("[FAIL] " + testName);
        }
    }

    private static void expectThrows(String testName, Runnable action) {
        try {
            action.run();
            failed++;
            System.out.println("[FAIL] " + testName + " | expected exception, but nothing was thrown");
        } catch (Exception e) {
            passed++;
            System.out.println("[PASS] " + testName + " | threw: " + e.getClass().getSimpleName()
                    + " | message: " + e.getMessage());
        }
    }

    // =========================
    // Tests
    // =========================

    private static void testEmptySystem() {
        System.out.println("\n=== testEmptySystem ===");
        ClinicManager cm = new ClinicManager();

        expectThrows("getLeastBusyDoctor on empty system", cm::getLeastBusyDoctor);
        expectArrayEquals("getTopKBusyDoctors on empty system", new String[0], cm.getTopKBusyDoctors(3));
        expectEquals("numDoctorsWithLoadInRange on empty system", 0, cm.numDoctorsWithLoadInRange(0, 10));
        expectEquals("averageLoadWithinRange on empty system", 0, cm.averageLoadWithinRange(0, 10));

        expectThrows("nextPatient on empty system", () -> cm.nextPatient("D1"));
        expectThrows("waitingForDoctor on empty system", () -> cm.waitingForDoctor("P1"));
        expectThrows("patientEnter(auto) on empty system", () -> cm.patientEnter("P1"));
    }

    private static void testDoctorEnterLeave() {
        System.out.println("\n=== testDoctorEnterLeave ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        cm.doctorEnter("D2");

        expectEquals("D1 initial load", 0, cm.numPatients("D1"));
        expectEquals("D2 initial load", 0, cm.numPatients("D2"));
        expectEquals("doctors with load 0..0 after insert", 2, cm.numDoctorsWithLoadInRange(0, 0));
        expectEquals("average load 0..0 after insert", 0, cm.averageLoadWithinRange(0, 0));

        expectThrows("duplicate doctor enter", () -> cm.doctorEnter("D1"));

        cm.doctorLeave("D2");
        expectThrows("deleted doctor should not exist", () -> cm.numPatients("D2"));
        expectEquals("doctors with load 0..0 after D2 leaves", 1, cm.numDoctorsWithLoadInRange(0, 0));

        expectThrows("leave non-existing doctor", () -> cm.doctorLeave("D2"));
    }

    private static void testPatientEnterSpecificDoctor() {
        System.out.println("\n=== testPatientEnterSpecificDoctor ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        cm.doctorEnter("D2");

        cm.patientEnter("D1", "P1");
        cm.patientEnter("D1", "P2");
        cm.patientEnter("D2", "P3");

        expectEquals("D1 load after 2 patients", 2, cm.numPatients("D1"));
        expectEquals("D2 load after 1 patient", 1, cm.numPatients("D2"));

        expectEquals("waitingForDoctor P1", "D1", cm.waitingForDoctor("P1"));
        expectEquals("waitingForDoctor P2", "D1", cm.waitingForDoctor("P2"));
        expectEquals("waitingForDoctor P3", "D2", cm.waitingForDoctor("P3"));

        expectEquals("nextPatient D1", "P1", cm.nextPatient("D1"));
        expectEquals("nextPatient D2", "P3", cm.nextPatient("D2"));

        expectThrows("duplicate patient id specific enter", () -> cm.patientEnter("D2", "P1"));
        expectThrows("enter patient to missing doctor", () -> cm.patientEnter("D9", "P9"));
    }

    private static void testPatientLeaveOperations() {
        System.out.println("\n=== testPatientLeaveOperations ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        cm.patientEnter("D1", "P1");
        cm.patientEnter("D1", "P2");
        cm.patientEnter("D1", "P3");

        expectEquals("nextPatient before leave", "P1", cm.nextPatient("D1"));

        String left = cm.nextPatientLeave("D1");
        expectEquals("nextPatientLeave returns P1", "P1", left);
        expectEquals("nextPatient after removing first", "P2", cm.nextPatient("D1"));
        expectEquals("load after nextPatientLeave", 2, cm.numPatients("D1"));
        expectThrows("P1 removed from system", () -> cm.waitingForDoctor("P1"));

        cm.patientLeaveEarly("P3");
        expectEquals("load after patientLeaveEarly", 1, cm.numPatients("D1"));
        expectEquals("nextPatient after removing P3", "P2", cm.nextPatient("D1"));
        expectThrows("P3 removed from system", () -> cm.waitingForDoctor("P3"));

        cm.nextPatientLeave("D1");
        expectEquals("D1 load back to 0", 0, cm.numPatients("D1"));

        expectThrows("no next patient now", () -> cm.nextPatient("D1"));
        expectThrows("leave early missing patient", () -> cm.patientLeaveEarly("P999"));
    }

    private static void testRangeQueries() {
        System.out.println("\n=== testRangeQueries ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        cm.doctorEnter("D2");
        cm.doctorEnter("D3");

        cm.patientEnter("D1", "P1");
        cm.patientEnter("D1", "P2");
        cm.patientEnter("D2", "P3");

        // loads: D1=2, D2=1, D3=0

        expectEquals("doctors in range 0..0", 1, cm.numDoctorsWithLoadInRange(0, 0));
        expectEquals("doctors in range 1..1", 1, cm.numDoctorsWithLoadInRange(1, 1));
        expectEquals("doctors in range 2..2", 1, cm.numDoctorsWithLoadInRange(2, 2));
        expectEquals("doctors in range 0..2", 3, cm.numDoctorsWithLoadInRange(0, 2));

        expectEquals("average load in range 0..2", 1, cm.averageLoadWithinRange(0, 2)); // (0+1+2)/3 = 1
        expectEquals("average load in range 1..2", 1, cm.averageLoadWithinRange(1, 2)); // (1+2)/2 = 1
        expectEquals("average load in empty range", 0, cm.averageLoadWithinRange(5, 7));
    }

    private static void testLeastBusyDoctor() {
        System.out.println("\n=== testLeastBusyDoctor ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        expectEquals("least busy with one doctor", "D1", cm.getLeastBusyDoctor());

        cm.doctorEnter("D2");
        cm.doctorEnter("D3");

        // all 0, any one of them is logically acceptable unless you fixed tie-breaker
        String least1 = cm.getLeastBusyDoctor();
        expectTrue("least busy among 3 doctors with load 0",
                least1.equals("D1") || least1.equals("D2") || least1.equals("D3"));

        cm.patientEnter("D1", "P1");
        String least2 = cm.getLeastBusyDoctor();
        expectTrue("least busy after D1 gets one patient",
                least2.equals("D2") || least2.equals("D3"));

        cm.patientEnter("D2", "P2");
        String least3 = cm.getLeastBusyDoctor();
        expectEquals("least busy after D1 and D2 get one patient", "D3", least3);

        cm.patientEnter("D3", "P3");
        String least4 = cm.getLeastBusyDoctor();
        expectTrue("least busy after all loads equal 1",
                least4.equals("D1") || least4.equals("D2") || least4.equals("D3"));
    }

    private static void testAutoAssignLeastBusyDoctor() {
        System.out.println("\n=== testAutoAssignLeastBusyDoctor ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        cm.doctorEnter("D2");
        cm.doctorEnter("D3");

        cm.patientEnter("P1");
        cm.patientEnter("P2");
        cm.patientEnter("P3");

        int d1 = cm.numPatients("D1");
        int d2 = cm.numPatients("D2");
        int d3 = cm.numPatients("D3");

        expectEquals("total doctors with load 1 after 3 auto-assigned patients",
                3, cm.numDoctorsWithLoadInRange(1, 1));

        expectTrue("each doctor has exactly one patient after 3 auto-assigned patients",
                d1 == 1 && d2 == 1 && d3 == 1);

        cm.patientEnter("P4");

        int sumLoads = cm.numPatients("D1") + cm.numPatients("D2") + cm.numPatients("D3");
        expectEquals("total load after 4 auto-assigned patients", 4, sumLoads);

        expectThrows("duplicate patient id auto enter", () -> cm.patientEnter("P4"));
    }

    private static void testTopKBusyDoctors() {
        System.out.println("\n=== testTopKBusyDoctors ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        cm.doctorEnter("D2");
        cm.doctorEnter("D3");
        cm.doctorEnter("D4");

        cm.patientEnter("D1", "P1");
        cm.patientEnter("D1", "P2");
        cm.patientEnter("D1", "P3");

        cm.patientEnter("D2", "P4");
        cm.patientEnter("D2", "P5");

        cm.patientEnter("D3", "P6");

        // loads: D1=3, D2=2, D3=1, D4=0

        String[] top1 = cm.getTopKBusyDoctors(1);
        expectArrayEquals("top1 doctors", new String[]{"D1"}, top1);

        String[] top2 = cm.getTopKBusyDoctors(2);
        expectArrayEquals("top2 doctors", new String[]{"D1", "D2"}, top2);

        String[] top3 = cm.getTopKBusyDoctors(3);
        expectArrayEquals("top3 doctors", new String[]{"D1", "D2", "D3"}, top3);

        String[] top10 = cm.getTopKBusyDoctors(10);
        expectArrayEquals("top10 trimmed to existing doctors",
                new String[]{"D1", "D2", "D3", "D4"}, top10);
    }

    private static void testTopKBusyDoctorsEdgeCases() {
        System.out.println("\n=== testTopKBusyDoctorsEdgeCases ===");
        ClinicManager cm = new ClinicManager();

        expectArrayEquals("topK on empty returns empty", new String[0], cm.getTopKBusyDoctors(3));
        expectThrows("topK with k = 0", () -> cm.getTopKBusyDoctors(0));
        expectThrows("topK with negative k", () -> cm.getTopKBusyDoctors(-1));

        cm.doctorEnter("D1");
        cm.doctorEnter("D2");

        String[] top2 = cm.getTopKBusyDoctors(2);
        expectTrue("top2 with equal loads contains both doctors",
                top2.length == 2 &&
                        contains(top2, "D1") &&
                        contains(top2, "D2"));
    }

    private static void testStructureAfterMixedOperations() {
        System.out.println("\n=== testStructureAfterMixedOperations ===");
        ClinicManager cm = new ClinicManager();

        cm.doctorEnter("D1");
        cm.doctorEnter("D2");
        cm.doctorEnter("D3");

        cm.patientEnter("P1");
        cm.patientEnter("P2");
        cm.patientEnter("P3");
        cm.patientEnter("P4");

        expectEquals("total doctors range 0..10 after auto enters", 3, cm.numDoctorsWithLoadInRange(0, 10));

        String doctorOfP1 = cm.waitingForDoctor("P1");
        cm.patientLeaveEarly("P1");
        expectThrows("P1 removed after early leave", () -> cm.waitingForDoctor("P1"));

        if (cm.numPatients(doctorOfP1) == 0) {
            cm.doctorLeave(doctorOfP1);
            expectThrows("removed doctor gone", () -> cm.numPatients(doctorOfP1));
        }

        int totalDoctorsLeft = cm.numDoctorsWithLoadInRange(0, 10);
        expectTrue("total doctors left is 2 or 3 after optional doctor leave",
                totalDoctorsLeft == 2 || totalDoctorsLeft == 3);

        int totalLoad = 0;
        if (existsDoctor(cm, "D1")) totalLoad += cm.numPatients("D1");
        if (existsDoctor(cm, "D2")) totalLoad += cm.numPatients("D2");
        if (existsDoctor(cm, "D3")) totalLoad += cm.numPatients("D3");

        expectEquals("total remaining patients after removing one early", 3, totalLoad);
    }

    // =========================
    // Utility helpers
    // =========================

    private static boolean contains(String[] arr, String target) {
        for (String s : arr) {
            if (target.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private static boolean existsDoctor(ClinicManager cm, String doctorId) {
        try {
            cm.numPatients(doctorId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
