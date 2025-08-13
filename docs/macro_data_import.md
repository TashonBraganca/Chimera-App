# Macro Economic Data Import Specifications

## Purpose
Define import procedures, data sources, and specifications for macro economic data from RBI, MoSPI, and FBIL to support ranking and economic context analysis.

## Data Sources Overview

### Reserve Bank of India (RBI) Data Warehouse
- **Base URL**: https://dbie.rbi.org.in/
- **API Access**: DBIE (Database on Indian Economy) REST API
- **Update Frequency**: Daily/Weekly/Monthly based on series
- **Key Series**: Policy rates, exchange rates, money supply, credit growth

### Ministry of Statistics & Programme Implementation (MoSPI)  
- **Base URL**: http://mospi.nic.in/
- **Data Access**: CSV downloads, some API access
- **Update Frequency**: Monthly for CPI/WPI, Quarterly for GDP
- **Key Series**: CPI, WPI, GDP, Industrial Production Index

### Financial Benchmarks India Limited (FBIL)
- **Base URL**: https://www.fbil.org.in/
- **Data Access**: Direct downloads, RSS feeds
- **Update Frequency**: Daily for rates, Real-time during market hours
- **Key Series**: MIBOR, G-Sec yields, Corporate bond indices

## RBI Data Integration

### Priority Data Series
```yaml
rbi_series:
  policy_rates:
    - series_id: "POLICY_REPO_RATE"
      description: "RBI Policy Repo Rate"
      frequency: "IRREGULAR"
      units: "Percent per annum"
      
    - series_id: "POLICY_REVERSE_REPO_RATE"  
      description: "RBI Reverse Repo Rate"
      frequency: "IRREGULAR"
      units: "Percent per annum"
      
  exchange_rates:
    - series_id: "USD_INR_REFERENCE_RATE"
      description: "USD-INR Reference Rate"
      frequency: "DAILY"
      units: "INR per USD"
      
    - series_id: "EUR_INR_REFERENCE_RATE"
      description: "EUR-INR Reference Rate" 
      frequency: "DAILY"
      units: "INR per EUR"
      
  money_supply:
    - series_id: "M3_MONEY_SUPPLY"
      description: "Broad Money (M3)"
      frequency: "FORTNIGHTLY"
      units: "INR Crores"
      
  credit_growth:
    - series_id: "BANK_CREDIT_GROWTH_YOY"
      description: "Bank Credit Growth (YoY)"
      frequency: "FORTNIGHTLY"  
      units: "Percent"
```

### RBI Data Adapter Implementation
```java
@Component
@ConditionalOnProperty(name = "data.sources.rbi.enabled", havingValue = "true")
public class RbiDataAdapter implements DataSourceAdapter<MacroEconomicData> {
    
    @Value("${data.sources.rbi.api-url}")
    private String apiUrl;
    
    @Value("${data.sources.rbi.api-key:}")
    private String apiKey;
    
    private final List<String> prioritySeries = Arrays.asList(
        "POLICY_REPO_RATE",
        "USD_INR_REFERENCE_RATE", 
        "M3_MONEY_SUPPLY",
        "BANK_CREDIT_GROWTH_YOY"
    );
    
    @Override
    @RateLimiter(name = "rbi-api")
    @CircuitBreaker(name = "rbi-circuit")
    public CompletableFuture<IngestionResult<MacroEconomicData>> fetchData(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<MacroEconomicData> allData = new ArrayList<>();
                
                for (String seriesId : prioritySeries) {
                    List<MacroEconomicData> seriesData = fetchRbiSeries(seriesId, request);
                    allData.addAll(seriesData);
                }
                
                return IngestionResult.<MacroEconomicData>builder()
                    .data(allData)
                    .sourceUrl(apiUrl)
                    .fetchedAt(Instant.now())
                    .rowCount(allData.size())
                    .status(IngestionStatus.SUCCESS)
                    .build();
                    
            } catch (Exception e) {
                log.error("Failed to fetch RBI data", e);
                return IngestionResult.<MacroEconomicData>builder()
                    .status(IngestionStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            }
        });
    }
    
    private List<MacroEconomicData> fetchRbiSeries(String seriesId, FetchRequest request) {
        String endpoint = String.format("%s/DBIE/dbie.rbi?format=json&fromdate=%s&todate=%s&series=%s",
            apiUrl, 
            request.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            request.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
            seriesId
        );
        
        try {
            ResponseEntity<RbiApiResponse> response = restTemplate.getForEntity(endpoint, RbiApiResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseRbiResponse(response.getBody(), seriesId);
            } else {
                throw new DataIngestionException("RBI API returned error: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            throw new DataIngestionException("Failed to call RBI API for series: " + seriesId, e);
        }
    }
    
    private List<MacroEconomicData> parseRbiResponse(RbiApiResponse response, String seriesId) {
        List<MacroEconomicData> data = new ArrayList<>();
        
        if (response.getData() != null) {
            for (RbiDataPoint point : response.getData()) {
                data.add(MacroEconomicData.builder()
                    .seriesId(seriesId)
                    .seriesName(point.getSeriesName())
                    .period(LocalDate.parse(point.getPeriod(), DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .value(new BigDecimal(point.getValue()))
                    .units(point.getUnits())
                    .frequency(point.getFrequency())
                    .source("RBI")
                    .ingestionTime(Instant.now())
                    .build());
            }
        }
        
        return data;
    }
}
```

## MoSPI Data Integration

### Consumer Price Index (CPI) Data
```yaml
mospi_series:
  consumer_price_index:
    - series_id: "CPI_COMBINED"
      description: "CPI - Combined"
      base_year: "2012=100"
      frequency: "MONTHLY"
      
    - series_id: "CPI_RURAL"
      description: "CPI - Rural"
      base_year: "2012=100"
      frequency: "MONTHLY"
      
    - series_id: "CPI_URBAN"
      description: "CPI - Urban" 
      base_year: "2012=100"
      frequency: "MONTHLY"
      
  wholesale_price_index:
    - series_id: "WPI_ALL_COMMODITIES"
      description: "WPI - All Commodities"
      base_year: "2011-12=100"
      frequency: "MONTHLY"
      
    - series_id: "WPI_FOOD_ARTICLES"
      description: "WPI - Food Articles"
      base_year: "2011-12=100"
      frequency: "MONTHLY"
```

### MoSPI Data Adapter Implementation  
```java
@Component
@ConditionalOnProperty(name = "data.sources.mospi.enabled", havingValue = "true")
public class MospiDataAdapter implements DataSourceAdapter<MacroEconomicData> {
    
    @Value("${data.sources.mospi.base-url}")
    private String baseUrl;
    
    private final Map<String, String> datasetUrls = Map.of(
        "CPI_COMBINED", "/sites/default/files/data/cpi_combined.csv",
        "WPI_ALL_COMMODITIES", "/sites/default/files/data/wpi_all.csv"
    );
    
    @Override
    @RateLimiter(name = "mospi-api")
    public CompletableFuture<IngestionResult<MacroEconomicData>> fetchData(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<MacroEconomicData> allData = new ArrayList<>();
                
                for (Map.Entry<String, String> dataset : datasetUrls.entrySet()) {
                    String seriesId = dataset.getKey();
                    String dataPath = dataset.getValue();
                    
                    List<MacroEconomicData> seriesData = fetchMospiDataset(seriesId, dataPath);
                    allData.addAll(seriesData);
                }
                
                return IngestionResult.<MacroEconomicData>builder()
                    .data(allData)
                    .sourceUrl(baseUrl)
                    .fetchedAt(Instant.now())
                    .rowCount(allData.size())
                    .status(IngestionStatus.SUCCESS)
                    .build();
                    
            } catch (Exception e) {
                log.error("Failed to fetch MoSPI data", e);
                return IngestionResult.<MacroEconomicData>builder()
                    .status(IngestionStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            }
        });
    }
    
    private List<MacroEconomicData> fetchMospiDataset(String seriesId, String dataPath) {
        String downloadUrl = baseUrl + dataPath;
        
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String csvContent = new String(response.getBody(), StandardCharsets.UTF_8);
                return parseMospiCsv(csvContent, seriesId);
            } else {
                throw new DataIngestionException("MoSPI download failed: " + response.getStatusCode());
            }
            
        } catch (RestClientException e) {
            throw new DataIngestionException("Failed to download MoSPI data: " + seriesId, e);
        }
    }
    
    private List<MacroEconomicData> parseMospiCsv(String csvContent, String seriesId) {
        List<MacroEconomicData> data = new ArrayList<>();
        String[] lines = csvContent.split("\n");
        
        // Skip header row
        for (int i = 1; i < lines.length; i++) {
            String[] fields = lines[i].split(",");
            
            if (fields.length >= 3) {
                try {
                    LocalDate period = parseDate(fields[0].trim());
                    BigDecimal value = new BigDecimal(fields[1].trim());
                    
                    data.add(MacroEconomicData.builder()
                        .seriesId(seriesId)
                        .seriesName(getSeriesName(seriesId))
                        .period(period)
                        .value(value)
                        .units(getSeriesUnits(seriesId))
                        .frequency("MONTHLY")
                        .source("MoSPI")
                        .ingestionTime(Instant.now())
                        .build());
                        
                } catch (Exception e) {
                    log.warn("Failed to parse MoSPI data line: {}", lines[i], e);
                }
            }
        }
        
        return data;
    }
}
```

## FBIL Data Integration

### Interest Rate Series
```yaml
fbil_series:
  mibor_rates:
    - series_id: "MIBOR_1D"
      description: "Mumbai Interbank Offered Rate - 1 Day"
      frequency: "DAILY"
      units: "Percent per annum"
      
    - series_id: "MIBOR_7D"
      description: "Mumbai Interbank Offered Rate - 7 Days"
      frequency: "DAILY" 
      units: "Percent per annum"
      
  gsec_yields:
    - series_id: "GSEC_1Y"
      description: "Government Security Yield - 1 Year"
      frequency: "DAILY"
      units: "Percent per annum"
      
    - series_id: "GSEC_10Y" 
      description: "Government Security Yield - 10 Year"
      frequency: "DAILY"
      units: "Percent per annum"
```

### FBIL Data Adapter Implementation
```java
@Component
@ConditionalOnProperty(name = "data.sources.fbil.enabled", havingValue = "true")
public class FbilDataAdapter implements DataSourceAdapter<MacroEconomicData> {
    
    @Value("${data.sources.fbil.base-url}")
    private String baseUrl;
    
    @Value("${data.sources.fbil.data-path}")
    private String dataPath;
    
    @Override
    @RateLimiter(name = "fbil-api")
    public CompletableFuture<IngestionResult<MacroEconomicData>> fetchData(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // FBIL provides daily yield data via Excel download
                String downloadUrl = baseUrl + dataPath + "?date=" + 
                    request.getTargetDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                
                byte[] excelData = downloadFbilData(downloadUrl);
                List<MacroEconomicData> yieldData = parseFbilExcel(excelData, request.getTargetDate());
                
                return IngestionResult.<MacroEconomicData>builder()
                    .data(yieldData)
                    .sourceUrl(downloadUrl)
                    .fetchedAt(Instant.now())
                    .rowCount(yieldData.size())
                    .status(IngestionStatus.SUCCESS)
                    .build();
                    
            } catch (Exception e) {
                log.error("Failed to fetch FBIL data", e);
                return IngestionResult.<MacroEconomicData>builder()
                    .status(IngestionStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            }
        });
    }
    
    private List<MacroEconomicData> parseFbilExcel(byte[] excelData, LocalDate targetDate) {
        List<MacroEconomicData> data = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                
                Cell instrumentCell = row.getCell(0);
                Cell yieldCell = row.getCell(1);
                
                if (instrumentCell != null && yieldCell != null) {
                    String instrument = instrumentCell.getStringCellValue();
                    double yieldValue = yieldCell.getNumericCellValue();
                    
                    data.add(MacroEconomicData.builder()
                        .seriesId(mapFbilInstrumentToSeriesId(instrument))
                        .seriesName(instrument)
                        .period(targetDate)
                        .value(BigDecimal.valueOf(yieldValue))
                        .units("Percent per annum")
                        .frequency("DAILY")
                        .source("FBIL")
                        .ingestionTime(Instant.now())
                        .build());
                }
            }
            
        } catch (Exception e) {
            throw new DataIngestionException("Failed to parse FBIL Excel data", e);
        }
        
        return data;
    }
}
```

## Data Storage Schema

### Macro Economic Data Table
```sql
CREATE TABLE macro_economic_data (
    id BIGSERIAL PRIMARY KEY,
    series_id VARCHAR(100) NOT NULL,
    series_name VARCHAR(255) NOT NULL,
    period DATE NOT NULL,
    value DECIMAL(20,6) NOT NULL,
    units VARCHAR(100),
    frequency VARCHAR(20),
    source VARCHAR(50) NOT NULL,
    base_year VARCHAR(20),
    seasonal_adjustment VARCHAR(50),
    ingestion_time TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    provenance_id UUID REFERENCES data_provenance(id),
    
    CONSTRAINT uk_macro_series_period UNIQUE (series_id, period),
    INDEX idx_macro_series_period (series_id, period DESC),
    INDEX idx_macro_period (period DESC),
    INDEX idx_macro_source (source)
);

-- Time-series extension for efficient querying
SELECT create_hypertable('macro_economic_data', 'period');
```

### Macro Data Series Metadata
```sql  
CREATE TABLE macro_series_metadata (
    series_id VARCHAR(100) PRIMARY KEY,
    series_name VARCHAR(255) NOT NULL,
    description TEXT,
    source VARCHAR(50) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    units VARCHAR(100),
    base_year VARCHAR(20),
    start_date DATE,
    last_updated DATE,
    is_active BOOLEAN DEFAULT TRUE,
    update_schedule VARCHAR(100),
    data_lag_days INTEGER,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

## Import Job Scheduling

### Macro Data Import Schedule
```java
@Configuration
public class MacroDataScheduleConfig {
    
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Kolkata") // Daily at 12 PM IST
    public void scheduleRbiDailyImport() {
        scheduleJob(JobType.MACRO_RBI_DAILY, createRbiDailyParameters());
    }
    
    @Scheduled(cron = "0 0 15 * * FRI", zone = "Asia/Kolkata") // Weekly on Friday 3 PM IST
    public void scheduleRbiWeeklyImport() {
        scheduleJob(JobType.MACRO_RBI_WEEKLY, createRbiWeeklyParameters());
    }
    
    @Scheduled(cron = "0 0 10 15 * *", zone = "Asia/Kolkata") // Monthly on 15th at 10 AM IST
    public void scheduleMospiMonthlyImport() {
        scheduleJob(JobType.MACRO_MOSPI_MONTHLY, createMospiMonthlyParameters());
    }
    
    @Scheduled(cron = "0 30 17 * * MON-FRI", zone = "Asia/Kolkata") // Weekdays 5:30 PM IST
    public void scheduleFbilDailyImport() {
        scheduleJob(JobType.MACRO_FBIL_DAILY, createFbilDailyParameters());
    }
}
```

## Data Quality Specifications

### Macro Data Quality Thresholds
```yaml
macro_data_quality:
  completeness:
    rbi: 0.95      # 95% of expected series should have data
    mospi: 0.90    # 90% completeness for monthly data
    fbil: 0.98     # 98% completeness for daily rates
    
  accuracy:
    value_ranges:
      policy_rates: [0, 20]      # 0-20% for policy rates
      exchange_rates: [50, 100]  # 50-100 for USD-INR
      cpi_values: [50, 300]      # 50-300 for CPI index
      yields: [0, 25]            # 0-25% for bond yields
      
  consistency:
    date_format: "YYYY-MM-DD"
    decimal_places: 6
    required_fields: ["series_id", "period", "value", "source"]
    
  timeliness:
    max_delay_days:
      daily_series: 1    # 1 day delay acceptable
      weekly_series: 7   # 1 week delay acceptable  
      monthly_series: 30 # 1 month delay acceptable
```

## Configuration

### Macro Data Configuration Properties
```yaml
chimera:
  macro-data:
    sources:
      rbi:
        enabled: true
        api-url: "https://dbie.rbi.org.in"
        api-key: ${RBI_API_KEY:}
        timeout: 60s
        
      mospi:
        enabled: true
        base-url: "http://mospi.nic.in"
        timeout: 120s
        
      fbil:
        enabled: true
        base-url: "https://www.fbil.org.in"
        data-path: "/daily-rates-download"
        timeout: 30s
        
    import:
      batch-size: 1000
      parallel-processing: true
      max-concurrent-imports: 3
      
    retention:
      historical-data-years: 10
      cleanup-frequency: "0 0 2 1 * *"  # Monthly cleanup
```

---

**Document Version**: 1.0  
**Last Updated**: 2025-08-10  
**Implementation Target**: Phase M2  
**Dependencies**: AlternativeDataService, DataQualityService, Apache POI for Excel parsing