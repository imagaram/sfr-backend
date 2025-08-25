# SFR API Compatibility Layer

This module adds a minimal compatibility layer to expose selected OpenAPI endpoints under `/api/v1/sfr` while delegating business logic to existing services.

Covered endpoints (Phase 1):
- GET `/api/v1/sfr/balance/{user_id}`
- GET `/api/v1/sfr/balance/{user_id}/history?page&limit`
- POST `/api/v1/sfr/transfer`

Notes:
- Numeric amounts are formatted as strings with 8 decimal places, per spec.
- Sender user is resolved from `from_user_id` if provided, otherwise from Authentication.
- Error responses are simplified for now.
