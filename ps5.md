# Hybrid Lightweight Post-Quantum Messaging Framework
## Problem Statement 5: Lightweight Post Quantum Messaging

---

## 1. Understanding of the Real-World Cyber Security Problem

The Quantum Threat Landscape

Quantum computing is rapidly developing, and it is a serious, long-term threat to the cryptography we use today. When sufficiently powerful quantum computers are built, Shor’s algorithm will enable them to break not only RSA but also the more general public-key cryptosystems Diffie-Hellman and Elliptic Curve Cryptography in polynomial time. That means that the very cornerstone of modern digital security could come crashing down, exposing billions of encrypted communications and sensitive digital operations.

Harvest Now, Decrypt Later (HNDL) Attacks

One of the most dangerous immediate risks is the “harvest now, decrypt later” strategy. Here, attackers capture encrypted communications today; even if they can’t decrypt them, they can store them until quantum computers become powerful enough to crack them. Current predictions estimate this capability could arrive within 10–15 years.

This becomes particularly alarming because sensitive data may remain valuable for years, such as financial transactions, healthcare records, government communications, and personal messages. Even though such communications may look protected today, they could be exposed in the future.

The Mobile & IoT Challenge

For example, post-quantum cryptographic algorithms such as CRYSTALS-Kyber for key encapsulation and CRYSTALS-Dilithium for digital signatures are immune against quantum attacks but also include several main challenges with respect to real-world implementation, mainly on mobile and IoT devices.

Computational Overhead: PQC operations require 3-10x more CPU cycles than classical cryptography.

Memory constraints: Public keys are 800 bytes to 1.5 KB, Private keys reach up to 2.4 KB hard for IoT devices to handle with only 32–128 KB RAM.

Bandwidth Usage: Larger keys raise transmission costs, especially for IoT devices over cellular networks.

Energy Drain: If continuous PQC usage increased the battery drain in mobile devices by 15-30%.

Latency Impact: Real-time messaging can have an additional 50-200 ms delay per operation.


The Adoption Dilemma

This is partly due to the fact that messaging apps offer their services with quite fast message delivery-usually within sub-100 ms; a fully postquantum messaging design would degrade this user experience.

That creates a real and serious gap: we have algorithms which can provide quantum safety, but most environments cannot practically deploy them because of resource limitations.

The challenge is very applicable to India because India is highly mobile-first and digitally expanding. With more than 750 million smartphone users, India needs quantum-secure solutions that remain practical and usable.


---

## 2. Proposed Solution and Technical Approach

Hybrid post-quantum security model

The proposed solution to solve this is a hybrid cryptographic framework, combining classical algorithms and post-quantum cryptography optimally. The idea behind it goes as follows:

Use quantum-resistant methods where they matter most and keep the protocol lightweight wherever possible.

Three-Tier Security Architecture


Tier 1: Hybrid Key Exchange (Session Establishment)

●	Uses X25519 (classical ECDH) along with CRYSTALS-Kyber-512 (post-quantum KEM)

●	Produces a hybrid shared secret:

K_hybrid = KDF(K_classical || K_pqc)

●	Ensures quantum-resistant forward secrecy for the session keys

●	Runs only once per session, so PQC overhead remains limited


Tier 2: Lightweight Session Encryption (Message Exchange)

●	Encrypts messages using ChaCha20-Poly1305 authenticated encryption with 256-bit keys

●	Very high performance: 1–2 Gbps throughput on mobile processors

●	Retains post-quantum protection because the session key itself is quantum-secure

●	Enables real-time messaging with <50ms encryption/decryption latency


Tier 3: Adaptive Authentication (Context-Dependent)

Authentication mode adapts to message sensitivity and device capability:

●	High-Security Mode: CRYSTALS-Dilithium-2 signatures for critical messages

●	Standard Mode: HMAC-SHA3 for lightweight message authentication

●	IoT Mode: Pre-shared symmetric keys for constrained devices

●	System automatically determines authentication strength based on context


Protocol Flow

1.	Initialization Phase: Hybrid key exchange (classical + PQC)

2.	Session Key Derivation: HKDF-SHA256 merges key exchange outputs

3.	Secure Channel Setup: ChaCha20-Poly1305 encrypts all messages

4.	Key Rotation: Periodic lightweight ratcheting mechanism

5.	Forward Secrecy: Session keys deleted after use to isolate damage


Device-Aware Optimization

The protocol is tuned depending on device class:

●	Mobile Devices: full hybrid protocol with optional Dilithium signatures

●	IoT Gateways: hybrid key exchange with symmetric encryption downstream

●	Ultra-Constrained IoT: pre-shared PQC session keys + symmetric-only runtime

●	Adaptive Fallback: security gracefully degrades based on device limitations

Technical Advantages

●	Selective PQC usage: PQC overhead only in session setup (hourly/daily)

●	Uses standardized algorithms: Kyber, Dilithium, ChaCha20

●	Backward-compatible: classical component supports transition phase

●	Fail-safe design: classical security still applies if PQC fails


Security Mechanisms

Forward Secrecy Implementation

●	Sessions use unique ephemeral keys

●	Key ratcheting after N messages (configurable between 100-1000)

●	Keys deleted after session termination

●	No long-term PQC keys stored on device (only certificates remain)


Threat Model Coverage

●	 Quantum attacker using Shor’s algorithm

●	 Man-in-the-middle attacks during handshake

●	 Replay and message tampering attacks

●	 Forward secrecy against key compromise

●	 Harvest now, decrypt later resistance


---

## 3. System Architecture

### High-Level Architecture Diagram
<img width="3158" height="4800" alt="DNS Tunneling Attack Flow-2026-01-15-132451" src="https://github.com/user-attachments/assets/0f6db5e4-2d64-46ce-89b4-882a1f0d955c" />


### Component Architecture

**A. Cryptographic Core Module**
<img width="1236" height="4690" alt="DNS Tunneling Attack Flow-2026-01-15-133010" src="https://github.com/user-attachments/assets/3e82a881-32cf-4770-8e75-2afc6e143d81" />


**B. Protocol Stack**
<img width="1240" height="2835" alt="diagram-2026-01-15-133600" src="https://github.com/user-attachments/assets/73fb0579-5a9d-4891-a532-d6cd35efe0a6" />


**C. Device Capability Detection**
<img width="3608" height="2785" alt="diagram-2026-01-15-133822" src="https://github.com/user-attachments/assets/ad4581a8-b263-4828-96fe-52b2a09e76f8" />


### Data Flow Architecture

**Session Establishment (One-time PQC Overhead)**
<img width="930" height="1940" alt="diagram-2026-01-15-134350" src="https://github.com/user-attachments/assets/70eb50d7-dc87-495d-a639-f7fb0ab78257" />


**Message Exchange (Lightweight Symmetric)**
<img width="2023" height="5285" alt="diagram-2026-01-15-134712" src="https://github.com/user-attachments/assets/dec482c8-084b-4a50-9a49-1b44b41cbb1a" />


### Security Mechanisms

**Forward Secrecy Implementation**
- Each session derives unique ephemeral keys
- Key ratcheting after N messages (configurable: 100-1000 messages)
- Automatic key deletion after session termination
- No long-term PQC keys stored on device (only certificates)

**Threat Model Coverage**
- Quantum adversary with Shor's algorithm capability
- Man-in-the-middle attacks during key exchange
- Message tampering and replay attacks
- Forward secrecy against key compromise
- Harvest now, decrypt later scenarios

---

## 4. Expected Outcomes

Quantifiable Security Guarantees


Quantum Resistance

●	128-bit post-quantum security (Kyber-512)

●	Grover-resistant due to 256-bit symmetric keys

●	Forward secrecy holds even against quantum adversaries

●	Provides defense against harvest now, decrypt later attacks


Performance Metrics (Target)

●	Session setup latency: <500 ms (vs 2-5s full PQC)

●	Message encryption latency: <20 ms per message

●	Throughput: >100,000 messages/second on mobile processors

●	Memory footprint: <2 MB

●	Battery drain: <5% extra compared to classical messaging


Device Compatibility: Mobile Devices (Smartphones, Tablets)

●	Full hybrid protocol + Dilithium signatures

●	Battery impact: 3-5% daily typical usage

●	Storage: 1.5-2 MB crypto libraries

●	Minimum specs: 2GB RAM, ARM Cortex-A53 or equivalent


IoT Gateways (Home Hubs, Industrial Controllers)

●	Full hybrid protocol managing constrained devices

●	Supports 100+ concurrent PQC sessions

●	RAM: 256 MB minimum

●	Use cases: smart home hubs, industrial PLCs


Constrained IoT Devices (Sensors, Actuators)

●	Gateway-assisted mode with pre-established PQC session keys

●	Device performs only symmetric encryption during runtime

●	RAM: 32 KB minimum

●	Use cases: sensors, smart meters, wearables


Deployment Readiness

●	Built on NIST-approved PQC algorithms

●	Compatible with Signal Protocol and XMPP

●	Backward-compatible classical transition support

●	SDK/API available for integration

●	Cross-platform support: Android, iOS, Linux embedded


Real-World Impact

●	Protect 750+ million Indian mobile users

●	Secure Smart Cities mission infrastructure (100+ cities)

●	Enable quantum-safe healthcare communication

●	Strengthen Digital India initiatives with long-term security


---

## 5. Benchmarking Against Existing Solutions

Comparative Analysis


| **Aspect** | **Classical Messaging (Signal, WhatsApp)** | **Full PQC Messaging (Academic Prototypes)** | **Proposed Hybrid Approach** |
|------------|-------------------------------------------|---------------------------------------------|------------------------------|
| **Quantum Resistance** | No (vulnerable to quantum attacks) | Yes (fully quantum-safe) | Yes (quantum-safe session keys) |
| **Session Setup Latency** | 50-100ms (ECDH) | 2-5 seconds (full PQC handshake) | 300-500ms (hybrid KEM) |
| **Message Latency** | <10ms (symmetric encryption) | 50-200ms (PQC signatures per message) | <20ms (symmetric encryption) |
| **Memory Footprint** | 500 KB - 1 MB | 5-10 MB (large PQC libraries) | 1.5-2 MB (optimized hybrid) |
| **Battery Impact (Daily)** | 1-2% (baseline) | 15-30% (continuous PQC ops) | 3-5% (occasional PQC) |
| **IoT Device Support** | Excellent (lightweight) | Poor (too resource-intensive) | Good (adaptive modes) |
| **Bandwidth Overhead** | Low (32-64 bytes/msg) | High (2-4 KB per signature) | Low (32-64 bytes/msg after setup) |
| **Standardization** | Industry standard (Signal Protocol) | Research-only, not standardized | NIST PQC standards compliant |
| **Deployment Feasibility** | Deployed at billion-user scale | Not production-ready | Practical for large-scale deployment |
| **Forward Secrecy** | Yes (but not quantum-safe) | Yes (quantum-safe) | Yes (quantum-safe) |


Technical Comparison: Key Exchange Performance


| **Protocol** | **Key Generation** | **Encapsulation** | **Decapsulation** | **Public Key Size** | **Ciphertext Size** |
|--------------|-------------------|-------------------|------------------|---------------------|---------------------|
| **X25519 (Classical)** | 0.05ms | 0.08ms | 0.08ms | 32 bytes | 32 bytes |
| **Kyber-512 (PQC)** | 0.15ms | 0.18ms | 0.20ms | 800 bytes | 768 bytes |
| **Dilithium-2 (PQC Sig)** | 0.50ms | 1.2ms (sign) | 0.6ms (verify) | 1312 bytes | 2420 bytes |
| **Hybrid X25519+Kyber** | 0.20ms | 0.26ms | 0.28ms | 832 bytes | 800 bytes |


Advantages Over Existing Approaches

**vs Classical Messaging:**
-  Quantum-resistant security without sacrificing user experience
-  Future-proof against quantum computer development
-  Minimal adoption friction (similar performance profile)

**vs Full PQC Solutions:**
-  4-10x faster message throughput
-  3-5x lower memory requirements
-  5-6x lower battery consumption
-  Practical for resource-constrained devices

**vs Hybrid Academic Proposals:**
-  Device-aware adaptive security (not one-size-fits-all)
-  Production-ready implementation strategy
-  Comprehensive IoT ecosystem support

---

## 6. Differentiation and Unique Value Proposition

Strategic Hybrid Architecture This framework uses PQC only where it provides the most security benefit, keeping runtime efficient and lightweight. 

Key Differentiators 


1. Adaptive Security Profiles 

The protocol changes automatically based on device and network conditions: 

●	Mobile Profile: hybrid KEM and optional Dilithium signatures 

●	Gateway Profile: manages PQC sessions for multiple devices 

●	Constrained Profile: uses pre-shared PQC keys and symmetric runtime This allows one framework to cover the entire IoT-to-cloud range.
 

2. Temporal PQC Optimization 

PQC runs only during session setup, every few hours or days, not per message. This cuts PQC overhead by 95 to 99 percent compared to full PQC while still ensuring quantum-safe forward secrecy. 


3. Fail-Safe Hybrid Design 

If one component fails, security remains intact: 

●	If Kyber fails, X25519 still provides classical security 

●	If quantum breaks X25519, Kyber still offers PQ security Security only fails if both components fail at the same time. 


4. IoT Ecosystem Integration 

This framework is designed specifically for India’s IoT ecosystem: 

●	Smart Cities: traffic, utilities, monitoring 

●	Healthcare IoT: medical devices, patient monitoring 

●	Industrial control: SCADA and automation 

●	Agriculture IoT: precision farming sensors 


5. Standards-Based Modularity 

Built entirely on NIST-standardized algorithms, this allows: 

●	Easy integration into existing infrastructure 

●	Better compliance with regulations 

●	Upgrades without redesign 

●	Vendor neutrality 


Unique Value Proposition Summary 

●	Mobile Users: quantum-safe messaging with no noticeable performance impact 

●	IoT Developers: practical PQC messaging without hardware upgrades

●	Enterprises: scalable defense against HNDL attacks 

●	India’s Ecosystem: homegrown quantum-safe security in line with national initiatives 


---

## 7. Product Roadmap and End-Use Cases

# Roadmap

Phase 1: Protocol Design and Architecture Validation (Months 1-2)

●	Finalize protocol specification 

●	Define device profiles and adaptation logic 

●	Conduct threat modeling and proof-of-concept security analysis 

●	Choose the best parameter sets (Kyber-512 vs. 768, Dilithium-2 vs.3) 

●	Deliverable: technical specs and security analysis report 


Phase 2: Core Implementation and MVP (Months 3-5) 

●	Implement X25519, Kyber-512, and ChaCha20-Poly1305 core 

●	Build hybrid handshake protocol 

●	Manage sessions and key rotation 

●	Create device capability detection module 

●	Deliverable: Android-to-Android messaging MVP 


Phase 3: Optimization and IoT Integration (Months 6-8) 

●	Optimize for ARM processors 

●	Create constrained device gateway-assisted mode 

●	Benchmark against classical and full PQC solutions  

●	Test memory and battery performance 

●	Deliverable: multi-device demo and benchmarks 


Phase 4: Testing, Audit, and Deployment Readiness (Months 9-10) 

●	Conduct penetration testing and fuzzing 

●	Complete a third-party security audit 

●	Run interoperability tests with Signal Protocol 

●	Develop SDK/API and full documentation 

●	Deliverable: audited production-ready crypto library and SDK 


Phase 5: Pilot Deployment and Community Engagement (Months 11-12) 

●	Roll out a pilot in academic settings 

●	Establish partnerships with IoT providers 

●	Release open-source version 

●	Gather real-world feedback 

●	Deliverable: pilot case studies and public library release 

# End-Use Cases and Target Users 

1. Secure Mobile Messaging 

●	Target: public, privacy-focused users, journalists, activists 

●	Use: quantum-safe alternative to WhatsApp/Signal 

●	Features: end-to-end encryption, group chats, file sharing, voice/video integration 

●	Impact: protects over 50 million users from potential quantum decryption 


2. Healthcare IoT Communication 

●	Target: hospitals and telemedicine platforms 

●	Use: secure messaging between medical devices and the cloud 

●	Scenario: wearables to gateway to hospital cloud 

●	Impact: secures patient data and ensures DPDP/HIPAA compliance 


3. Smart City Infrastructure 

●	Target: municipal bodies, infrastructure operators 

●	Use: secure messages for city sensor networks 

●	Scenario: over 10,000 sensors per city with secure gateways 

●	Impact: supports India’s Smart Cities mission 


4. Financial Services and Digital Payments 

●	Target: banks, fintech, UPI providers 

●	Use: quantum-safe banking authentication 

●	Impact: protects more than 8 billion monthly UPI transactions in India 


5. Government and Defense 

●	Target: agencies, defense, and critical infrastructure 

●	Use: quantum-safe classified communication 

●	Impact: strengthens national security messaging 


6. Industrial IoT and SCADA 

●	Target: manufacturing, grids, oil and gas 

●	Use: prevents quantum-enabled attacks on operations 

●	Impact: fortifies India’s industrial backbone 


7. Academic and Research Collaboration 

●	Target: universities and research labs 

●	Use: secure collaboration and protection of intellectual property 

●	Impact: allows secure sharing of patent-pending and sensitive research 

Scalability and Market Potential 

Addressable Market 

●	750 million smartphone users in India 

●	1.1 billion IoT devices in India by 2026 

●	Secure messaging market projected to reach $8.5 billion by 2027 

●	IoT security market expected to be $36.6 billion by 2026 Adoption Strategy - Open-source core library 

●	Commercial enterprise SDK - Government pilots - Academic partnerships for validation 


---

## 8. Impact, Scalability, and Sustainability

### National Impact 

Digital India Initiative 

●	Safeguards Digital India infrastructure 

●	Protects citizen data under DPDP Act 

●	Facilitates quantum-safe Aadhaar, DigiLocker, and UMANG ecosystem 

●	Supports Atmanirbhar Bharat through local innovation 

Ecosystem Development 

●	Positions India as a leader in PQC adoption 

●	Strengthens academic knowledge of PQC 

●	Creates skilled jobs in security and cryptography 

●	Attracts global attention to India’s security innovation 

### Scalability 

Technical Scalability 

●	Designed to support billions of devices 

●	Functions from 32KB sensors to cloud servers 

●	Optimized for rural low-bandwidth networks 

Ecosystem Scalability 

●	Open-source contributions allow rapid development 

●	Modular design supports customization for industries 

●	Cloud-native ready, accommodates SaaS deployments 

●	SDK/API allows for third-party integration 
### Sustainability 

Long-Term Security 

●	Anticipated 10-15 years of quantum resilience 

●	Cryptographic flexibility for future upgrades 

●	Forward secrecy protects old sessions even if compromised 

Economic Sustainability 

●	Software-only solution with no hardware upgrades necessary 

●	Minimal operational changes from classical messaging 

●	Monetization through enterprise licensing, consulting, and support contracts 

Environmental Sustainability 

●	Low battery impact increases device lifespan 

●	Efficient cryptography reduces data center costs 

●	No need for quantum-specific hardware 

### Global Standards Contribution 

●	Aligns with NIST PQC standards 

●	Potential for future IETF standardization 

●	Reference implementation for academic use 

●	Supports global collaboration in cryptography 


---

## Conclusion

This hybrid lightweight post-quantum messaging framework tackles one of today’s most pressing cybersecurity issues: safeguarding communications from quantum threats without losing user-friendliness. By applying PQC only when necessary, this system achieves strong quantum resistance while remaining practical for mobile and IoT environments. The project is notable for its adaptive security, ease of deployment, and suitability for India’s growing digital infrastructure. 

With a clear roadmap and strong real-world applications, it can significantly contribute to a future where digital communications are quantum secure.

---
## Declarations

**Originality:**
This proposal and the proposed solution are entirely original work created by our team. We have not copied, plagiarized, or used AI-generated content. All ideas, technical approaches, and documentation are the result of our own research, analysis, and innovation.


---

**Contact Information:**

- **Team Lead**: Rajdeep Das

- **Email**: Rajdeep.Das2025@iem.edu.in

- **Institution**: Institute of Engineering and Management

- **Team Members**: Shivyansh Rai, Shouvik Sen, Mekhla Sen, Archishman Das


Our team is committed to progressing this idea from concept to Minimum Viable Product (MVP) within the CSIC 1.0 framework. We will actively engage with mentors, incorporate feedback, and dedicate the necessary time and effort to deliver a production-ready quantum-safe messaging solution that serves India's mobile-first, IoT-driven digital ecosystem.
