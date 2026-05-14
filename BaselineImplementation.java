import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class Researcher {
    private String name;
    private UI ui;

    public Researcher(String name, UI ui) {
        this.name = name;
        this.ui = ui;
    }

    public void submitResearchOutput(Map<String, String> data) {
        System.out.println("[" + this.name + "] Submitting research output: " + data.get("title"));
        ui.submit(data);
    }

    public void receiveNotification(String message) {
        System.out.println("[" + this.name + "] Received notification: " + message);
    }
}

class UI {
    private SubmissionController controller;

    public UI(SubmissionController controller) {
        this.controller = controller;
    }

    public void submit(Map<String, String> data) {
        System.out.println("[UI] Receiving submission and passing to controller.");
        controller.submit(data);
    }
}

class SubmissionController {
    private Validator validator;
    private Database database;
    private ReviewerManager reviewerManager;
    private EvaluationManager evaluationManager;
    private NotificationService notificationService;

    public SubmissionController(Validator validator, Database database, ReviewerManager reviewerManager, EvaluationManager evaluationManager, NotificationService notificationService) {
        this.validator = validator;
        this.database = database;
        this.reviewerManager = reviewerManager;
        this.evaluationManager = evaluationManager;
        this.notificationService = notificationService;
    }

    public boolean submit(Map<String, String> data) {
        System.out.println("[SubmissionController] Validating submission format.");
        if (!validator.validateFormat(data)) {
            System.out.println("[SubmissionController] Submission invalid.");
            return false;
        }

        System.out.println("[SubmissionController] Submission valid. Saving to database.");
        database.saveSubmission(data);

        System.out.println("[SubmissionController] Getting available reviewers.");
        List<Reviewer> availableReviewers = reviewerManager.getAvailableReviewers();
        System.out.println("[SubmissionController] Assigning reviews to " + availableReviewers.size() + " reviewers.");
        for (Reviewer reviewer : availableReviewers) {
            reviewer.assignReview(data.get("title"));
        }

        System.out.println("[SubmissionController] Starting evaluation process.");
        evaluationManager.startEvaluation(data.get("title"), availableReviewers);
        return true;
    }
}

class Validator {
    public boolean validateFormat(Map<String, String> data) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return data.containsKey("title") && data.containsKey("content");
    }
}

class Database {
    private Map<String, Map<String, String>> submissions = new HashMap<>();
    private Map<String, Map<String, Object>> reviewers = new HashMap<>();
    private Map<String, List<Map<String, Object>>> scores = new HashMap<>();

    public Database() {
        reviewers.put("R1", Map.of("name", "Reviewer 1", "expertise", List.of("AI")));
        reviewers.put("R2", Map.of("name", "Reviewer 2", "expertise", List.of("ML")));
        reviewers.put("R3", Map.of("name", "Reviewer 3", "expertise", List.of("AI", "ML")));
    }

    public void saveSubmission(Map<String, String> data) {
        submissions.put(data.get("title"), data);
        System.out.println("[Database] Saved submission: " + data.get("title"));
    }

    public Map<String, Map<String, Object>> fetchReviewers() {
        System.out.println("[Database] Fetching all reviewers.");
        return reviewers;
    }

    public void saveScore(String submissionTitle, String reviewerName, int score) {
        scores.computeIfAbsent(submissionTitle, k -> new ArrayList<>()).add(Map.of("reviewer", reviewerName, "score", score));
        System.out.println("[Database] Saved score for " + submissionTitle + " by " + reviewerName + ": " + score);
    }
}

class ReviewerManager {
    private Database database;

    public ReviewerManager(Database database) {
        this.database = database;
    }

    public List<Reviewer> getAvailableReviewers() {
        Map<String, Map<String, Object>> allReviewersData = database.fetchReviewers();
        try {
            Thread.sleep(200); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<Reviewer> availableReviewers = new ArrayList<>();
        for (String name : allReviewersData.keySet()) {
            availableReviewers.add(new Reviewer(name, database));
        }
        return availableReviewers;
    }
}

class Reviewer {
    private String name;
    private Database database;

    public Reviewer(String name, Database database) {
        this.name = name;
        this.database = database;
    }

    public void assignReview(String submissionTitle) {
        System.out.println("[" + this.name + "] Assigned to review: " + submissionTitle);
    }

    public void submitScore(EvaluationManager evaluationManager, String submissionTitle) {
        Random random = new Random();
        int score = random.nextInt(10) + 1; 
        System.out.println("[" + this.name + "] Submitting score " + score + " for " + submissionTitle + ".");
        evaluationManager.submitScore(submissionTitle, this.name, score);
    }
}

class EvaluationManager {
    private Database database;
    private NotificationService notificationService;
    private Map<String, Map<String, Object>> evaluations = new HashMap<>();

    public EvaluationManager(Database database, NotificationService notificationService) {
        this.database = database;
        this.notificationService = notificationService;
    }

    public void startEvaluation(String submissionTitle, List<Reviewer> reviewers) {
        evaluations.put(submissionTitle, Map.of("reviewers", reviewers, "scores", new ArrayList<Integer>()));
        System.out.println("[EvaluationManager] Evaluation started for " + submissionTitle + ".");
        for (Reviewer reviewer : reviewers) {
            reviewer.submitScore(this, submissionTitle);
        }

        processEvaluation(submissionTitle);
    }

    public void submitScore(String submissionTitle, String reviewerName, int score) {
        database.saveScore(submissionTitle, reviewerName, score);
        ((List<Integer>) evaluations.get(submissionTitle).get("scores")).add(score);
    }

    private void processEvaluation(String submissionTitle) {
        List<Integer> scores = (List<Integer>) evaluations.get(submissionTitle).get("scores");
        if (scores.isEmpty()) {
            System.out.println("[EvaluationManager] No scores for " + submissionTitle + ", cannot process.");
            return;
        }

        double averageScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        System.out.println("[EvaluationManager] Average score for " + submissionTitle + ": " + averageScore);

        String outcome;
        if (averageScore > 7) {
            outcome = "accepted";
        } else if (averageScore > 5) {
            outcome = "revision";
        } else {
            outcome = "rejected";
        }
        System.out.println("[EvaluationManager] Outcome for " + submissionTitle + ": " + outcome);

        notificationService.notify(submissionTitle, outcome);
    }
}

class NotificationService {
    public void notify(String submissionTitle, String outcome) {
        System.out.println("[NotificationService] Notifying researcher about " + submissionTitle + " outcome: " + outcome + ".");
        System.out.println("[NotificationService] Notification sent for " + submissionTitle + ": " + outcome);
    }
}

public class BaselineImplementation {
    public static void main(String[] args) {
        Database database = new Database();
        NotificationService notificationService = new NotificationService();
        ReviewerManager reviewerManager = new ReviewerManager(database);
        EvaluationManager evaluationManager = new EvaluationManager(database, notificationService);
        Validator validator = new Validator();
        SubmissionController submissionController = new SubmissionController(validator, database, reviewerManager, evaluationManager, notificationService);
        UI ui = new UI(submissionController);

        Researcher researcher = new Researcher("Researcher", ui);

        Map<String, String> researchData = new HashMap<>();
        researchData.put("title", "Optimisation of Machine Learning Models");
        researchData.put("content", "This presents techniques for optimising machine learning model performance and efficiency.");

        researcher.submitResearchOutput(researchData);

        System.out.println("\n--- End of Baseline Simulation ---");
    }
}