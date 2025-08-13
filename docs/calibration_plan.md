# Calibration Plan - Precision@k ≥ 0.90 for Displayed Picks

## Purpose
Establish time-series cross-validation framework with reliability curves, isotonic/Platt calibration, and threshold τ selection to achieve precision@k ≥ 0.90 for displayed rankings with proper abstention on low-confidence predictions.

## Calibration Framework Overview

### Objectives
1. **Precision Target**: Achieve ≥ 90% precision on displayed picks
2. **Calibrated Probabilities**: Convert raw scores to well-calibrated probabilities
3. **Confidence Thresholding**: Determine optimal τ threshold for abstention
4. **Temporal Robustness**: Ensure stability across different market regimes
5. **Asset Type Specificity**: Separate calibration for equities vs mutual funds

### Success Metrics
- **Precision@k**: P(success | displayed) ≥ 0.90 for top-k recommendations
- **Kendall's τ**: Rank correlation with actual future performance
- **Brier Score**: Calibration quality measure (lower is better)
- **Reliability**: Mean absolute difference between predicted and actual probabilities
- **Coverage**: Fraction of universe displayed after abstention

## Time-Series Cross-Validation Design

### Walk-Forward Validation Framework
```python
def walk_forward_validation(data, initial_window=252*2, step_size=21, forecast_horizon=63):
    """
    Time-series CV with purged gaps to prevent look-ahead bias
    
    Args:
        initial_window: Initial training period (2 years = 504 days)
        step_size: Step forward interval (3 weeks = 21 days)  
        forecast_horizon: Prediction horizon (3 months = 63 days)
    """
    
    validation_results = []
    
    for start_idx in range(initial_window, len(data) - forecast_horizon, step_size):
        # Training period
        train_start = start_idx - initial_window
        train_end = start_idx
        
        # Purged gap to prevent leakage (1 month buffer)
        purge_gap = 21
        
        # Test period  
        test_start = start_idx + purge_gap
        test_end = test_start + forecast_horizon
        
        if test_end > len(data):
            break
            
        # Extract train/test data with embargo
        train_data = data[train_start:train_end]
        test_data = data[test_start:test_end]
        
        # Train model and validate
        model = train_ranking_model(train_data)
        predictions = model.predict(test_data.features)
        actuals = calculate_future_performance(test_data, forecast_horizon)
        
        # Evaluate performance
        results = evaluate_predictions(predictions, actuals)
        validation_results.append(results)
    
    return validation_results
```

### Purged Cross-Validation Implementation
```java
@Service
public class TimeSeriesValidationService {
    
    /**
     * Purged time-series cross-validation to prevent leakage
     */
    public ValidationResults runPurgedTimeSeriesCV(
            LocalDate startDate, LocalDate endDate, ValidationConfig config) {
        
        List<ValidationFold> folds = generatePurgedFolds(startDate, endDate, config);
        List<FoldResult> results = new ArrayList<>();
        
        for (ValidationFold fold : folds) {
            log.info("Running validation fold: {} to {}", 
                fold.getTrainStart(), fold.getTestEnd());
            
            // Train ranking model on training data
            RankingModel model = trainModel(fold);
            
            // Generate predictions on test data
            List<RankingPrediction> predictions = model.predict(fold.getTestFeatures());
            
            // Calculate actual future performance (labels)
            List<PerformanceLabel> actuals = calculateFuturePerformance(
                fold.getTestAssets(), fold.getTestStart(), fold.getTestEnd(), config.getHorizon());
            
            // Evaluate fold performance
            FoldResult foldResult = evaluateFold(predictions, actuals, config);
            results.add(foldResult);
        }
        
        return aggregateValidationResults(results);
    }
    
    /**
     * Generate purged folds with embargo periods
     */
    private List<ValidationFold> generatePurgedFolds(
            LocalDate start, LocalDate end, ValidationConfig config) {
        
        List<ValidationFold> folds = new ArrayList<>();
        LocalDate currentStart = start.plusDays(config.getInitialWindowDays());
        
        while (currentStart.plusDays(config.getEmbargoGapDays() + config.getHorizonDays()).isBefore(end)) {
            ValidationFold fold = ValidationFold.builder()
                .trainStart(currentStart.minusDays(config.getInitialWindowDays()))
                .trainEnd(currentStart)
                .testStart(currentStart.plusDays(config.getEmbargoGapDays()))
                .testEnd(currentStart.plusDays(config.getEmbargoGapDays() + config.getHorizonDays()))
                .build();
                
            folds.add(fold);
            currentStart = currentStart.plusDays(config.getStepSizeDays());
        }
        
        log.info("Generated {} validation folds with {} day embargo", 
            folds.size(), config.getEmbargoGapDays());
        return folds;
    }
}
```

## Calibration Methods

### Isotonic Regression Calibration
```java
@Component
public class IsotonicCalibrator {
    
    /**
     * Fit isotonic regression to calibrate scores
     * Monotonic mapping: f(score) → probability
     */
    public IsotonicRegressionModel fitIsotonicCalibration(
            List<Double> scores, List<Boolean> outcomes) {
        
        // Sort by scores while maintaining outcome correspondence
        List<ScoreOutcomePair> pairs = IntStream.range(0, scores.size())
            .mapToObj(i -> new ScoreOutcomePair(scores.get(i), outcomes.get(i)))
            .sorted(Comparator.comparing(ScoreOutcomePair::getScore))
            .collect(Collectors.toList());
        
        // Pool Adjacent Violators Algorithm (PAVA)
        List<Double> calibratedScores = new ArrayList<>();
        List<Double> calibratedProbs = new ArrayList<>();
        
        int i = 0;
        while (i < pairs.size()) {
            int j = i + 1;
            double sumY = pairs.get(i).getOutcome() ? 1.0 : 0.0;
            double sumW = 1.0;
            
            // Find violating sequence
            while (j < pairs.size()) {
                double currentAvg = sumY / sumW;
                double nextValue = pairs.get(j).getOutcome() ? 1.0 : 0.0;
                
                if (nextValue >= currentAvg) break;
                
                sumY += nextValue;
                sumW += 1.0;
                j++;
            }
            
            // Average the violating sequence
            double avgProb = sumY / sumW;
            for (int k = i; k < j; k++) {
                calibratedScores.add(pairs.get(k).getScore());
                calibratedProbs.add(avgProb);
            }
            
            i = j;
        }
        
        return new IsotonicRegressionModel(calibratedScores, calibratedProbs);
    }
    
    /**
     * Apply isotonic calibration to new scores
     */
    public Double calibrate(IsotonicRegressionModel model, Double score) {
        if (score == null) return null;
        
        // Find position in calibrated scores using binary search
        int pos = Collections.binarySearch(model.getScores(), score);
        
        if (pos >= 0) {
            return model.getProbabilities().get(pos);
        } else {
            // Interpolate between adjacent points
            int insertPos = -(pos + 1);
            if (insertPos == 0) {
                return model.getProbabilities().get(0);
            } else if (insertPos >= model.getScores().size()) {
                return model.getProbabilities().get(model.getScores().size() - 1);
            } else {
                return interpolateLinear(score, 
                    model.getScores().get(insertPos - 1), 
                    model.getScores().get(insertPos),
                    model.getProbabilities().get(insertPos - 1), 
                    model.getProbabilities().get(insertPos));
            }
        }
    }
}
```

### Platt Scaling Alternative
```java
@Component
public class PlattScalingCalibrator {
    
    /**
     * Platt scaling: fit sigmoid to map scores → probabilities
     * P(y=1|score) = 1 / (1 + exp(A*score + B))
     */
    public PlattScalingModel fitPlattScaling(
            List<Double> scores, List<Boolean> outcomes) {
        
        // Convert to arrays for optimization
        double[] x = scores.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = outcomes.stream().mapToDouble(b -> b ? 1.0 : 0.0).toArray();
        
        // Optimize sigmoid parameters A and B using maximum likelihood
        OptimizationResult result = optimizeSigmoidParameters(x, y);
        
        return new PlattScalingModel(result.getA(), result.getB());
    }
    
    /**
     * Apply Platt scaling calibration
     */
    public Double calibrate(PlattScalingModel model, Double score) {
        if (score == null) return null;
        
        double exponent = model.getA() * score + model.getB();
        return 1.0 / (1.0 + Math.exp(-exponent));
    }
    
    /**
     * Optimize sigmoid parameters using Newton-Raphson
     */
    private OptimizationResult optimizeSigmoidParameters(double[] scores, double[] labels) {
        // Implementation of Newton-Raphson optimization for sigmoid parameters
        // Initial parameter estimates
        double A = 0.0;
        double B = Math.log((countPositives(labels) + 1.0) / (countNegatives(labels) + 1.0));
        
        // Iterative optimization (simplified implementation)
        for (int iter = 0; iter < 100; iter++) {
            // Calculate gradient and Hessian
            double[] gradient = calculateGradient(scores, labels, A, B);
            double[][] hessian = calculateHessian(scores, labels, A, B);
            
            // Newton-Raphson update
            double[] update = solveLinearSystem(hessian, gradient);
            A -= update[0];
            B -= update[1];
            
            // Check convergence
            if (Math.abs(update[0]) + Math.abs(update[1]) < 1e-6) break;
        }
        
        return new OptimizationResult(A, B);
    }
}
```

## Threshold Selection (τ)

### Precision-Recall Threshold Optimization
```java
@Service
public class ThresholdOptimizationService {
    
    /**
     * Find optimal confidence threshold τ to achieve target precision
     */
    public ThresholdOptimizationResult findOptimalThreshold(
            List<ValidationResult> validationResults, double targetPrecision) {
        
        // Combine all validation predictions
        List<CalibrationPoint> allPredictions = validationResults.stream()
            .flatMap(vr -> vr.getPredictions().stream())
            .collect(Collectors.toList());
        
        // Sort by confidence (descending)
        allPredictions.sort(Comparator.comparing(CalibrationPoint::getConfidence).reversed());
        
        // Evaluate precision at different confidence thresholds
        List<ThresholdResult> thresholdResults = new ArrayList<>();
        
        for (double threshold = 0.50; threshold <= 0.95; threshold += 0.01) {
            ThresholdResult result = evaluateThreshold(allPredictions, threshold);
            thresholdResults.add(result);
            
            // Early stopping if we achieve target precision
            if (result.getPrecision() >= targetPrecision) {
                log.info("Found threshold {} achieving precision {:.3f}", 
                    threshold, result.getPrecision());
                break;
            }
        }
        
        // Select optimal threshold balancing precision and coverage
        ThresholdResult optimal = selectOptimalThreshold(thresholdResults, targetPrecision);
        
        return ThresholdOptimizationResult.builder()
            .optimalThreshold(optimal.getThreshold())
            .achievedPrecision(optimal.getPrecision())
            .coverage(optimal.getCoverage())
            .recall(optimal.getRecall())
            .f1Score(optimal.getF1Score())
            .allResults(thresholdResults)
            .build();
    }
    
    /**
     * Evaluate precision/recall at specific threshold
     */
    private ThresholdResult evaluateThreshold(List<CalibrationPoint> predictions, double threshold) {
        List<CalibrationPoint> displayed = predictions.stream()
            .filter(p -> p.getConfidence() >= threshold)
            .collect(Collectors.toList());
        
        if (displayed.isEmpty()) {
            return ThresholdResult.builder()
                .threshold(threshold)
                .precision(0.0)
                .recall(0.0)
                .coverage(0.0)
                .f1Score(0.0)
                .build();
        }
        
        long truePositives = displayed.stream()
            .mapToLong(p -> p.isActualSuccess() ? 1 : 0)
            .sum();
        
        long allPositives = predictions.stream()
            .mapToLong(p -> p.isActualSuccess() ? 1 : 0)
            .sum();
        
        double precision = (double) truePositives / displayed.size();
        double recall = allPositives > 0 ? (double) truePositives / allPositives : 0.0;
        double coverage = (double) displayed.size() / predictions.size();
        double f1Score = 2 * precision * recall / (precision + recall);
        
        return ThresholdResult.builder()
            .threshold(threshold)
            .precision(precision)
            .recall(recall)
            .coverage(coverage)
            .f1Score(f1Score)
            .displayedCount(displayed.size())
            .totalCount(predictions.size())
            .build();
    }
    
    /**
     * Select optimal threshold considering precision target and coverage
     */
    private ThresholdResult selectOptimalThreshold(
            List<ThresholdResult> results, double targetPrecision) {
        
        // Filter results meeting precision target
        List<ThresholdResult> validResults = results.stream()
            .filter(r -> r.getPrecision() >= targetPrecision)
            .collect(Collectors.toList());
        
        if (validResults.isEmpty()) {
            // If no threshold meets target, return highest precision
            return results.stream()
                .max(Comparator.comparing(ThresholdResult::getPrecision))
                .orElse(results.get(0));
        }
        
        // Among valid results, maximize coverage (or F1 score)
        return validResults.stream()
            .max(Comparator.comparing(ThresholdResult::getCoverage))
            .orElse(validResults.get(0));
    }
}
```

## Reliability Curves and Calibration Assessment

### Calibration Quality Metrics
```java
@Component
public class CalibrationQualityAssessor {
    
    /**
     * Calculate Brier Score: BS = (1/N) * Σ(p_i - o_i)²
     * where p_i = predicted probability, o_i = actual outcome (0 or 1)
     */
    public double calculateBrierScore(List<Double> predictedProbs, List<Boolean> actualOutcomes) {
        if (predictedProbs.size() != actualOutcomes.size()) {
            throw new IllegalArgumentException("Prediction and outcome lists must have same size");
        }
        
        double sumSquaredError = 0.0;
        for (int i = 0; i < predictedProbs.size(); i++) {
            double predicted = predictedProbs.get(i);
            double actual = actualOutcomes.get(i) ? 1.0 : 0.0;
            sumSquaredError += Math.pow(predicted - actual, 2);
        }
        
        return sumSquaredError / predictedProbs.size();
    }
    
    /**
     * Generate reliability curve data for calibration assessment
     */
    public ReliabilityCurve generateReliabilityCurve(
            List<Double> predictedProbs, List<Boolean> actualOutcomes, int numBins) {
        
        List<ReliabilityBin> bins = new ArrayList<>();
        
        // Create probability bins
        for (int i = 0; i < numBins; i++) {
            double binStart = (double) i / numBins;
            double binEnd = (double) (i + 1) / numBins;
            
            // Collect predictions in this bin
            List<Integer> binIndices = IntStream.range(0, predictedProbs.size())
                .filter(idx -> predictedProbs.get(idx) >= binStart && predictedProbs.get(idx) < binEnd)
                .boxed()
                .collect(Collectors.toList());
            
            if (binIndices.isEmpty()) continue;
            
            // Calculate bin statistics
            double meanPredicted = binIndices.stream()
                .mapToDouble(idx -> predictedProbs.get(idx))
                .average()
                .orElse(0.0);
            
            double meanActual = binIndices.stream()
                .mapToDouble(idx -> actualOutcomes.get(idx) ? 1.0 : 0.0)
                .average()
                .orElse(0.0);
            
            bins.add(ReliabilityBin.builder()
                .binStart(binStart)
                .binEnd(binEnd)
                .count(binIndices.size())
                .meanPredicted(meanPredicted)
                .meanActual(meanActual)
                .calibrationError(Math.abs(meanPredicted - meanActual))
                .build());
        }
        
        return ReliabilityCurve.builder()
            .bins(bins)
            .expectedCalibrationError(calculateECE(bins))
            .maxCalibrationError(calculateMCE(bins))
            .build();
    }
    
    /**
     * Calculate Expected Calibration Error (ECE)
     */
    private double calculateECE(List<ReliabilityBin> bins) {
        int totalCount = bins.stream().mapToInt(ReliabilityBin::getCount).sum();
        
        return bins.stream()
            .mapToDouble(bin -> (bin.getCount() / (double) totalCount) * bin.getCalibrationError())
            .sum();
    }
    
    /**
     * Calculate Maximum Calibration Error (MCE)
     */
    private double calculateMCE(List<ReliabilityBin> bins) {
        return bins.stream()
            .mapToDouble(ReliabilityBin::getCalibrationError)
            .max()
            .orElse(0.0);
    }
}
```

## Asset-Specific Calibration

### Separate Models by Asset Type
```java
@Service
public class AssetSpecificCalibrationService {
    
    private final Map<AssetType, CalibrationModel> calibrationModels = new HashMap<>();
    
    /**
     * Train separate calibration models for equity vs mutual fund
     */
    public void trainAssetSpecificModels(List<ValidationResult> validationResults) {
        
        // Separate validation results by asset type
        Map<AssetType, List<ValidationResult>> resultsByType = validationResults.stream()
            .collect(Collectors.groupingBy(ValidationResult::getAssetType));
        
        for (Map.Entry<AssetType, List<ValidationResult>> entry : resultsByType.entrySet()) {
            AssetType assetType = entry.getKey();
            List<ValidationResult> typeResults = entry.getValue();
            
            log.info("Training calibration model for {}: {} samples", 
                assetType, typeResults.size());
            
            // Extract scores and outcomes
            List<Double> scores = typeResults.stream()
                .map(ValidationResult::getRawScore)
                .collect(Collectors.toList());
            
            List<Boolean> outcomes = typeResults.stream()
                .map(ValidationResult::isSuccess)
                .collect(Collectors.toList());
            
            // Train calibration model (isotonic or Platt)
            CalibrationModel model = trainCalibrationModel(scores, outcomes, assetType);
            calibrationModels.put(assetType, model);
            
            // Evaluate model quality
            evaluateCalibrationModel(model, scores, outcomes, assetType);
        }
    }
    
    /**
     * Apply asset-specific calibration
     */
    public Double calibrate(Double rawScore, AssetType assetType) {
        CalibrationModel model = calibrationModels.get(assetType);
        
        if (model == null) {
            log.warn("No calibration model available for {}", assetType);
            return sigmoidTransform(rawScore); // Fallback
        }
        
        return model.calibrate(rawScore);
    }
}
```

## Performance Evaluation Framework

### Comprehensive Evaluation Metrics
```java
@Service
public class CalibrationEvaluationService {
    
    /**
     * Comprehensive evaluation of calibration quality
     */
    public CalibrationEvaluationReport evaluateCalibration(
            List<ValidationResult> validationResults, double threshold) {
        
        // Basic performance metrics
        PerformanceMetrics performance = calculatePerformanceMetrics(validationResults, threshold);
        
        // Calibration quality metrics
        CalibrationQualityMetrics calibration = calculateCalibrationMetrics(validationResults);
        
        // Ranking quality metrics
        RankingQualityMetrics ranking = calculateRankingMetrics(validationResults);
        
        // Temporal stability analysis
        TemporalStabilityMetrics temporal = calculateTemporalStability(validationResults);
        
        return CalibrationEvaluationReport.builder()
            .performance(performance)
            .calibration(calibration)
            .ranking(ranking)
            .temporal(temporal)
            .threshold(threshold)
            .totalSamples(validationResults.size())
            .evaluationDate(LocalDate.now())
            .build();
    }
    
    private RankingQualityMetrics calculateRankingMetrics(List<ValidationResult> results) {
        // Calculate Kendall's τ between predicted and actual rankings
        List<Double> predictedRanks = results.stream()
            .map(ValidationResult::getPredictedRank)
            .collect(Collectors.toList());
        
        List<Double> actualRanks = results.stream()
            .map(ValidationResult::getActualRank)
            .collect(Collectors.toList());
        
        double kendallTau = calculateKendallTau(predictedRanks, actualRanks);
        
        // Information Coefficient (Spearman correlation)
        double informationCoefficient = calculateSpearmanCorrelation(predictedRanks, actualRanks);
        
        // Hit rate (top quintile)
        double topQuintileHitRate = calculateTopQuintileHitRate(results);
        
        return RankingQualityMetrics.builder()
            .kendallTau(kendallTau)
            .informationCoefficient(informationCoefficient)
            .topQuintileHitRate(topQuintileHitRate)
            .build();
    }
}
```

## Configuration and Hyperparameters

### Calibration Configuration
```yaml
calibration:
  # Validation setup
  validation:
    initial_window_days: 504    # 2 years initial training
    step_size_days: 21          # 3 weeks step forward
    horizon_days: 63            # 3 months prediction horizon
    embargo_gap_days: 21        # 1 month purge gap
    min_validation_samples: 1000
    
  # Threshold optimization
  threshold:
    target_precision: 0.90      # 90% precision target
    min_coverage: 0.30          # Minimum 30% coverage
    search_range: [0.50, 0.95]  # Threshold search range
    search_step: 0.01           # Search step size
    
  # Calibration methods
  methods:
    primary: "isotonic"         # isotonic, platt, or ensemble
    fallback: "sigmoid"         # Simple sigmoid fallback
    ensemble_weights: [0.6, 0.4] # Isotonic + Platt ensemble
    
  # Model parameters
  isotonic:
    min_samples_per_bin: 10     # Minimum samples for reliability
    
  platt:
    max_iterations: 100         # Newton-Raphson iterations
    convergence_tolerance: 1e-6 # Convergence threshold
    
  # Asset-specific settings
  asset_specific:
    enable: true                # Use separate models by asset type
    min_samples_per_type: 500   # Minimum samples per asset type
    
  # Quality thresholds
  quality:
    min_brier_score: 0.25       # Maximum acceptable Brier score
    max_ece: 0.05              # Maximum Expected Calibration Error
    min_kendall_tau: 0.05      # Minimum ranking correlation
```

## Success Criteria and Validation

### Phase M3 Acceptance Criteria
1. **Precision@k ≥ 0.90**: Achieved on displayed recommendations
2. **Kendall's τ ≥ 0.05**: Significant rank correlation with future performance  
3. **Brier Score ≤ 0.25**: Good calibration quality
4. **Coverage ≥ 30%**: Reasonable fraction of universe displayed
5. **Temporal Stability**: Consistent performance across market regimes

### Validation Report Template
```markdown
# Calibration Validation Report

## Executive Summary
- **Precision@k**: {achieved_precision:.1%} (Target: ≥90%)
- **Optimal Threshold τ**: {optimal_tau:.2f}
- **Coverage**: {coverage:.1%} of universe displayed
- **Kendall's τ**: {kendall_tau:.3f}
- **Brier Score**: {brier_score:.3f}

## Model Performance
- **True Positives**: {tp} recommendations
- **False Positives**: {fp} recommendations  
- **Abstentions**: {abstentions} due to low confidence

## Calibration Quality
- **Expected Calibration Error**: {ece:.3f}
- **Maximum Calibration Error**: {mce:.3f}
- **Reliability Curve**: [Attach calibration plot]

## Recommendations
- {recommendations}
```

---

**Document Version**: 1.0  
**Last Updated**: 2025-08-10  
**Implementation Target**: Phase M3  
**Dependencies**: Historical validation data, statistical libraries, optimization frameworks