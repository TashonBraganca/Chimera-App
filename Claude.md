```markdown
# MVP — PHASE-BY-PHASE MASTER PLAN (30–40 Days)
Purpose
- Ship a legal, fast, useful MVP that ranks Indian assets (NSE/BSE equities and
  AMFI mutual funds), explains “why” via LLM with citations, and runs on a clean
  3-screen Flutter Android app. Everything is transparent, calibrated, and Play-Store
  ready.

Guiding Principle
- Optimize for precision on displayed picks using calibrated probability and
  abstention. Show “Uncertain” when confidence is low. Track precision@k,
  Kendall’s \\( \\tau \\), and net returns including India-specific costs.

```

```markdown
# PHASE M0 — REALITY, SOURCING, AND COMPLIANCE (Duration: 1 Day)

## Purpose
Lock legal data sources, disclaimers, and posture. Avoid rework later.

## Definition of Done (DoD)
- docs/data_sources.md approved; TOS/usage for each source VERIFIED.
- Disclaimers approved; “Research/Education” posture confirmed.
- Hosting, DB, and secret-store decisions made.

## Tasks (To‑Do, Agent‑Executable)
- [ ] Update docs/data_sources.md with APPROVED MVP sources:
      NSE/BSE EOD archives (VERIFY TOS & cadence), AMFI NAV (official),
      RBI DW, MoSPI CPI/WPI, FBIL yields, GDELT (India filter), Reuters/PIB RSS,
      Google Trends (TOS-compliant). Add near-RT vendor placeholders (deferred).
- [ ] Update docs/compliance.md with disclaimers and DPDP posture:
      educational only, consent text, retention policy, export/delete.
- [ ] Update docs/feature_flags.md defaults:
      risky sources OFF (Reddit/Broker/Quantum/Neuromorphic).
- [ ] Choose infra track (PaaS recommended) and secret store (provider vault).
- [ ] Create docs/acceptance_owner_map.md listing signers for Product/Eng/Legal.

## Risks & VERIFY
- [ ] VERIFY each source TOS before enabling server-side ingestion.
- [ ] VERIFY disclaimers appear in every results/chat design mock.

## Human Intervention
- Approve data sources and disclaimers; choose hosting (Fly.io/Render/Railway).

```

```markdown
# PHASE M1 — REPO FOUNDATION & HEALTH (Duration: 2 Days)

## Purpose
Create mono-repo skeleton (backend + Flutter) with health endpoints and docs.

## DoD
- Backend health endpoints planned; Flutter app shell planned; CI draft ready.
- Docs present: tech_stack, architecture overview, feature flags.

## Tasks
- [ ] Create repo structure: backend/, flutter_app/, docs/, scripts/, infra/.
- [ ] Document tech stack versions (Java 21, Gradle, Spring Boot 3.3.x,
      Flutter/Dart, Postgres 16 + Timescale, Redis 7).
- [ ] Health & observability plan:
      /health/ready, /freshness summary, Sentry DSNs via env.
- [ ] CI plan doc: build/test/lint, no secrets in logs, PR gates.

## Risks & VERIFY
- [ ] VERIFY no secrets in repo (gitleaks).
- [ ] VERIFY toolchain versions installed locally.

## Human Intervention
- Provide Sentry DSNs (optional) and choose CI provider (GitHub Actions).

```

```markdown
# PHASE M2 — DATA INGESTION (EOD + NEWS) (Duration: 3 Days)

## Purpose
Ingest EOD equities (bhavcopy), AMFI NAVs, macro series, and news metadata.
Track freshness and anomalies.

## DoD
- Idempotent ingestors for NSE/BSE EOD and AMFI NAV; macro placeholders.
- News metadata (titles/links/summaries) from GDELT + Reuters/PIB RSS.
- Ingestion audit + freshness endpoints documented; anomalies flagged.

## Tasks
- [ ] docs/ingestion_jobs.md: schedule windows (IST), retries/backoff, rate limits.
- [ ] backend/service specs:
      - [ ] AlternativeDataService.md: source adapters with feature flags.
      - [ ] DataIngestionService.md: orchestrate daily EOD/NAV and periodic news.
      - [ ] DataQualityPolicy.md: schema, range checks, anomaly z-score
            \\( z = \\frac{x - \\mu}{\\sigma} \\) flag at \\( |z| > 6 \\).
      - [ ] FreshnessService.md: last_success_ts, rows, anomalies per source.
- [ ] Provenance: store source URL, fetched_at (UTC), checksum, counts.
- [ ] Macro: add RBI/MoSPI/FBIL series IDs and import script spec.

## Acceptance
- [ ] Re-run ingestion without duplicates; anomalies logged; freshness non-empty.

## Risks & VERIFY
- [ ] VERIFY NSE/BSE access patterns and caching; be polite (throttle).

## Human Intervention
- Confirm publication windows; provide any API keys (NewsAPI optional).

```

```markdown
# PHASE M3 — FEATURES, SCORING, CALIBRATION (Duration: 4 Days)

## Purpose
Compute features; rank assets with a clear scoring formula; calibrate
probabilities; gate on confidence for 90%+ precision on displayed picks.

## DoD
- Feature vectors per asset/day with provenance.
- Scoring formula documented; weights tuned by horizon.
- Calibrated probability \\( p \\) and threshold \\( \\tau \\) established to meet
  precision target on validation; “Uncertain” path specified.

## Tasks
- [ ] backend/FeatureEngineeringService.md:
      - [ ] Features: multi-horizon returns, EWMA volatility
            \\( \\sigma_t^2 = (1-\\lambda) r_t^2 + \\lambda \\sigma_{t-1}^2 \\),
            max drawdown, volume z-score/liquidity, 12–1 momentum, 1-week reversal,
            sentiment score \\(\\in[-1,1]\\) (from news), ESG proxy if available.
      - [ ] Normalize via cross-sectional z-scores (sector & global).
- [ ] backend/RankingService.md:
      - [ ] Transparent score:
            \\( \\text{Score}_i = w_r z(\\hat{\\mu}_i) - w_\\sigma z(\\hat{\\sigma}_i)
               - w_d z(\\text{DD}_i) + w_l z(\\text{liq}_i)
               + w_s z(\\text{sent}_i) + w_e z(\\text{ESG}_i) \\),
            \\( w \\ge 0, \\sum w = 1 \\); presets per horizon (short/med/long).
      - [ ] Output: ranked list with score, confidence%, “Last updated”, citations.
- [ ] Calibration plan:
      - [ ] Time-series CV (rolling); reliability curves; calibrate with
            isotonic/Platt; store \\( \\tau \\) so that validation precision@k
            \\( \\ge 0.90 \\) for displayed picks; abstain otherwise.
- [ ] docs/feature_weights_tuning.md: grid-search ranges and chosen defaults.

## Acceptance
- [ ] Validation report with precision@k, Kendall’s \\( \\tau \\), Brier score.
- [ ] Threshold \\( \\tau \\) recorded; “Uncertain” labeling implemented in spec.

## Risks & VERIFY
- [ ] VERIFY no leakage (purged CV with embargo).

## Human Intervention
- Approve target \\( \\tau \\) and weight presets per horizon.

```

```markdown
# PHASE M4 — LLM EXPLAIN (RAG-FIRST) (Duration: 3 Days)

## Purpose
Explain rankings and answer user questions with citations and disclaimers.

## DoD
- RAG index (Postgres + pgvector) from approved sources; chunking/embeddings set.
- Chat/explain specs with mandatory citations, “Last updated,” and refusal policy.

## Tasks
- [ ] backend/RagIndexService.md:
      - [ ] Tables: documents, chunks, index_run; chunk size 500–800 tokens with overlap.
      - [ ] Embeddings provider selection (local or API); top‑k retrieval with filters.
      - [ ] Index refresh cadences (30–120 min news; daily filings).
- [ ] backend/CustomLLMService.md:
      - [ ] Provider-agnostic calls; system prompt injects disclaimer, requires citations,
            refuses personalized advice, and shows timestamps from FreshnessService.
      - [ ] Response JSON contract: answer_text, citations[], confidence_percent,
            last_updated_ist, safety_flags[].
- [ ] backend/AgentOrchestratorService.md:
      - [ ] Macro/Technical/Fundamental/Sentiment agent summaries (3–5 bullets each)
            with weights \\( w_m, w_t, w_f, w_s \\), \\( \\sum w = 1 \\).
- [ ] backend/ChatController.md:
      - [ ] /chat/ask and /rank/explain endpoint specs.

## Acceptance
- [ ] 100% citation presence; refusal policy verified on 10 prompts.
- [ ] p95 chat \\( \\le 2.5 \\) s on small answers (\\(< 300\\) tokens).

## Risks & VERIFY
- [ ] VERIFY licensing of indexed content; no scraped paywalled text.

## Human Intervention
- Provide LLM and embedding API keys (or confirm local inference).

```

```markdown
# PHASE M5 — BACKEND APIS (RANK, EXPLAIN, FRESHNESS) (Duration: 2 Days)

## Purpose
Expose minimal APIs for Android: rank, explain, freshness.

## DoD
- /rank, /explain, /freshness specs finalized; auth/rate limits documented.

## Tasks
- [ ] backend/AnalyticsController.md:
      - [ ] POST /rank: { amount_inr, horizon_days, risk_pref?, filters? }
            returns ranked list with score, confidence, last_updated, disclaimer.
      - [ ] POST /rank/explain: returns per-asset “why” with citations.
- [ ] backend/FreshnessController.md:
      - [ ] GET /freshness: per-source last_success_ts, age (mins), anomalies.
- [ ] Security & rate limits:
      - [ ] JWT spec, public rate limit, input validation, error payloads.

## Acceptance
- [ ] Example cURL payloads and JSON responses in docs/api_examples.md.

## Risks & VERIFY
- [ ] VERIFY idempotent behavior and 4xx/5xx semantics.

## Human Intervention
- Approve rate-limit thresholds and JWT policy (if auth used in MVP).

```

```markdown
# PHASE M6 — FLUTTER ANDROID APP (3-SCREEN UX) (Duration: 5 Days)

## Purpose
Deliver a fast, minimal, accessible app: Input → Results → Chat. Offline-first.

## DoD
- Flutter screens built; offline cache; accessibility AA; crash-free ≥ 99.5%.

## Tasks
- [ ] UI Screens:
      - [ ] Input: ₹ amount, horizon, risk; submit to /rank; show disclaimers.
      - [ ] Results: list top‑k with score, confidence%, “Last updated”; sort/tabs
            1D/1W/1M; button “Explain” → /rank/explain.
      - [ ] Chat: concise Q&A with citations and timestamps; refusal text ready.
- [ ] Performance:
      - [ ] Flutter performance optimization; App Startup optimization; off‑main‑thread
            chart data prep; stable keys in lists; efficient state management.
- [ ] Offline-first:
      - [ ] Room cache for last results; banner “Using cached data from HH:MM IST.”
- [ ] Notifications:
      - [ ] FCM stub and deep links; categories defined but can be OFF initially.
- [ ] Accessibility:
      - [ ] TalkBack content descriptions; large font support; high contrast theme.

## Acceptance
- [ ] p95 cold start \\( < 1.2 \\) s; frame time \\( \\le 16 \\) ms p95; no ANRs in smoke tests.
- [ ] “Disclaimers + Last updated” visible on Results and Chat.

## Risks & VERIFY
- [ ] VERIFY Data Safety form mapping; minimal permissions (INTERNET, BIOMETRICS).

## Human Intervention
- Provide app icon/branding and copy; approve content in disclaimers.

```

```markdown
# PHASE M7 — DEPLOY & OBSERVABILITY (PaaS) (Duration: 2 Days)

## Purpose
Deploy staging/production with health checks, metrics, logs, and alerts.

## DoD
- Staging and prod deployed over HTTPS; health gates; dashboards live; alerts set.

## Tasks
- [ ] PaaS plan:
      - [ ] Deploy backend container; configure env and secrets; Postgres/Redis managed.
      - [ ] Health checks /health/ready; autoscale bounds.
- [ ] Observability:
      - [ ] Metrics (latency p50/p95/p99; error rate; job success; freshness).
      - [ ] Logs JSON structured; PII redaction; tracing minimal.
- [ ] Alerts:
      - [ ] Latency/error/freshness thresholds; alert channels set (email/Slack).

## Acceptance
- [ ] Synthetic monitors for /rank, /chat, /freshness; alerts fire on induced errors.

## Risks & VERIFY
- [ ] VERIFY region/data residency per DPDP (prefer ap-south-1 or nearest).

## Human Intervention
- Provide domain/DNS; choose alert channels; confirm autoscale bounds.

```

```markdown
# PHASE M8 — VALIDATION & CALIBRATION (Duration: 3 Days)

## Purpose
Prove MVP quality: calibrate thresholds; document metrics; prepare dashboard.

## DoD
- Validation report with precision@k, Kendall’s \\( \\tau \\), Brier score,
  calibration plots; \\( \\tau \\) selected to hit precision \\( \\ge 0.90 \\) on displayed picks.

## Tasks
- [ ] Evaluation suite:
      - [ ] Time-series CV; walk-forward OOS window; compute precision@k,
            Kendall’s \\( \\tau \\), Sharpe/Sortino with India costs, turnover.
      - [ ] Calibration curves; Brier score; choose \\( \\tau \\) meeting precision target.
- [ ] LLM eval:
      - [ ] 50-Q curated set; citation presence=100%; hallucination \\( < 5\\% \\);
            refusal correctness verified.
- [ ] Dashboard:
      - [ ] Grafana panels for ranking quality, calibration, latency, freshness.

## Acceptance
- [ ] Signed evaluation PDF (or md) stored; thresholds baked into config.

## Risks & VERIFY
- [ ] VERIFY no leakage; costs included; “accuracy%” claims removed.

## Human Intervention
- Approve threshold \\( \\tau \\) and publish validation summary.

```

```markdown
# PHASE M9 — BETA & FEEDBACK (Duration: 3 Days)

## Purpose
Run a small closed beta (100–300 users) and incorporate feedback.

## DoD
- Closed beta distributed; survey + analytics captured; “understood why?”
  \\( \\ge 80\\% \\) target met; bugfix backlog prioritized.

## Tasks
- [ ] Distribution:
      - [ ] Play Console internal/closed track; Firebase App Distribution optional.
- [ ] Feedback:
      - [ ] In-app feedback link; Google Forms survey; collect time-to-first-value,
            clarity of “why,” performance satisfaction.
- [ ] Analytics:
      - [ ] Event logs (pseudonymous); clickthrough on “Explain”; crash-free rate.

## Acceptance
- [ ] Crash-free \\( \\ge 99.5\\% \\); latency SLOs green; survey meets thresholds.

## Risks & VERIFY
- [ ] VERIFY consent; anonymize analytics; opt-out path exists.

## Human Intervention
- Provide initial tester list; approve survey questions.

```

```markdown
# PHASE M10 — MONETIZATION TOGGLES (SAFE) (Duration: 1 Day)

## Purpose
Enable freemium gating and optional ads—OFF by default until after beta.

## DoD
- Plan gating spec live; ads specs documented; no ads on advice-like outputs.

## Tasks
- [ ] MonetizationService.md:
      - [ ] Free: 5 recs/day, delayed data, “Upgrade” CTA.
      - [ ] Premium stub: unlock caps (future billing integration).
- [ ] AdsPlan.md:
      - [ ] AdMob placements on education/help screens only; frequency caps;
            A/B plan (OFF by default).
- [ ] API entitlement checks in spec (server-side).

## Acceptance
- [ ] Gating logic validated in staging; ads OFF; no crashes with flags toggled.

## Risks & VERIFY
- [ ] VERIFY policy compliance (no ads near financial claims).

## Human Intervention
- Approve gating limits and copy; decide if ads remain OFF for beta.

```

```markdown
# PHASE M11 — LAUNCH PREP & LISTING (Duration: 2 Days)

## Purpose
Finalize store listing and staged rollout plan with clear, honest copy.

## DoD
- Privacy Policy/TOS linked; Data Safety form correct; screenshots show
  disclaimers and “Last updated”; staged rollout gates defined.

## Tasks
- [ ] Store assets:
      - [ ] Title, descriptions, screenshots with disclaimers visible; promo video optional.
- [ ] Policies:
      - [ ] Privacy Policy + TOS URLs; Data Safety accurate; content ratings.
- [ ] Rollout:
      - [ ] Staged rollout gates (crash rate \\( < 0.5\\% \\), latency targets, feedback
            threshold); auto-rollback criteria documented.

## Acceptance
- [ ] Listing passes precheck; staged rollout ready.

## Risks & VERIFY
- [ ] VERIFY no performance promises; honest methodology references only.

## Human Intervention
- Approve listing copy and staged rollout gates/date.

```

```markdown
# CURSOR AGENT — MVP EXECUTION PROMPT

You are to implement ONE MVP phase at a time from M0 to M11, strictly per each
phase’s Tasks and Acceptance/DoD. Do not proceed to the next phase without
explicit approval. Follow these rules:

Golden Rules
- Legal-first: only approved sources from docs/data_sources.md.
- No secrets committed; env-only; PII minimized and anonymized.
- Educational posture unless RIA; inject disclaimers in all user-facing outputs.
- Feature flags: risky/experimental OFF by default.

Execution Steps (per phase)
0) Read phase doc + Risks & VERIFY + Human Intervention list.
1) Produce a to-do checklist from Tasks; create/modify files and docs exactly as named.
2) Add tests/docs/seed data per spec; run lint/tests; ensure idempotency.
3) Verify Acceptance/DoD; generate phase_report.md with metrics and any blocked items.
4) Commit on branch phase-mX/<short>; open PR; stop.

Human Inputs Required
- Approvals for disclaimers, data sources, hosting choice, threshold \\( \\tau \\),
  store copy, and any API keys. If missing, stop and request them before coding.

```

```markdown
# QUICK SUCCESS METRICS (MVP)

- Data freshness: EOD ingested \\( \\le 30 \\) min post publish; news index refresh 30–120 min.
- Ranking performance: precision@k on displayed picks \\( \\ge 0.90 \\) via threshold \\( \\tau \\);
  Kendall’s \\( \\tau \\) reported; turnover and India costs included.
- LLM answers: citation rate 100%; hallucination \\( < 5\\% \\); p95 \\( \\le 2.5 \\) s.
- App quality: cold start \\( < 1.2 \\) s; frame time \\( \\le 16 \\) ms p95; crash-free \\( \\ge 99.5\\% \\).
- Compliance: disclaimers visible; DPDP posture documented; no restricted scraping.

```

```markdown
# HUMAN INTERVENTION — ONE-TIME BEFORE M2–M4

- Approve data sources and disclaimers (M0).
- Confirm hosting and secret store (M1).
- Provide optional keys: NewsAPI/LLM/Embeddings (M4).
- Approve calibration threshold \\( \\tau \\) and weight presets (M3/M8).
- Approve store listing copy and staged rollout gates (M11).

```

```markdown
# WHY THIS MVP WINS

- Useful on day 1: clear rankings, fast “why” with citations, offline support.
- Honest accuracy: calibrated, high-precision displayed picks; abstention on low-confidence.
- Legal and scalable: official/public sources; clean Play listing; staged rollout; cheap to run.
- Extensible: portfolio optimization, personalization, monetization ready behind flags.

```