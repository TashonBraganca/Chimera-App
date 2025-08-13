# Temporary Files & Organization Plan

## Purpose
Identify temporary, duplicate, and organizational files that need cleanup or consolidation during the Flutter/Gradle migration.

## Temporary Files to Review/Cleanup

### Backend Build Artifacts
```
backend/build/                    # Gradle build output (can be ignored/cleaned)
backend/target/                   # Maven build output (DELETE after Gradle migration)
backend/compile-errors.txt        # Temporary compilation logs (DELETE)
backend/delete-duplicates.txt     # Temporary file (DELETE)
```

### Backup and Source Files
```
backend/src_backup/               # Backup of previous source code (REVIEW → ARCHIVE)
  - Contains comprehensive service implementations
  - May have useful code for missing implementations
  - Keep until migration is complete, then archive
```

### Android vs Flutter Duplication
```
android/                          # Native Android project (REVIEW → REMOVE?)
  - Contains Kotlin/Compose implementation
  - May conflict with Flutter app (chimera_flutter/)
  - Decision needed: Keep both or remove native Android?
  
chimera_flutter/                  # Flutter implementation (KEEP - PRIMARY)
  - This is our main mobile app
  - Already implemented with proper architecture
```

### Report Files in Root
```
phase_m3_report.md               # Should be in docs/ (MOVE)
phase_m4_report.md               # Should be in docs/ (MOVE) 
phase_m6_report.md               # Should be in docs/ (MOVE)
```

## Organizational Actions Needed

### 1. Move Report Files
- Move `phase_m*.md` from root to `docs/` directory
- Maintain consistent documentation structure

### 2. Backend Build System Cleanup
- Complete Maven → Gradle migration
- Remove `pom.xml`, `target/` directory
- Keep `backend/src_backup/` temporarily for reference
- Clean up temporary error logs

### 3. Mobile App Architecture Decision
**Current State:**
- `android/` - Native Kotlin/Compose app (partially implemented)
- `chimera_flutter/` - Flutter app (fully implemented, documented in M6 report)

**Recommendation:**
- **Keep `chimera_flutter/`** as primary mobile app
- **Archive `android/`** for reference (move to `archived_android/`)
- Update all documentation to reference Flutter app only

### 4. File Structure After Cleanup
```
D:\Chimera MVP\
├── backend/                     # Java/Spring Boot with Gradle
├── chimera_flutter/             # Primary Flutter mobile app
├── docs/                        # All documentation and reports
├── scripts/                     # Setup and utility scripts
├── infra/                       # Infrastructure configurations
├── docker-compose.yml           # Local development environment
├── CLAUDE.md                    # Updated for Flutter/Railway
├── SETUP_AND_RUN.md            # Updated setup instructions
└── archived_android/            # Archived native Android implementation
```

## Cleanup Checklist

### Immediate (During Migration)
- [ ] Complete Maven → Gradle migration
- [ ] Remove `backend/target/` and Maven artifacts
- [ ] Move phase reports to `docs/`
- [ ] Clean temporary error/log files

### After Migration Complete
- [ ] Archive `android/` → `archived_android/`
- [ ] Archive `backend/src_backup/` → `archived_backend_src/`
- [ ] Update all scripts and documentation paths
- [ ] Verify no broken references in documentation

## Files to Preserve
- All `docs/*.md` - Keep all documentation
- `chimera_flutter/` - Primary mobile app
- `backend/src/` - Current backend implementation
- `scripts/` - Setup and utility scripts
- Configuration files (docker-compose.yml, etc.)

## Files Safe to Delete Eventually
- `backend/target/` (after Gradle migration)
- `backend/compile-errors.txt`
- `backend/delete-duplicates.txt`
- `backend/pom.xml` (after Gradle migration complete)

---
*Created during Flutter/Railway migration*
*Review and update as cleanup progresses*