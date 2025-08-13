# Data Sources - MVP Approved Sources

## Purpose
This document lists all approved data sources for the Chimera MVP, with verified Terms of Service (TOS) compliance and usage guidelines.

## Approved Sources

### Equity Data (NSE/BSE)
- **NSE EOD Archives**
  - Source: National Stock Exchange Bhavcopy
  - URL: https://www.nseindia.com/products-services/data-products
  - TOS Status: ✅ VERIFIED - Public domain EOD data
  - Usage: Daily EOD equity prices, volumes, corporate actions
  - Rate Limits: Respect server load, cache appropriately
  - Cadence: Daily post-market close (typically by 18:30 IST)

- **BSE EOD Archives**  
  - Source: Bombay Stock Exchange Bhavcopy
  - URL: https://www.bseindia.com/markets/equity/EQReports/
  - TOS Status: ✅ VERIFIED - Public domain EOD data
  - Usage: Daily EOD equity prices, volumes for cross-validation
  - Rate Limits: Respect server load, cache appropriately
  - Cadence: Daily post-market close

### Mutual Funds Data
- **AMFI NAV**
  - Source: Association of Mutual Funds in India
  - URL: https://www.amfiindia.com/spages/NAVAll.txt
  - TOS Status: ✅ VERIFIED - Official public data
  - Usage: Daily NAV for all mutual fund schemes
  - Rate Limits: Daily downloads only
  - Cadence: Daily by 21:00 IST

### Macroeconomic Data
- **RBI Database on Indian Economy (DBIE)**
  - Source: Reserve Bank of India
  - URL: https://dbie.rbi.org.in/DBIE/dbie.rbi?site=home
  - TOS Status: ✅ VERIFIED - Public domain
  - Usage: Interest rates, money supply, inflation indicators
  - Rate Limits: API rate limits apply
  - Cadence: Various (weekly/monthly/quarterly)

- **Ministry of Statistics and Programme Implementation (MoSPI)**
  - Source: Government of India
  - URL: http://www.mospi.gov.in/
  - TOS Status: ✅ VERIFIED - Public domain
  - Usage: CPI, WPI, GDP data
  - Rate Limits: Reasonable usage
  - Cadence: Monthly/quarterly releases

- **Financial Benchmarks India Limited (FBIL)**
  - Source: FBIL
  - URL: https://www.fbil.org.in/
  - TOS Status: ✅ VERIFIED - Public benchmark rates
  - Usage: Treasury bill rates, bond yields
  - Rate Limits: Daily access
  - Cadence: Daily

### News & Sentiment Data
- **GDELT Project (India Filter)**
  - Source: Global Database of Events, Language, and Tone
  - URL: https://www.gdeltproject.org/
  - TOS Status: ✅ VERIFIED - Academic/research use allowed
  - Usage: News sentiment, event data filtered for India
  - Rate Limits: API limits apply
  - Cadence: Real-time (15-minute updates)
  - Filter: Geographic filter for India-related events only

- **Reuters India RSS**
  - Source: Reuters
  - URL: https://www.reuters.com/world/india/
  - TOS Status: ✅ VERIFIED - RSS feeds for non-commercial research
  - Usage: News headlines and summaries
  - Rate Limits: Reasonable RSS polling
  - Cadence: Hourly checks

- **Press Information Bureau (PIB) RSS**
  - Source: Government of India
  - URL: https://pib.gov.in/
  - TOS Status: ✅ VERIFIED - Public domain
  - Usage: Government announcements and policy news
  - Rate Limits: Reasonable RSS polling
  - Cadence: As published

### Market Sentiment
- **Google Trends (India)**
  - Source: Google
  - URL: https://trends.google.com/trends/
  - TOS Status: ✅ VERIFIED - API terms compliant
  - Usage: Search trends for financial terms, company names
  - Rate Limits: API rate limits strictly observed
  - Cadence: Daily/weekly aggregates

## Near Real-Time Vendor Placeholders (DEFERRED)
The following sources are placeholders for future implementation, not part of MVP:
- Bloomberg Terminal API (pending subscription)
- Refinitiv Eikon (pending subscription)  
- Alpha Vantage India (evaluation phase)

## Prohibited Sources (Feature Flags OFF)
These sources are explicitly disabled via feature flags for MVP:
- Reddit financial discussions
- Broker research reports
- Quantum computing data sources
- Neuromorphic computing signals

## Data Retention Policy
- Raw data: 5 years for backtesting
- Processed features: 2 years
- News summaries: 1 year
- User queries: Anonymized, 90 days

## Compliance Notes
- All sources verified as public domain or with appropriate academic/research usage rights
- No paid subscription services in MVP to avoid TOS complications
- All data marked with source attribution and last updated timestamps
- DPDP Act 2023 compliance: minimal data collection, explicit consent, retention limits

## Review Schedule
- Quarterly TOS review for all sources
- Annual compliance audit
- Immediate review if any source changes TOS

---
*Last Updated: 2025-08-10*
*Reviewed By: [PENDING APPROVAL]*