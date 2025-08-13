# Acceptance Owner Map - Sign-off Authority

## Purpose
Define clear ownership and approval authority for different aspects of the Chimera MVP across Product, Engineering, and Legal domains.

## Approval Categories

### Legal & Compliance (Critical Path)
| Area | Description | Primary Owner | Required Approvers | Decision Level |
|------|-------------|---------------|-------------------|----------------|
| **Data Sources TOS** | Third-party data usage rights verification | Legal | Legal Lead + Product Lead | REQUIRED |
| **Disclaimers & Warnings** | All user-facing legal text | Legal | Legal Lead + Compliance Officer | REQUIRED |
| **Privacy Policy** | DPDP Act compliance and data handling | Legal | Legal Lead + DPO | REQUIRED |
| **Terms of Service** | Platform usage terms | Legal | Legal Lead + Product Lead | REQUIRED |
| **Investment Advice Posture** | Educational vs advisory positioning | Legal | Legal Lead + SEBI Consultant | REQUIRED |
| **Data Export Controls** | Cross-border data transfer compliance | Legal | Legal Lead + Compliance Officer | REQUIRED |

### Product & User Experience
| Area | Description | Primary Owner | Required Approvers | Decision Level |
|------|-------------|---------------|-------------------|----------------|
| **Ranking Algorithm Weights** | Feature weights and scoring methodology | Product | Product Lead + Quant Lead | HIGH |
| **Calibration Thresholds** | Precision targets and confidence thresholds | Product | Product Lead + ML Engineer | HIGH |
| **User Interface Design** | App screens, flows, and interactions | Product | Product Lead + UX Designer | MEDIUM |
| **Disclaimer Placement** | Where and how disclaimers appear | Product | Product Lead + Legal Lead | HIGH |
| **"Uncertain" UX Logic** | How low-confidence results are handled | Product | Product Lead + ML Engineer | HIGH |
| **Monetization Features** | Freemium gates, premium features | Product | Product Lead + Business Lead | MEDIUM |

### Engineering & Technical
| Area | Description | Primary Owner | Required Approvers | Decision Level |
|------|-------------|---------------|-------------------|----------------|
| **Architecture Decisions** | Technology stack and patterns | Engineering | Tech Lead + Senior Engineer | HIGH |
| **API Design** | Endpoints, schemas, rate limits | Engineering | Tech Lead + Backend Engineer | MEDIUM |
| **Security Implementation** | Authentication, encryption, access control | Engineering | Tech Lead + Security Engineer | HIGH |
| **Database Schema** | Data models and relationships | Engineering | Tech Lead + Data Engineer | MEDIUM |
| **Deployment Strategy** | Infrastructure and DevOps approach | Engineering | Tech Lead + DevOps Engineer | MEDIUM |
| **Performance Targets** | Latency, throughput, availability SLAs | Engineering | Tech Lead + Product Lead | HIGH |

### Data & Analytics
| Area | Description | Primary Owner | Required Approvers | Decision Level |
|------|-------------|---------------|-------------------|----------------|
| **Feature Engineering** | Technical indicators and transformations | Data Science | Quant Lead + ML Engineer | HIGH |
| **Model Validation** | Backtesting methodology and metrics | Data Science | Quant Lead + Product Lead | REQUIRED |
| **Data Quality Rules** | Anomaly detection and validation logic | Data Science | Data Engineer + Quant Lead | MEDIUM |
| **LLM Prompt Engineering** | System prompts and safety guidelines | Data Science | ML Engineer + Legal Lead | HIGH |
| **Evaluation Metrics** | Success criteria and measurement approach | Data Science | Quant Lead + Product Lead | HIGH |

## Role Definitions

### Legal Domain
```yaml
Legal Lead:
  - Final authority on all compliance matters
  - Must approve all user-facing legal content
  - Signs off on data source usage rights
  - Escalation point for regulatory questions

DPO (Data Protection Officer):
  - DPDP Act compliance oversight
  - Privacy policy and consent mechanism approval
  - Data retention and deletion policy owner
  - Cross-border transfer authorization

SEBI Consultant:
  - Investment advisory compliance guidance
  - Review of ranking disclaimers
  - Educational vs advisory boundary setting
  - Regulatory filing recommendations
```

### Product Domain
```yaml
Product Lead:
  - User experience and feature prioritization
  - Business metrics and success criteria
  - Go-to-market strategy and positioning
  - Cross-functional coordination

Business Lead:
  - Monetization strategy and pricing
  - Market analysis and competitive positioning
  - Partnership and integration decisions
  - Revenue targets and business model

UX Designer:
  - User interface design and usability
  - Accessibility compliance (AA standard)
  - User research and feedback integration
  - Design system consistency
```

### Engineering Domain
```yaml
Tech Lead:
  - Technical architecture and design patterns
  - Code quality standards and review process
  - Technology evaluation and adoption
  - Performance and scalability planning

Senior Engineer:
  - Implementation standards and best practices
  - Code review and mentoring
  - Technical risk assessment
  - Cross-team technical coordination

Security Engineer:
  - Security architecture and threat modeling
  - Vulnerability assessment and penetration testing
  - Incident response planning
  - Compliance audit support
```

### Data Science Domain
```yaml
Quant Lead:
  - Quantitative model development and validation
  - Risk metrics and performance measurement
  - Research methodology and statistical rigor
  - Academic collaboration and publication

ML Engineer:
  - Machine learning pipeline development
  - Model deployment and monitoring
  - A/B testing framework and analysis
  - Automated model retraining and updates

Data Engineer:
  - Data pipeline architecture and reliability
  - Data quality monitoring and alerting
  - ETL/ELT process design and optimization
  - Database performance and scaling
```

## Approval Process

### Phase-Gate Approvals
```yaml
Phase M0 (Foundation):
  Required: Legal Lead, Product Lead
  Documents: data_sources.md, compliance.md, feature_flags.md
  
Phase M3 (Scoring):
  Required: Quant Lead, Product Lead, Legal Lead
  Deliverable: Calibration threshold τ and validation report
  
Phase M4 (LLM):
  Required: ML Engineer, Legal Lead
  Deliverable: Prompt templates and safety mechanisms
  
Phase M8 (Validation):
  Required: Quant Lead, Product Lead
  Deliverable: Performance validation report
  
Phase M11 (Launch):
  Required: Legal Lead, Product Lead, Tech Lead
  Deliverable: Store listing and privacy policy
```

### Decision Authority Matrix
```yaml
REQUIRED (Cannot proceed without approval):
  - Legal compliance matters
  - Data source licensing
  - Privacy policy changes
  - Investment advice positioning

HIGH (Strong recommendation needed):
  - Algorithm parameters
  - Performance targets  
  - Security implementation
  - User experience decisions

MEDIUM (Consultation recommended):
  - Technical implementation details
  - API design choices
  - Database schema changes
  - UI/UX refinements

LOW (Team discretion):
  - Code structure decisions
  - Development tool choices
  - Minor feature modifications
  - Performance optimizations
```

## Escalation Procedures

### Disagreement Resolution
```yaml
Level 1: Direct discussion between domain leads
Level 2: Cross-functional review meeting
Level 3: Executive stakeholder decision
Level 4: External consultant/legal counsel

Timeline: 
  - Level 1-2: 48 hours
  - Level 3: 1 week
  - Level 4: 2 weeks maximum
```

### Emergency Decisions
```yaml
Security Issues:
  - Security Engineer has immediate authority
  - Post-incident review within 24 hours
  - Legal notification if data involved

Regulatory Compliance:
  - Legal Lead has immediate halt authority
  - All affected features disabled pending review
  - 48-hour resolution target

Performance/Outages:
  - Tech Lead has operational authority
  - Business impact assessment within 1 hour
  - Post-mortem within 1 week
```

## Documentation Requirements

### Sign-off Documentation
```yaml
Required for Each Approval:
  - Written confirmation (email/document)
  - Date and timestamp of approval
  - Specific scope of approval
  - Any conditions or limitations
  - Next review date if applicable

Retention:
  - Legal approvals: 7 years
  - Product decisions: 3 years
  - Technical decisions: 2 years
  - All decisions: Accessible for audit
```

### Change Management
```yaml
Approval Changes:
  - Changes to approved items require re-approval
  - Material changes trigger full review cycle
  - Minor changes may use expedited process
  - Emergency changes have retroactive approval
```

## Contact Information

### Primary Contacts
```yaml
# [TO BE FILLED BY HUMAN INTERVENTION]

Legal Domain:
  Legal Lead: [NAME] <email>
  DPO: [NAME] <email>
  SEBI Consultant: [NAME] <email>

Product Domain:
  Product Lead: [NAME] <email>
  Business Lead: [NAME] <email>
  UX Designer: [NAME] <email>

Engineering Domain:
  Tech Lead: [NAME] <email>
  Senior Engineer: [NAME] <email>
  Security Engineer: [NAME] <email>

Data Science Domain:
  Quant Lead: [NAME] <email>
  ML Engineer: [NAME] <email>
  Data Engineer: [NAME] <email>
```

### Escalation Contacts
```yaml
# [TO BE FILLED BY HUMAN INTERVENTION]

Executive Level:
  CEO/Founder: [NAME] <email>
  CTO: [NAME] <email>
  Chief Legal Officer: [NAME] <email>

External Consultants:
  SEBI Advisory: [FIRM] <contact>
  Legal Counsel: [FIRM] <contact>
  Compliance Auditor: [FIRM] <contact>
```

## Review Schedule

### Regular Reviews
```yaml
Quarterly: Full approval authority review
Monthly: Contact information updates
Weekly: Active decision tracking
Daily: Critical path approvals monitoring
```

### Annual Reviews
```yaml
Authority Matrix: Annual review of decision levels
Process Efficiency: Approval timeline optimization  
Role Definitions: Responsibility boundary updates
Escalation Procedures: Process improvement review
```

---

## Implementation Checklist

### Phase M0 Immediate Actions
- [ ] Identify and confirm all role owners
- [ ] Establish communication channels
- [ ] Create approval tracking system
- [ ] Document emergency contact procedures

### Phase M1-M11 Ongoing
- [ ] Track approvals against phase gates
- [ ] Maintain decision audit trail
- [ ] Regular process adherence review
- [ ] Stakeholder feedback integration

---
*Last Updated: 2025-08-10*
*Owner: [PENDING - Human Intervention Required]*
*Next Review: After role assignments confirmed*