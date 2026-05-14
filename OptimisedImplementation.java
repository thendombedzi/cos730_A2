import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

class SubmissionDecisionEngine {
    public Map.Entry<String, String> evaluateSubmission(Map<String, String> submissionData, boolean validationResult, boolean reviewersAvailable, List<Integer> scores) {
        boolean isValidFormat = validationResult;
        boolean hasReviewers = reviewersAvailable;
        double averageScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        if (!isValidFormat) {
            return Map.entry("invalid_format", "Submission format is invalid.");
        } else if (!hasReviewers) {
            return Map.entry("no_reviewers", "No suitable reviewers available.");
        } else if (averageScore > 7) {
            return Map.entry("accepted", "Submission accepted.");
        } else if (averageScore > 5) {
            return Map.entry("revised", "Submission requires revision.");
        } else {
            return Map.entry("rejected", "Submission rejected.");
        }
    }
}

class ResearcherOptimised {
    private String name;
    private SubmissionService submissionService;

    public ResearcherOptimised(String name, SubmissionService submissionService) {
        this.name = name;
        this.submissionService = submissionService;
    }

    public void submitResearchOutput(Map<String, String> data) {
        System.out.println("[" + this.name + "] Submitting research output: " + data.get("title"));
        submissionService.submit(data, this);
    }

    public void receiveNotification(String message) {
        System.out.println("[" + this.name + "] Received notification: " + message);
    }

    public String getName() {
    return name;
}
}

class SubmissionService {
    private ValidatorOptimised validator;
    private DatabaseOptimised database;
    private ReviewerManagerOptimised reviewerManager;
    private EvaluationManagerOptimised evaluationManager;
    private NotificationServiceOptimised notificationService;
    private SubmissionDecisionEngine decisionEngine;

    public SubmissionService(ValidatorOptimised validator, DatabaseOptimised database, ReviewerManagerOptimised reviewerManager, EvaluationManagerOptimised evaluationManager, NotificationServiceOptimised notificationService, SubmissionDecisionEngine decisionEngine) {
        this.validator = validator;
        this.database = database;
        this.reviewerManager = reviewerManager;
        this.evaluationManager = evaluationManager;
        this.notificationService = notificationService;
        this.decisionEngine = decisionEngine;
    }

    public boolean submit(Map<String, String> data, ResearcherOptimised researcher) {
        System.out.println("[SubmissionService] Validating submission format.");
        boolean isValid = validator.validateFormat(data);

        Map.Entry<String, String> initialOutcome = decisionEngine.evaluateSubmission(data, isValid, true, new ArrayList<>()); 

        if (initialOutcome.getKey().equals("invalid_format")) {
            System.out.println("[SubmissionService] " + initialOutcome.getValue());
            researcher.receiveNotification(initialOutcome.getValue());
            return false;
        }

        System.out.println("[SubmissionService] Submission valid. Saving to database.");
        database.saveSubmission(data);

        System.out.println("[SubmissionService] Getting available reviewers.");
        List<ReviewerOptimised> availableReviewers = reviewerManager.getAvailableReviewers();

        Map.Entry<String, String> reviewerOutcome = decisionEngine.evaluateSubmission(data, isValid, !availableReviewers.isEmpty(), new ArrayList<>()); 
        if (reviewerOutcome.getKey().equals("no_reviewers")) {
            System.out.println("[SubmissionService] " + reviewerOutcome.getValue());
            researcher.receiveNotification(reviewerOutcome.getValue());
            return false;
        }

        System.out.println("[SubmissionService] Assigning reviews to " + availableReviewers.size() + " reviewers.");
        for (ReviewerOptimised reviewer : availableReviewers) {
            reviewer.assignReview(data.get("title"));
        }

        System.out.println("[SubmissionService] Starting evaluation process.");
        evaluationManager.startEvaluation(data.get("title"), availableReviewers, researcher);
        return true;
    }
}

class ValidatorOptimised {
    public boolean validateFormat(Map<String, String> data) {
        try {
            Thread.sleep(50); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return data.containsKey("title") && data.containsKey("content");
    }
}

class DatabaseOptimised {
    private Map<String, Map<String, String>> submissions = new HashMap<>();
    private Map<String, Map<String, Object>> reviewers = new HashMap<>();
    private Map<String, List<Map<String, Object>>> scores = new HashMap<>();

    public DatabaseOptimised() {
        reviewers.put("R1", Map.of("name", "Reviewer 1", "expertise", List.of("AI")));
        reviewers.put("R2", Map.of("name", "Reviewer 2", "expertise", List.of("ML")));
        reviewers.put("R3", Map.of("name", "Reviewer 3", "expertise", List.of("AI", "ML")));
    }

    public void saveSubmission(Map<String, String> data) {
        submissions.put(data.get("title"), data);
        System.out.println("[DatabaseOptimised] Saved submission: " + data.get("title"));
    }

    public Map<String, Map<String, Object>> fetchReviewers() {
        System.out.println("[DatabaseOptimised] Fetching all reviewers.");
        return reviewers;
    }

    public void saveScore(String submissionTitle, String reviewerName, int score) {
        scores.computeIfAbsent(submissionTitle, k -> new ArrayList<>()).add(Map.of("reviewer", reviewerName, "score", score));
        System.out.println("[DatabaseOptimised] Saved score for " + submissionTitle + " by " + reviewerName + ": " + score);
    }
}

class ReviewerManagerOptimised {
    private DatabaseOptimised database;

    public ReviewerManagerOptimised(DatabaseOptimised database) {
        this.database = database;
    }

    public List<ReviewerOptimised> getAvailableReviewers() {
        Map<String, Map<String, Object>> allReviewersData = database.fetchReviewers();
        try {
            Thread.sleep(100); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<ReviewerOptimised> availableReviewers = new ArrayList<>();
        for (String name : allReviewersData.keySet()) {
            availableReviewers.add(new ReviewerOptimised(name));
        }
        return availableReviewers;
    }
}

class ReviewerOptimised {
    private String name;

    public ReviewerOptimised(String name) {
        this.name = name;
    }

    public void assignReview(String submissionTitle) {
        System.out.println("[" + this.name + "] Assigned to review: " + submissionTitle);
    }

    public void submitScore(EvaluationManagerOptimised evaluationManager, String submissionTitle) {
        Random random = new Random();
        int score = random.nextInt(10) + 1;
        System.out.println("[" + this.name + "] Submitting score " + score + " for " + submissionTitle + ".");
        evaluationManager.recordScore(submissionTitle, this.name, score);
    }
}

class EvaluationManagerOptimised {
    private DatabaseOptimised database;
    private NotificationServiceOptimised notificationService;
    private SubmissionDecisionEngine decisionEngine;
    private Map<String, Map<String, Object>> evaluations = new HashMap<>();

    public EvaluationManagerOptimised(DatabaseOptimised database, NotificationServiceOptimised notificationService, SubmissionDecisionEngine decisionEngine) {
        this.database = database;
        this.notificationService = notificationService;
        this.decisionEngine = decisionEngine;
    }

    public void startEvaluation(String submissionTitle, List<ReviewerOptimised> reviewers, ResearcherOptimised researcher) {
        evaluations.put(submissionTitle, Map.of("reviewers", reviewers, "scores", new ArrayList<Integer>(), "researcher", researcher));
        System.out.println("[EvaluationManagerOptimised] Evaluation started for " + submissionTitle + ".");
        for (ReviewerOptimised reviewer : reviewers) {
            reviewer.submitScore(this, submissionTitle);
        }

        processEvaluation(submissionTitle);
    }

    public void recordScore(String submissionTitle, String reviewerName, int score) {
        database.saveScore(submissionTitle, reviewerName, score);
        ((List<Integer>) evaluations.get(submissionTitle).get("scores")).add(score);
    }

    private void processEvaluation(String submissionTitle) {
        List<Integer> scores = (List<Integer>) evaluations.get(submissionTitle).get("scores");
        ResearcherOptimised researcher = (ResearcherOptimised) evaluations.get(submissionTitle).get("researcher");

        if (scores.isEmpty()) {
            System.out.println("[EvaluationManagerOptimised] No scores for " + submissionTitle + ", cannot process.");
            return;
        }

        Map.Entry<String, String> outcome = decisionEngine.evaluateSubmission(null, true, true, scores);
        System.out.println("[EvaluationManagerOptimised] Outcome for " + submissionTitle + ": " + outcome.getKey() + " - " + outcome.getValue());

        notificationService.notify(submissionTitle, outcome.getKey(), researcher);
    }
}

class NotificationServiceOptimised {
    public void notify(String submissionTitle, String outcome, ResearcherOptimised researcher) {
        String message = "Your submission \'" + submissionTitle + "\' has been " + outcome + ".";
        System.out.println("[NotificationServiceOptimised] Notifying researcher " + researcher.getName() + " about " + submissionTitle + " outcome: " + outcome + ".");
        researcher.receiveNotification(message);
    }
}

public class OptimisedImplementation {
    public static void main(String[] args) {
        DatabaseOptimised database = new DatabaseOptimised();
        SubmissionDecisionEngine decisionEngine = new SubmissionDecisionEngine();
        NotificationServiceOptimised notificationService = new NotificationServiceOptimised();
        ReviewerManagerOptimised reviewerManager = new ReviewerManagerOptimised(database);
        EvaluationManagerOptimised evaluationManager = new EvaluationManagerOptimised(database, notificationService, decisionEngine);
        ValidatorOptimised validator = new ValidatorOptimised();
        SubmissionService submissionService = new SubmissionService(validator, database, reviewerManager, evaluationManager, notificationService, decisionEngine);

        ResearcherOptimised researcher = new ResearcherOptimised("Researcher ", submissionService);

        Map<String, String> researchData = new HashMap<>();
        researchData.put("title", "Optimised Machine Learning Models for Medical Diagnosis");
        researchData.put("content", "This presents an optimised approach to machine learning models, significantly improving accuracy.");

        researcher.submitResearchOutput(researchData);

        System.out.println("\n--- End of Optimised Simulation ---");
    }
}