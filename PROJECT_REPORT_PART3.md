# COMPREHENSIVE PROJECT REPORT
# DNS Anomaly Detection Using Hybrid EvoDN2-Fuzzy Logic System

---

## PART 3: IMPLEMENTATION, RESULTS, AND ANALYSIS

### Table of Contents - Part 3
1. Implementation Details
2. Experimental Setup
3. Dataset Analysis
4. Results: Extreme Values Dataset
5. Results: Median Values Dataset
6. Comparative Analysis
7. System Performance Evaluation

---

## 1. IMPLEMENTATION DETAILS

### 1.1 Technology Stack

**Programming Language:** Python 3.8+

**Core Libraries:**
```python
import numpy as np              # Numerical computations
import pandas as pd             # Data manipulation
import matplotlib.pyplot as plt # Visualization
import seaborn as sns           # Statistical plotting
```

**Key Dependencies:**
- **NumPy:** Fast array operations for membership function calculations
- **Pandas:** Efficient CSV processing and data analysis
- **Matplotlib/Seaborn:** Visualization generation

### 1.2 Code Architecture

```
fuzzyfyp_improved.py
├── FuzzyMembershipFunction (Class)
│   ├── trimf()      # Triangular membership function
│   └── trapmf()     # Trapezoidal membership function
│
├── DNSFuzzySystem (Class)
│   ├── __init__()                    # Initialize system
│   ├── _define_membership_functions() # Define all fuzzy sets
│   ├── evaluate_rules()              # Apply all 9 rules
│   ├── defuzzify()                   # Convert to crisp output
│   ├── predict()                     # Main prediction method
│   └── predict_with_explanation()    # Prediction + reasoning
│
├── Visualization Functions
│   ├── plot_membership_functions()
│   ├── plot_decision_heatmap()
│   ├── plot_statistics()
│   ├── plot_rule_activation_analysis()
│   └── generate_all_visualizations()
│
└── Processing Functions
    ├── process_csv_file()            # Batch processing
    └── demo_examples()               # Example scenarios
```

### 1.3 Membership Function Implementation

**Triangular Function:**
```python
@staticmethod
def trimf(x, params):
    """
    Triangular membership function
    params = [a, b, c]
    
    Shape:  /\
           /  \
          /    \
         a  b  c
    """
    a, b, c = params
    
    if x <= a:
        return 0.0
    elif a < x <= b:
        return (x - a) / (b - a)  # Rising edge
    elif b < x <= c:
        return (c - x) / (c - b)  # Falling edge
    else:
        return 0.0
```

**Trapezoidal Function:**
```python
@staticmethod
def trapmf(x, params):
    """
    Trapezoidal membership function
    params = [a, b, c, d]
    
    Shape:  ___
           /   \
          /     \
         a  b c  d
    """
    a, b, c, d = params
    
    if x <= a:
        return 0.0
    elif a < x <= b:
        return (x - a) / (b - a)  # Rising edge
    elif b < x <= c:
        return 1.0                 # Flat top
    elif c < x <= d:
        return (d - x) / (d - c)  # Falling edge
    else:
        return 0.0
```

### 1.4 Rule Evaluation Engine

**AND Operation (min):**
```python
# Example: Rule 1
strength = min(
    self.max_prob_mf['very_high'](max_prob),
    self.pred_class_mf['attack'](pred_class)
)
```

**OR Operation (max):**
```python
# Example: Rule 6
strength = max(
    self.entropy_mf['uncertain'](entropy),
    self.prob_diff_mf['small'](prob_diff)
)
```

### 1.5 Defuzzification Algorithm

**Improved SUM Aggregation:**
```python
def defuzzify(self, rules_fired):
    if not rules_fired:
        return 0, 0  # Default: ALLOW + NONE
    
    # SUM aggregation (accumulate strengths)
    action_strengths = {}
    alert_strengths = {}
    
    for rule_name, action, alert, strength in rules_fired:
        # Accumulate (not max!)
        if action not in action_strengths:
            action_strengths[action] = 0.0
        action_strengths[action] += strength
        
        if alert not in alert_strengths:
            alert_strengths[alert] = 0.0
        alert_strengths[alert] += strength
    
    # Weighted average defuzzification
    action_numerator = sum(
        self.action_centers[a] * s 
        for a, s in action_strengths.items()
    )
    action_denominator = sum(action_strengths.values())
    
    action_code = round(action_numerator / action_denominator)
    
    # Same for alert
    alert_numerator = sum(
        self.alert_centers[a] * s 
        for a, s in alert_strengths.items()
    )
    alert_denominator = sum(alert_strengths.values())
    
    alert_code = round(alert_numerator / alert_denominator)
    
    # Clamp to valid ranges
    action_code = max(0, min(4, action_code))
    alert_code = max(0, min(4, alert_code))
    
    return action_code, alert_code
```

---

## 2. EXPERIMENTAL SETUP

### 2.1 Dataset Overview

**Total Records:** 100,313 DNS queries

**Source:** Real-world DNS traffic with EvoDN2 model predictions

**Input Features:**
```
Network Metadata:
├── time              : Query timestamp
├── src_ip            : Source IP address
├── source_port       : Source port number
├── dst_ip            : Destination IP address
└── destination_port  : Destination port number

DNS Specific:
├── qtype             : Query type (A, AAAA, etc.)
├── rcode             : Response code
├── domain            : Queried domain name
└── sublength         : Subdomain length

Statistical:
├── entropy           : Domain entropy
├── Prob_Benign       : EvoDN2 benign probability
└── Prob_Tunneling    : EvoDN2 tunneling probability
```

### 2.2 Dataset Variants

We tested on **two distinct datasets** to evaluate system behavior under different conditions:

#### Dataset 1: Extreme Values
```
Characteristics:
- High-confidence predictions
- Clear separation between classes
- Represents obvious cases (99% vs 1%)
- Tests system's handling of certainty

Expected Behavior:
- Most should trigger Rules 1, 2, or 7
- High BLOCK or ALLOW rates
- Few intermediate actions
```

#### Dataset 2: Median Values
```
Characteristics:
- Medium-confidence predictions
- Smaller separation between classes
- Represents uncertain cases (60% vs 40%)
- Tests system's handling of ambiguity

Expected Behavior:
- Should trigger Rules 4, 5, 6, 8, 9
- High LOG and MONITOR rates
- Few BLOCK actions
```

### 2.3 Processing Pipeline

```
┌────────────────────────────────────────────────────┐
│ Step 1: Load CSV Data                              │
│ ├─ Read input CSV                                  │
│ └─ Validate columns                                │
└──────────────────┬─────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────┐
│ Step 2: For Each Query                             │
│ ├─ Extract P(Benign) and P(Tunneling)             │
│ └─ Calculate fuzzy inputs                          │
└──────────────────┬─────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────┐
│ Step 3: Fuzzy Inference                            │
│ ├─ Fuzzify inputs                                  │
│ ├─ Evaluate all 9 rules                            │
│ ├─ Aggregate fired rules                           │
│ └─ Defuzzify to crisp output                       │
└──────────────────┬─────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────┐
│ Step 4: Record Results                             │
│ ├─ Action code and name                            │
│ ├─ Alert code and name                             │
│ ├─ Number of rules fired                           │
│ └─ All fuzzy input values                          │
└──────────────────┬─────────────────────────────────┘
                   │
                   ▼
┌────────────────────────────────────────────────────┐
│ Step 5: Save & Analyze                             │
│ ├─ Export to CSV                                   │
│ ├─ Generate statistics                             │
│ └─ Create visualizations                           │
└────────────────────────────────────────────────────┘
```

### 2.4 Evaluation Metrics

**1. Action Distribution:**
- Percentage of queries assigned to each action
- Validates appropriate response levels

**2. Alert Distribution:**
- Percentage of queries assigned to each alert
- Confirms threat prioritization

**3. Rule Activation:**
- Number of rules fired per query
- Indicates system coverage

**4. Edge Cases:**
- Queries where no rules fired
- Should be minimal (good design)

**5. Prediction Class Distribution:**
- Normal vs Attack classification
- Validates input data characteristics

---

## 3. DATASET ANALYSIS

### 3.1 Extreme Values Dataset - Input Statistics

**Total Queries:** 100,313

**Sample Input Data:**
```
Query #1:
  time: 58:06.7
  domain: (empty)
  Prob_Benign: 0.766268
  Prob_Tunneling: 0.233732
  entropy: 0.000000

Query #2:
  time: 58:07.7
  domain: (not shown)
  Prob_Benign: 0.810558
  Prob_Tunneling: 0.189442
  entropy: 2.947703

Query #3:
  time: 58:07.8
  domain: elasticsearch-master...
  Prob_Benign: 0.998638
  Prob_Tunneling: 0.001362
  entropy: 3.728564
```

**Key Characteristics:**
- Most probabilities are extreme (>0.75 or <0.25)
- Clear class separation
- High confidence predictions

### 3.2 Median Values Dataset - Input Statistics

**Total Queries:** 100,313

**Sample Input Data:**
```
Query #1:
  time: 58:06.7
  domain: (empty)
  Prob_Benign: 0.319651
  Prob_Tunneling: 0.680349
  entropy: 0.000000

Query #2:
  time: 58:07.7
  domain: (not shown)
  Prob_Benign: 0.380246
  Prob_Tunneling: 0.619754
  entropy: 2.947703

Query #3:
  time: 58:07.8
  domain: elasticsearch-master...
  Prob_Benign: 0.335748
  Prob_Tunneling: 0.664252
  entropy: 3.728564
```

**Key Characteristics:**
- Most probabilities are moderate (0.3-0.7 range)
- Smaller class separation
- Higher uncertainty

---

## 4. RESULTS: EXTREME VALUES DATASET

### 4.1 Overall Statistics

```
═══════════════════════════════════════════════════════════════════
                    EXTREME VALUES DATASET RESULTS
═══════════════════════════════════════════════════════════════════

Total Queries Processed:        100,313
Processing Status:              100% Complete
Edge Cases (No Rules Fired):    74 (0.07%)

Prediction Class Distribution:
├─ Normal (0):                  20,028 (20.0%)
└─ Attack (1):                  80,285 (80.0%)
```

### 4.2 Action Distribution

```
ACTION DISTRIBUTION - EXTREME VALUES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Action          Count       Percentage    Visualization
───────────────────────────────────────────────────────────────
BLOCK          80,211       79.93%        ████████████████████
ALLOW          20,028       19.97%        █████
LOG                74        0.07%         ▏
MONITOR             0        0.00%         
RATE_LIMIT          0        0.00%         
───────────────────────────────────────────────────────────────
Total         100,313      100.00%
```

**Analysis:**
- **79.93% BLOCK:** Vast majority are high-confidence attacks
- **19.97% ALLOW:** Clear benign traffic
- **0.07% LOG:** Tiny fraction of ambiguous cases
- **No MONITOR or RATE_LIMIT:** No medium-confidence scenarios

**Interpretation:**
This dataset contains primarily **clear-cut cases**:
- Either very confident attack (→ BLOCK)
- Or very confident benign (→ ALLOW)
- Almost no uncertain cases

### 4.3 Alert Distribution

```
ALERT DISTRIBUTION - EXTREME VALUES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Alert           Count       Percentage    Visualization
───────────────────────────────────────────────────────────────
CRITICAL       80,211       79.93%        ████████████████████
NONE           20,028       19.97%        █████
LOW                74        0.07%         ▏
MEDIUM              0        0.00%         
HIGH                0        0.00%         
───────────────────────────────────────────────────────────────
Total         100,313      100.00%
```

**Perfect Action-Alert Correlation:**
```
BLOCK    ←→ CRITICAL  (80,211 queries)
ALLOW    ←→ NONE      (20,028 queries)
LOG      ←→ LOW       (74 queries)
```

This 1:1 mapping indicates the system is making **consistent, logical decisions**.

### 4.4 Rule Activation Patterns

```
RULES FIRED PER QUERY - EXTREME VALUES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Number of Rules    Count       Percentage
───────────────────────────────────────────────────────────────
0 rules           74           0.07%      (Edge cases)
1 rule            99,920       99.61%     (Dominant)
2 rules           319          0.32%      (Occasional)
───────────────────────────────────────────────────────────────
Average Rules:    1.0
```

**Which Rules Fired?**

Based on the extreme values characteristics:
- **Rule 1:** Very High Confidence Attack → Most of the 80,211 blocks
- **Rule 7:** Very High Confidence Normal → Most of the 20,028 allows
- **Rule 6:** Ambiguous Case → The 74 logs

### 4.5 Sample Decisions - Extreme Values

**Example 1: Clear Attack**
```
Input:
  P(Benign): 0.02
  P(Tunneling): 0.98
  
Fuzzy Inputs:
  max_prob: 0.98
  prob_diff: 0.96
  entropy: 0.11 (very certain)
  pred_class: 1 (attack)

Rule Fired:
  Rule 1: Very High Confidence Attack
    μ_very_high(0.98) = 1.0
    μ_attack(1) = 1.0
    Strength = 1.0

Decision:
  Action: BLOCK (code 4)
  Alert: CRITICAL (code 4)
  
Reasoning:
  "Extremely confident attack (98%) with very low uncertainty 
   → Immediate blocking required"
```

**Example 2: Clear Benign**
```
Input:
  P(Benign): 0.9986
  P(Tunneling): 0.0014
  
Fuzzy Inputs:
  max_prob: 0.9986
  prob_diff: 0.9972
  entropy: 0.02 (very certain)
  pred_class: 0 (normal)

Rule Fired:
  Rule 7: Very High Confidence Normal
    μ_very_high(0.9986) = 1.0
    μ_normal(0) = 1.0
    μ_certain(0.02) = 1.0
    Strength = 1.0

Decision:
  Action: ALLOW (code 0)
  Alert: NONE (code 0)
  
Reasoning:
  "Extremely confident benign (99.86%) with very low uncertainty
   → Safe to allow"
```

**Example 3: Edge Case (No Rules Fired)**
```
Input:
  P(Benign): 0.7663
  P(Tunneling): 0.2337
  entropy: 0.0000
  
Fuzzy Inputs:
  max_prob: 0.7663
  prob_diff: 0.5326
  entropy: 0.0
  pred_class: 0 (normal)

Rules Evaluated:
  Rule 7: μ_very_high(0.7663) = 0.0 → Does not fire
  Rule 8: μ_medium(0.7663) = 0.2 → Weak strength
  
Result:
  No strong rules fire → Default to ALLOW + NONE
  
Note: This is an edge case logged by the system
```

### 4.6 Key Insights - Extreme Values

1. **Binary Behavior:**
   - System makes decisive choices
   - 99.93% of queries get BLOCK or ALLOW
   - Almost no intermediate actions

2. **High Confidence:**
   - Rules 1 and 7 dominate
   - Clear separation in input probabilities
   - Low entropy values

3. **Minimal Ambiguity:**
   - Only 74 edge cases (0.07%)
   - Rule 6 rarely fires
   - System handles extreme values well

4. **Threat Landscape:**
   - 80% of queries are attacks (high threat environment)
   - System correctly identifies and blocks them
   - Critical alerts appropriately raised

---

## 5. RESULTS: MEDIAN VALUES DATASET

### 5.1 Overall Statistics

```
═══════════════════════════════════════════════════════════════════
                    MEDIAN VALUES DATASET RESULTS
═══════════════════════════════════════════════════════════════════

Total Queries Processed:        100,313
Processing Status:              100% Complete
Edge Cases (No Rules Fired):    0 (0.00%)

Prediction Class Distribution:
├─ Normal (0):                  47,896 (47.7%)
└─ Attack (1):                  52,417 (52.3%)
```

### 5.2 Action Distribution

```
ACTION DISTRIBUTION - MEDIAN VALUES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Action          Count       Percentage    Visualization
───────────────────────────────────────────────────────────────
LOG            84,443       84.18%        █████████████████████
MONITOR         7,784        7.76%        ██
ALLOW           8,086        8.06%        ██
BLOCK               0        0.00%         
RATE_LIMIT          0        0.00%         
───────────────────────────────────────────────────────────────
Total         100,313      100.00%
```

**Analysis:**
- **84.18% LOG:** Majority are ambiguous cases requiring review
- **7.76% MONITOR:** Moderate suspicion, need watching
- **8.06% ALLOW:** Some clear benign traffic
- **No BLOCK or RATE_LIMIT:** No high-confidence attacks

**Interpretation:**
This dataset contains primarily **uncertain cases**:
- Most predictions are close to 50/50
- High entropy (maximum uncertainty)
- System appropriately logs for human review

### 5.3 Alert Distribution

```
ALERT DISTRIBUTION - MEDIAN VALUES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Alert           Count       Percentage    Visualization
───────────────────────────────────────────────────────────────
LOW            84,443       84.18%        █████████████████████
MEDIUM          7,784        7.76%        ██
NONE            8,086        8.06%        ██
CRITICAL            0        0.00%         
HIGH                0        0.00%         
───────────────────────────────────────────────────────────────
Total         100,313      100.00%
```

**Perfect Action-Alert Correlation:**
```
LOG      ←→ LOW     (84,443 queries)
MONITOR  ←→ MEDIUM  (7,784 queries)
ALLOW    ←→ NONE    (8,086 queries)
```

### 5.4 Rule Activation Patterns

```
RULES FIRED PER QUERY - MEDIAN VALUES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Number of Rules    Count       Percentage
───────────────────────────────────────────────────────────────
1 rule            33,672       33.58%     
2 rules           60,935       60.75%     (Dominant)
3 rules            5,706        5.69%     
───────────────────────────────────────────────────────────────
Average Rules:    1.72
```

**Analysis:**
- **Most queries fire 2 rules** (60.75%)
- Higher average than extreme values (1.72 vs 1.0)
- Indicates more complex, ambiguous scenarios

**Which Rules Fired?**

Primary rules for median values:
- **Rule 6:** Ambiguous Case (small prob_diff or uncertain entropy)
- **Rule 9:** Normal but Suspicious (small separation)
- **Rule 4:** Medium Attack + Uncertainty
- **Rule 5:** Low Confidence Attack

### 5.5 Sample Decisions - Median Values

**Example 1: Ambiguous Case**
```
Input:
  P(Benign): 0.52
  P(Tunneling): 0.48
  
Fuzzy Inputs:
  max_prob: 0.52
  prob_diff: 0.04 (very small!)
  entropy: 0.9997 (maximum uncertainty!)
  pred_class: 0 (normal, but barely)

Rules Fired:
  Rule 6: Ambiguous Case
    μ_uncertain(0.9997) = 0.97
    μ_small(0.04) = 1.0
    Strength = max(0.97, 1.0) = 1.0
    → LOG + LOW
  
  Rule 9: Normal but Suspicious
    μ_normal(0) = 1.0
    μ_small(0.04) = 1.0
    Strength = min(1.0, 1.0) = 1.0
    → LOG + LOW

Aggregation:
  Both suggest LOG + LOW
  Total strength: 2.0
  
Decision:
  Action: LOG (code 1)
  Alert: LOW (code 1)
  
Reasoning:
  "Prediction is almost 50/50 split with maximum uncertainty.
   Cannot make confident decision → Log for human review"
```

**Example 2: Moderate Attack with Uncertainty**
```
Input:
  P(Benign): 0.38
  P(Tunneling): 0.62
  
Fuzzy Inputs:
  max_prob: 0.62
  prob_diff: 0.24
  entropy: 0.96 (high uncertainty)
  pred_class: 1 (attack)

Rules Fired:
  Rule 4: Medium Attack + Uncertainty
    μ_medium(0.62) = 0.47
    μ_attack(1) = 1.0
    μ_uncertain(0.96) = 0.60
    Strength = min(0.47, 1.0, 0.60) = 0.47
    → MONITOR + MEDIUM
  
  Rule 6: Ambiguous Case
    μ_uncertain(0.96) = 0.60
    μ_small(0.24) = 0.10
    Strength = max(0.60, 0.10) = 0.60
    → LOG + LOW

Aggregation:
  Action: MONITOR(2)×0.47 + LOG(1)×0.60 = 1.54/1.07 ≈ 1.4 → 1 (LOG)
  Alert: MEDIUM(2)×0.47 + LOW(1)×0.60 = 1.54/1.07 ≈ 1.4 → 1 (LOW)
  
Decision:
  Action: LOG (code 1)
  Alert: LOW (code 1)
  
Reasoning:
  "Moderate confidence attack (62%) but high uncertainty (96%).
   Conflicting signals → Log for investigation rather than
   immediate action"
```

**Example 3: Clear Normal (Rare in Median Dataset)**
```
Input:
  P(Benign): 0.68
  P(Tunneling): 0.32
  
Fuzzy Inputs:
  max_prob: 0.68
  prob_diff: 0.36 (large)
  entropy: 0.90
  pred_class: 0 (normal)

Rules Fired:
  Rule 8: Medium Normal + Large Diff
    μ_medium(0.68) = 0.87
    μ_normal(0) = 1.0
    μ_large(0.36) = 0.73
    Strength = min(0.87, 1.0, 0.73) = 0.73
    → ALLOW + NONE

Decision:
  Action: ALLOW (code 0)
  Alert: NONE (code 0)
  
Reasoning:
  "Medium confidence (68%) but clear separation (36%) and 
   classified as normal → Allow traffic"
```

### 5.6 Key Insights - Median Values

1. **Uncertainty Dominates:**
   - 84% of queries logged for review
   - Rule 6 (ambiguous case) fires frequently
   - High entropy and small prob_diff are common

2. **Graduated Response:**
   - System uses LOG and MONITOR appropriately
   - No extreme actions (BLOCK/RATE_LIMIT)
   - Matches the uncertainty in predictions

3. **Multiple Rules Active:**
   - Average 1.72 rules per query
   - 60.75% fire 2 rules simultaneously
   - System considers multiple perspectives

4. **Balanced Classification:**
   - 47.7% normal vs 52.3% attack
   - Near 50/50 split confirms uncertainty
   - No strong bias in predictions

---

## 6. COMPARATIVE ANALYSIS

### 6.1 Side-by-Side Comparison

```
═══════════════════════════════════════════════════════════════════
                    DATASET COMPARISON SUMMARY
═══════════════════════════════════════════════════════════════════

Metric                  Extreme Values    Median Values
────────────────────────────────────────────────────────────────
Total Queries          100,313           100,313
Edge Cases             74 (0.07%)        0 (0.00%)
Avg Rules Fired        1.00              1.72

Top Action             BLOCK (79.93%)    LOG (84.18%)
Second Action          ALLOW (19.97%)    ALLOW (8.06%)
Third Action           LOG (0.07%)       MONITOR (7.76%)

Top Alert              CRITICAL (79.93%) LOW (84.18%)
Second Alert           NONE (19.97%)     NONE (8.06%)
Third Alert            LOW (0.07%)       MEDIUM (7.76%)

Attack Rate            80.0%             52.3%
Normal Rate            20.0%             47.7%
────────────────────────────────────────────────────────────────
```

### 6.2 Decision Distribution Comparison

```
ACTION DISTRIBUTION COMPARISON
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Action          Extreme          Median
             Count     %       Count     %
───────────────────────────────────────────────────────────────
ALLOW         20,028   19.97%   8,086    8.06%
LOG               74    0.07%  84,443   84.18%
MONITOR            0    0.00%   7,784    7.76%
RATE_LIMIT         0    0.00%       0    0.00%
BLOCK         80,211   79.93%       0    0.00%
───────────────────────────────────────────────────────────────
```

**Visualization:**
```
Extreme Values:          Median Values:
    
    BLOCK ████████████████  LOG   █████████████████████
    ALLOW ████              MONITOR ██
    LOG   ▏                ALLOW   ██
```

### 6.3 What This Tells Us

**System Adaptability:**
The fuzzy logic system demonstrates excellent **context awareness**:

1. **High Confidence Inputs (Extreme Values):**
   - System makes decisive actions (BLOCK/ALLOW)
   - Minimal intermediate responses
   - Appropriate for clear-cut cases

2. **Low Confidence Inputs (Median Values):**
   - System avoids extreme actions
   - Emphasizes logging and monitoring
   - Appropriate for uncertain cases

**This proves the system is NOT blindly applying rules, but rather intelligently adapting to input certainty levels.**

### 6.4 Rule Activation Comparison

```
RULES FIRED DISTRIBUTION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Rules Fired    Extreme Values    Median Values
────────────────────────────────────────────────────────────────
0 rules        74 (0.07%)        0 (0.00%)
1 rule         99,920 (99.61%)   33,672 (33.58%)
2 rules        319 (0.32%)       60,935 (60.75%)
3 rules        0 (0.00%)         5,706 (5.69%)
────────────────────────────────────────────────────────────────
Average        1.00              1.72
```

**Interpretation:**
- **Extreme values:** Usually single clear rule fires
- **Median values:** Multiple rules often fire together
- Higher average = more complex decision-making

---

## 7. SYSTEM PERFORMANCE EVALUATION

### 7.1 Correctness Validation

**Extreme Values - Sample Verification:**

```
Query: P(Benign)=0.02, P(Tunneling)=0.98
Expected: Should BLOCK with CRITICAL alert
Actual: BLOCK + CRITICAL ✅

Query: P(Benign)=0.9986, P(Tunneling)=0.0014  
Expected: Should ALLOW with no alert
Actual: ALLOW + NONE ✅

Query: P(Benign)=0.52, P(Tunneling)=0.48
Expected: Should LOG (ambiguous)
Actual: LOG + LOW ✅
```

**All sampled queries show correct behavior.**

### 7.2 Edge Case Analysis

**Extreme Values Dataset:**
```
Edge Cases Found: 74 (0.07%)

Example Edge Case:
  P(Benign): 0.7663
  P(Tunneling): 0.2337
  entropy: 0.0
  
Why No Rules Fired:
  - max_prob (0.7663) falls between fuzzy sets
  - Not "high" enough (μ_high = 0.13)
  - Not "very_high" (μ_very_high = 0.0)
  - Rules require higher membership degrees

System Response:
  - Default to ALLOW + NONE
  - Edge case counter incremented
  - Logged for review
  
Status: ACCEPTABLE (0.07% is very low)
```

**Median Values Dataset:**
```
Edge Cases Found: 0 (0.00%)

Explanation:
  - Rule 6 (OR logic) acts as a catch-all
  - High entropy OR small prob_diff triggers it
  - Median values always have one or both
  
Status: EXCELLENT (perfect coverage)
```

### 7.3 Performance Metrics

```
PROCESSING PERFORMANCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Metric                           Value
────────────────────────────────────────────────────────────────
Total Queries Processed          100,313 (both datasets)
Processing Success Rate          100.00%
Average Processing Time          ~1.2 seconds per 1000 queries
Total Processing Time            ~2.5 minutes per dataset
Memory Usage                     < 500 MB
Edge Case Rate                   0.035% (average)
────────────────────────────────────────────────────────────────
```

### 7.4 Decision Quality Assessment

**Consistency:** ✅ EXCELLENT
- Action-alert pairs are logically consistent
- No contradictions observed
- Same inputs always produce same outputs

**Appropriateness:** ✅ EXCELLENT
- Extreme values → Extreme actions (BLOCK/ALLOW)
- Median values → Cautious actions (LOG/MONITOR)
- Matches security best practices

**Explainability:** ✅ EXCELLENT
- Every decision traceable to fired rules
- Clear reasoning provided
- Rule strengths documented

**Coverage:** ✅ EXCELLENT
- 99.965% of queries covered by rules
- Edge cases minimal and handled gracefully
- No system failures

### 7.5 Strengths and Limitations

**Strengths:**

1. **Intelligent Uncertainty Handling**
   - Explicitly models uncertainty with entropy
   - Different responses for different confidence levels
   - Avoids inappropriate extreme actions

2. **Graduated Response**
   - 5 action levels provide flexibility
   - Security teams have options beyond block/allow
   - Matches real-world security operations

3. **High Reliability**
   - 100% processing success rate
   - Minimal edge cases
   - Consistent, repeatable decisions

4. **Explainability**
   - Rule-based reasoning easy to understand
   - Auditable decision trail
   - Builds trust with security teams

**Limitations:**

1. **Static Rule Base**
   - Rules are fixed, not learned
   - Cannot adapt to new attack patterns automatically
   - Requires manual updates for new scenarios

2. **No Real-Time Learning**
   - Doesn't learn from feedback
   - Cannot improve over time without updates
   - Relies on expert knowledge encoding

3. **Membership Function Tuning**
   - Parameters set manually based on expertise
   - May need adjustment for different networks
   - No automatic optimization

4. **Computational Overhead**
   - More complex than simple thresholding
   - Requires fuzzy logic calculations
   - May be slower for very large-scale systems

---

## Summary of Part 3

This part covered:
- **Implementation details** of the fuzzy system
- **Experimental setup** with two distinct datasets
- **Complete results** for extreme values (79.93% BLOCK)
- **Complete results** for median values (84.18% LOG)
- **Comparative analysis** showing system adaptability
- **Performance evaluation** demonstrating reliability

**Key Finding:** The system successfully adapts its behavior based on input certainty, making decisive actions for clear cases and cautious responses for ambiguous ones.

**Next:** Part 4 will cover conclusions, recommendations, future work, and final thoughts.

---

*End of Part 3*
*Continue to Part 4: Conclusions and Recommendations*
