# COMPREHENSIVE PROJECT REPORT
# DNS Anomaly Detection Using Hybrid EvoDN2-Fuzzy Logic System

---

## PART 2: FUZZY LOGIC IMPLEMENTATION DETAILS

### Table of Contents - Part 2
1. Fuzzy Logic Fundamentals
2. Membership Function Design
3. Fuzzy Rule Base
4. Inference Process
5. Defuzzification Strategy
6. Critical Improvements

---

## 1. FUZZY LOGIC FUNDAMENTALS

### 1.1 What is Fuzzy Logic?

**Traditional (Crisp) Logic:**
```
A query is either:
  • Attack (1) OR
  • Normal (0)
  
No middle ground, no degrees.
```

**Fuzzy Logic:**
```
A query can be:
  • Definitely an attack (μ = 1.0)
  • Probably an attack (μ = 0.8)
  • Somewhat suspicious (μ = 0.5)
  • Probably benign (μ = 0.2)
  • Definitely benign (μ = 0.0)
  
Allows gradual transitions and partial membership.
```

### 1.2 Why Fuzzy Logic for Security?

**Real-World Security Scenarios:**

```
Scenario 1: Very Clear Attack
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Domain: "zxcvbnmasdfghjklqwertyuiop.malicious.com"
Entropy: 4.2 (very high)
P(Tunneling): 0.98

Human Security Expert's Thought:
"This is DEFINITELY an attack. Block immediately!"

Fuzzy Logic Representation:
μ(very_high_confidence) = 1.0
μ(attack) = 1.0
→ Rule 1 fires → BLOCK + CRITICAL
```

```
Scenario 2: Borderline Case
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Domain: "server-updates.check-dns.com"
Entropy: 2.1 (moderate)
P(Benign): 0.52
P(Tunneling): 0.48

Human Security Expert's Thought:
"Hmm, this is really uncertain. Could go either way.
Let me log it and investigate later."

Fuzzy Logic Representation:
μ(small_prob_diff) = 0.9
μ(uncertain_entropy) = 0.3
→ Rule 6 fires → LOG + LOW
```

### 1.3 Mamdani Fuzzy Inference System

Our system follows the **Mamdani approach**, which consists of four stages:

```
┌─────────────────────────────────────────────────────────────┐
│  Stage 1: FUZZIFICATION                                      │
│  Convert crisp inputs to fuzzy membership degrees            │
│                                                               │
│  Input: max_prob = 0.75                                      │
│  Output: μ(high) = 0.67, μ(very_high) = 0.33               │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Stage 2: RULE EVALUATION                                    │
│  Apply fuzzy rules using AND (min) and OR (max) operators   │
│                                                               │
│  Rule: IF high confidence AND attack                         │
│  Strength = min(0.67, 1.0) = 0.67                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Stage 3: AGGREGATION                                        │
│  Combine outputs from multiple rules                         │
│                                                               │
│  BLOCK: strength = 0.67 + 0.45 = 1.12                       │
│  (using SUM aggregation)                                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Stage 4: DEFUZZIFICATION                                    │
│  Convert fuzzy output to crisp decision                      │
│                                                               │
│  Weighted average: action = 3.8 ≈ 4 (BLOCK)                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. MEMBERSHIP FUNCTION DESIGN

### 2.1 Input Variable 1: max_prob (Confidence)

**Definition:** The maximum probability value between P(Benign) and P(Tunneling)

**Purpose:** Represents how confident the ML model is in its prediction

**Range:** [0.0, 1.0]

**Fuzzy Sets:**

```
very_low:  Trapezoidal [0.0, 0.0, 0.3, 0.4]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤████████▓░░░
     0.8 ┤████████▓░░░
     0.6 ┤████████▓░░░
     0.4 ┤████████▓▒▒▒░░░
     0.2 ┤████████▓▒▒▒░░░
     0.0 ┤████████▓▒▒▒░░░
         └─────────┬─────────→
         0.0     0.3  0.4

Interpretation:
• [0.0 - 0.3]: Definitely very low (μ = 1.0)
• [0.3 - 0.4]: Transitioning out (μ decreases)
• [0.4+]:      Not very low (μ = 0.0)
```

```
low:       Triangular [0.3, 0.4, 0.5]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤        ▲
     0.8 ┤       ╱ ╲
     0.6 ┤      ╱   ╲
     0.4 ┤     ╱     ╲
     0.2 ┤    ╱       ╲
     0.0 ┤───╱─────────╲───→
         0.3   0.4   0.5

Peak at 0.4 (μ = 1.0)
```

```
medium:    Triangular [0.4, 0.55, 0.7]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤           ▲
     0.8 ┤         ╱   ╲
     0.6 ┤        ╱     ╲
     0.4 ┤       ╱       ╲
     0.2 ┤      ╱         ╲
     0.0 ┤─────╱───────────╲──→
         0.4    0.55     0.7

Peak at 0.55 (μ = 1.0)
```

```
high:      Triangular [0.6, 0.75, 0.9]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤               ▲
     0.8 ┤             ╱   ╲
     0.6 ┤            ╱     ╲
     0.4 ┤           ╱       ╲
     0.2 ┤          ╱         ╲
     0.0 ┤─────────╱───────────╲→
         0.6      0.75       0.9

Peak at 0.75 (μ = 1.0)
```

```
very_high: Trapezoidal [0.8, 0.9, 1.0, 1.0]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤                   ▒▒████████
     0.8 ┤                   ▒▒████████
     0.6 ┤                   ▒▒████████
     0.4 ┤                 ▒▒▒▒████████
     0.2 ┤               ▒▒▒▒▒▒████████
     0.0 ┤──────────────▒▒▒▒▒▒████████→
         0.8           0.9          1.0

Interpretation:
• [0.9 - 1.0]: Definitely very high (μ = 1.0)
• [0.8 - 0.9]: Transitioning in (μ increases)
```

**Example Evaluations:**

```python
max_prob = 0.35  →  very_low: 0.5, low: 0.5, others: 0
max_prob = 0.55  →  medium: 1.0, low: 0, high: 0
max_prob = 0.68  →  medium: 0.17, high: 0.53
max_prob = 0.95  →  very_high: 1.0, high: 0.33
```

**Design Rationale:**
- Overlapping sets allow smooth transitions
- Very low and very high use trapezoidal (flat tops)
- Middle sets use triangular (single peaks)
- Captures full spectrum from "no confidence" to "absolute certainty"

---

### 2.2 Input Variable 2: prob_diff (Separation)

**Definition:** Absolute difference between P(Benign) and P(Tunneling)

**Purpose:** Measures how clearly separated the two predictions are

**Range:** [0.0, 1.0]

**Calculation:**
```python
prob_diff = |P(Benign) - P(Tunneling)|
```

**Examples:**
```
P(Benign)=0.95, P(Tunneling)=0.05  →  prob_diff = 0.90 (LARGE separation)
P(Benign)=0.70, P(Tunneling)=0.30  →  prob_diff = 0.40 (MEDIUM separation)
P(Benign)=0.52, P(Tunneling)=0.48  →  prob_diff = 0.04 (SMALL separation)
```

**Fuzzy Sets:**

```
small:     Trapezoidal [0.0, 0.0, 0.15, 0.25]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤████▓░░░
     0.8 ┤████▓░░░
     0.6 ┤████▓░░░
     0.4 ┤████▓▒▒░░░
     0.2 ┤████▓▒▒░░░
     0.0 ┤████▓▒▒░░░
         └────┬──────→
         0.0 0.15 0.25

Small separation = ambiguous prediction
```

```
medium:    Triangular [0.15, 0.35, 0.55]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤         ▲
     0.8 ┤       ╱   ╲
     0.6 ┤      ╱     ╲
     0.4 ┤     ╱       ╲
     0.2 ┤    ╱         ╲
     0.0 ┤───╱───────────╲───→
         0.15  0.35   0.55

Moderate separation
```

```
large:     Trapezoidal [0.45, 0.65, 1.0, 1.0]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤                    ▒████████
     0.8 ┤                    ▒████████
     0.6 ┤                    ▒████████
     0.4 ┤                 ▒▒▒▒████████
     0.2 ┤              ▒▒▒▒▒▒████████
     0.0 ┤─────────────▒▒▒▒▒▒████████→
         0.45         0.65         1.0

Large separation = clear distinction
```

**Semantic Interpretation:**
- **Small (0-0.25):** "The model is very uncertain, almost a coin flip"
- **Medium (0.15-0.55):** "The model leans one way but not strongly"
- **Large (0.45-1.0):** "The model has a clear preference"

---

### 2.3 Input Variable 3: entropy (Uncertainty)

**Definition:** Shannon entropy of the probability distribution

**Purpose:** Quantifies the uncertainty in the prediction

**Range:** [0.0, 1.0] for binary classification

**Formula:**
```python
H = -(p₁ × log₂(p₁) + p₂ × log₂(p₂))

where:
  p₁ = P(Benign)
  p₂ = P(Tunneling) = 1 - p₁
```

**Properties:**
```
Minimum Entropy (H = 0):
  When p₁ = 0 or p₁ = 1 (completely certain)
  
Maximum Entropy (H = 1):
  When p₁ = 0.5, p₂ = 0.5 (maximum uncertainty)
```

**CRITICAL FIX:**
```python
# ORIGINAL (INCORRECT):
'uncertain': lambda x: trapmf(x, [0.9, 1.2, 2.0, 2.0])
                                           ^^^^ WRONG!
# Entropy for binary classification cannot exceed 1.0!

# FIXED:
'uncertain': lambda x: trapmf(x, [0.9, 1.0, 1.0, 1.0])
                                           ^^^^ CORRECT!
```

**Fuzzy Sets:**

```
certain:   Trapezoidal [0.0, 0.0, 0.3, 0.5]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤████████▓░░░
     0.8 ┤████████▓░░░
     0.6 ┤████████▓░░░
     0.4 ┤████████▓▒▒░░░
     0.2 ┤████████▓▒▒░░░
     0.0 ┤████████▓▒▒░░░
         └────────┬──────→
         0.0     0.3  0.5

Low entropy = high certainty
```

```
moderate:  Triangular [0.4, 0.7, 1.0]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤             ▲
     0.8 ┤           ╱   ╲
     0.6 ┤          ╱     ╲
     0.4 ┤         ╱       ╲
     0.2 ┤        ╱         ╲
     0.0 ┤───────╱───────────╲
         0.4     0.7        1.0

Moderate uncertainty
```

```
uncertain: Trapezoidal [0.9, 1.0, 1.0, 1.0] ✅ FIXED
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
     1.0 ┤                 ▒████
     0.8 ┤                 ▒████
     0.6 ┤                 ▒████
     0.4 ┤                ▒▒████
     0.2 ┤              ▒▒▒▒████
     0.0 ┤─────────────▒▒▒▒████
         0.9                 1.0
                              ↑
                    Binary maximum

High entropy = high uncertainty (50/50 split)
```

**Interpretation Examples:**
```
H = 0.10  →  certain (μ=1.0)     | "Almost 100% sure"
H = 0.50  →  moderate (μ=0.67)   | "Somewhat uncertain"
H = 0.92  →  moderate (μ=0.53)   | "Quite uncertain"
H = 0.99  →  uncertain (μ=0.9)   | "Very uncertain, almost 50/50"
```

---

### 2.4 Input Variable 4: pred_class (Prediction)

**Definition:** Binary classification result

**Purpose:** Determines which rule pathway to follow

**Range:** {0, 1}

**Values:**
```
0 = Normal (benign DNS traffic)
1 = Attack (tunneling detected)
```

**Calculation:**
```python
pred_class = 0 if P(Benign) > P(Tunneling) else 1
```

**Fuzzy Sets (Crisp):**

```python
'normal': μ(x) = 1.0 if x == 0 else 0.0
'attack': μ(x) = 1.0 if x == 1 else 0.0
```

**Graph:**
```
     1.0 ┤ ████                    ████
     0.8 ┤ ████                    ████
     0.6 ┤ ████                    ████
     0.4 ┤ ████                    ████
     0.2 ┤ ████                    ████
     0.0 ┤ ████████████████████████████
         └──┬─────────────────────┬────→
            0                     1
        (Normal)              (Attack)
```

**Note:** This is a crisp (binary) membership, not fuzzy. It acts as a selector for rule pathways.

---

### 2.5 Output Variables

#### 2.5.1 Action Centers

**Definition:** Discrete action codes representing security responses

```python
action_centers = {
    'allow':      0,    # Safe, permit traffic
    'log':        1,    # Record for analysis
    'monitor':    2,    # Watch closely
    'rate_limit': 3,    # Throttle requests
    'block':      4     # Deny immediately
}
```

**Semantic Meaning:**

```
Level 0: ALLOW
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Traffic is benign and safe.
Action: Permit without restrictions
Alert: None required
Example: Normal Google DNS lookup
```

```
Level 1: LOG
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Traffic is uncertain or slightly suspicious.
Action: Allow but record details for later analysis
Alert: Low priority for investigation
Example: Unusual but not clearly malicious
```

```
Level 2: MONITOR
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Traffic shows moderate suspicion.
Action: Allow but track behavior closely
Alert: Medium priority, watch for patterns
Example: Moderate confidence attack prediction
```

```
Level 3: RATE_LIMIT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Traffic is likely malicious but not certain.
Action: Throttle request rate to slow potential attack
Alert: High priority, likely threat
Example: Medium-high confidence attack
```

```
Level 4: BLOCK
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Traffic is definitely malicious.
Action: Deny all requests immediately
Alert: Critical priority, confirmed threat
Example: Very high confidence attack
```

#### 2.5.2 Alert Centers

```python
alert_centers = {
    'none':     0,    # No alert needed
    'low':      1,    # Minor concern
    'medium':   2,    # Moderate concern
    'high':     3,    # Significant threat
    'critical': 4     # Severe threat
}
```

**Alert Level Guidelines:**

| Alert | When to Use | Response Time |
|-------|-------------|---------------|
| NONE | Normal traffic | No action |
| LOW | Slight anomaly | Review weekly |
| MEDIUM | Moderate suspicion | Review daily |
| HIGH | Likely threat | Review hourly |
| CRITICAL | Confirmed threat | Immediate response |

---

## 3. FUZZY RULE BASE

### 3.1 Rule Design Philosophy

**Principles:**
1. **Attack rules take precedence** over normal rules
2. **Higher confidence → stronger action**
3. **Uncertainty → cautious approach** (log/monitor)
4. **Multiple factors** must align for strong actions
5. **Ambiguity handling** is explicit (Rule 6)

### 3.2 Complete Rule Definitions

#### RULE 1: Very High Confidence Attack
```
IF max_prob is very_high AND pred_class is attack
THEN action = BLOCK AND alert = CRITICAL

Logical Form:
  Strength = min(μ_very_high(max_prob), μ_attack(pred_class))
  
Interpretation:
  "When the model is extremely confident (>90%) that this is
   an attack, block it immediately and raise critical alert."

Example Trigger:
  max_prob = 0.96  →  μ_very_high = 1.0
  pred_class = 1   →  μ_attack = 1.0
  Strength = min(1.0, 1.0) = 1.0
  
  Result: BLOCK + CRITICAL (strength 1.0)

Real-World Scenario:
  Domain: "eW91cmRhdGFpc2hlcmU.tunneling.xyz"
  P(Tunneling): 0.96
  Entropy: 3.8
  
  → Very clearly an attack → Block immediately
```

---

#### RULE 2: High Confidence Attack with Certainty
```
IF max_prob is high AND pred_class is attack AND entropy is certain
THEN action = BLOCK AND alert = CRITICAL

Logical Form:
  Strength = min(μ_high(max_prob), μ_attack(pred_class), μ_certain(entropy))
  
Interpretation:
  "When confidence is high (75-90%), it's classified as attack,
   AND the prediction is very certain (low entropy), then block."

Example Trigger:
  max_prob = 0.82   →  μ_high = 0.47
  pred_class = 1    →  μ_attack = 1.0
  entropy = 0.25    →  μ_certain = 1.0
  Strength = min(0.47, 1.0, 1.0) = 0.47
  
  Result: BLOCK + CRITICAL (strength 0.47)

Real-World Scenario:
  P(Benign): 0.18, P(Tunneling): 0.82
  Entropy: 0.25 (very certain)
  
  → High confidence + low uncertainty → Block with moderate strength
```

---

#### RULE 3: Medium Confidence Attack with Large Separation
```
IF max_prob is medium AND pred_class is attack AND prob_diff is large
THEN action = RATE_LIMIT AND alert = HIGH

Logical Form:
  Strength = min(μ_medium(max_prob), μ_attack(pred_class), μ_large(prob_diff))
  
Interpretation:
  "When confidence is medium (55-70%), it's classified as attack,
   AND there's clear separation between probabilities, then rate-limit."

Example Trigger:
  max_prob = 0.68    →  μ_medium = 0.87
  pred_class = 1     →  μ_attack = 1.0
  prob_diff = 0.36   →  μ_large = 0.73
  Strength = min(0.87, 1.0, 0.73) = 0.73
  
  Result: RATE_LIMIT + HIGH (strength 0.73)

Real-World Scenario:
  P(Benign): 0.32, P(Tunneling): 0.68
  prob_diff: 0.36 (clear separation)
  
  → Moderate confidence but clear distinction → Limit rate, don't block yet
```

---

#### RULE 4: Medium Confidence Attack with Uncertainty
```
IF max_prob is medium AND pred_class is attack AND entropy is uncertain
THEN action = MONITOR AND alert = MEDIUM

Logical Form:
  Strength = min(μ_medium(max_prob), μ_attack(pred_class), μ_uncertain(entropy))
  
Interpretation:
  "When confidence is medium, it's classified as attack,
   BUT entropy is high (uncertain), be cautious → just monitor."

Example Trigger:
  max_prob = 0.58     →  μ_medium = 0.60
  pred_class = 1      →  μ_attack = 1.0
  entropy = 0.97      →  μ_uncertain = 0.70
  Strength = min(0.60, 1.0, 0.70) = 0.60
  
  Result: MONITOR + MEDIUM (strength 0.60)

Real-World Scenario:
  P(Benign): 0.42, P(Tunneling): 0.58
  Entropy: 0.97 (very uncertain, close to 50/50)
  
  → Medium confidence BUT high uncertainty → Monitor, don't act aggressively
```

---

#### RULE 5: Low Confidence Attack
```
IF max_prob is low AND pred_class is attack
THEN action = MONITOR AND alert = MEDIUM

Logical Form:
  Strength = min(μ_low(max_prob), μ_attack(pred_class))
  
Interpretation:
  "When confidence is low (40-50%) but still classified as attack,
   just monitor it rather than taking strong action."

Example Trigger:
  max_prob = 0.45    →  μ_low = 1.0
  pred_class = 1     →  μ_attack = 1.0
  Strength = min(1.0, 1.0) = 1.0
  
  Result: MONITOR + MEDIUM (strength 1.0)

Real-World Scenario:
  P(Benign): 0.55, P(Tunneling): 0.45
  (Wait, pred_class would be 0 here, not 1!)
  
  Actually triggers when:
  P(Benign): 0.45, P(Tunneling): 0.55
  
  → Low confidence attack → Monitor but don't block
```

---

#### RULE 6: Ambiguous Case (OR Rule) ⭐ SPECIAL
```
IF entropy is uncertain OR prob_diff is small
THEN action = LOG AND alert = LOW

Logical Form (OR uses max):
  Strength = max(μ_uncertain(entropy), μ_small(prob_diff))
  
Interpretation:
  "When EITHER the entropy is very high (very uncertain prediction)
   OR the probability difference is small (close to 50/50),
   then log it for human review rather than making automatic decision."

Example Trigger 1 (High Entropy):
  entropy = 0.99      →  μ_uncertain = 0.90
  prob_diff = 0.30    →  μ_small = 0.0
  Strength = max(0.90, 0.0) = 0.90
  
  Result: LOG + LOW (strength 0.90)

Example Trigger 2 (Small Difference):
  entropy = 0.50      →  μ_uncertain = 0.0
  prob_diff = 0.08    →  μ_small = 0.70
  Strength = max(0.0, 0.70) = 0.70
  
  Result: LOG + LOW (strength 0.70)

Real-World Scenario:
  P(Benign): 0.52, P(Tunneling): 0.48
  prob_diff: 0.04 (almost a tie!)
  entropy: 0.99 (maximum uncertainty)
  
  → Can't make confident decision → Log for human expert
```

**Why This Rule is Critical:**
- Handles edge cases explicitly
- Prevents inappropriate blocking of uncertain cases
- Ensures human review of ambiguous traffic
- Implements "when in doubt, investigate" principle

---

#### RULE 7: Very High Confidence Normal
```
IF max_prob is very_high AND pred_class is normal AND entropy is certain
THEN action = ALLOW AND alert = NONE

Logical Form:
  Strength = min(μ_very_high(max_prob), μ_normal(pred_class), μ_certain(entropy))
  
Interpretation:
  "When extremely confident (>90%) that traffic is normal
   AND entropy is low (very certain), allow without restrictions."

Example Trigger:
  max_prob = 0.97     →  μ_very_high = 1.0
  pred_class = 0      →  μ_normal = 1.0
  entropy = 0.12      →  μ_certain = 1.0
  Strength = min(1.0, 1.0, 1.0) = 1.0
  
  Result: ALLOW + NONE (strength 1.0)

Real-World Scenario:
  Domain: "google.com"
  P(Benign): 0.97, P(Tunneling): 0.03
  Entropy: 0.12 (very certain)
  
  → Clearly benign → Allow without any alerts
```

---

#### RULE 8: Medium Confidence Normal with Large Separation
```
IF max_prob is medium AND pred_class is normal AND prob_diff is large
THEN action = ALLOW AND alert = NONE

Logical Form:
  Strength = min(μ_medium(max_prob), μ_normal(pred_class), μ_large(prob_diff))
  
Interpretation:
  "When confidence is medium but it's classified as normal
   AND there's clear separation, allow it."

Example Trigger:
  max_prob = 0.68     →  μ_medium = 0.87
  pred_class = 0      →  μ_normal = 1.0
  prob_diff = 0.36    →  μ_large = 0.73
  Strength = min(0.87, 1.0, 0.73) = 0.73
  
  Result: ALLOW + NONE (strength 0.73)

Real-World Scenario:
  P(Benign): 0.68, P(Tunneling): 0.32
  prob_diff: 0.36 (clear preference for benign)
  
  → Medium confidence but clear distinction → Allow
```

---

#### RULE 9: Normal but Suspicious
```
IF pred_class is normal AND prob_diff is small
THEN action = LOG AND alert = LOW

Logical Form:
  Strength = min(μ_normal(pred_class), μ_small(prob_diff))
  
Interpretation:
  "When classified as normal BUT the probability difference is small
   (not very confident), log it for review."

Example Trigger:
  pred_class = 0      →  μ_normal = 1.0
  prob_diff = 0.12    →  μ_small = 0.70
  Strength = min(1.0, 0.70) = 0.70
  
  Result: LOG + LOW (strength 0.70)

Real-World Scenario:
  P(Benign): 0.56, P(Tunneling): 0.44
  prob_diff: 0.12 (small separation)
  
  → Classified as normal but weak confidence → Log for review
```

---

### 3.3 Rule Priority and Conflict Resolution

**Question:** What happens when multiple rules fire?

**Answer:** The system uses **SUM aggregation** followed by **weighted average defuzzification**.

**Example Scenario:**
```
Input:
  max_prob = 0.68 (P(Tunneling) = 0.68)
  prob_diff = 0.36
  entropy = 0.91
  pred_class = 1 (attack)

Rules That Fire:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Rule 3: Medium Attack + Large Diff
  μ_medium(0.68) = 0.87
  μ_attack(1) = 1.0
  μ_large(0.36) = 0.73
  Strength = min(0.87, 1.0, 0.73) = 0.73
  → RATE_LIMIT (3) + HIGH (3)

Rule 6: Ambiguous Case
  μ_uncertain(0.91) = 0.10
  μ_small(0.36) = 0.0
  Strength = max(0.10, 0.0) = 0.10
  → LOG (1) + LOW (1)

Aggregation (SUM):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Action strengths:
  RATE_LIMIT: 0.73
  LOG: 0.10
  Total: 0.83

Alert strengths:
  HIGH: 0.73
  LOW: 0.10
  Total: 0.83

Defuzzification (Weighted Average):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Action = (3 × 0.73 + 1 × 0.10) / 0.83
       = (2.19 + 0.10) / 0.83
       = 2.76
       ≈ 3 (RATE_LIMIT)

Alert = (3 × 0.73 + 1 × 0.10) / 0.83
      = 2.76
      ≈ 3 (HIGH)

Final Decision:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Action: RATE_LIMIT (code 3)
Alert: HIGH (code 3)
Num Rules Fired: 2
```

**Why SUM Aggregation?**
- More standard in Mamdani fuzzy systems
- Accumulates evidence from multiple rules
- Gives more weight to strongly-firing rules
- Better handles conflicting rules than MAX

---

## Summary of Part 2

This part covered:
- **Fuzzy logic fundamentals** and why it's suitable
- **Detailed membership functions** for all 4 inputs
- **Complete rule definitions** with examples
- **Rule evaluation mechanics** and aggregation
- **Critical improvements** including entropy fix

**Next:** Part 3 will cover the inference process, implementation details, experimental results, and system evaluation.

---

*End of Part 2*
*Continue to Part 3: Implementation and Results*
