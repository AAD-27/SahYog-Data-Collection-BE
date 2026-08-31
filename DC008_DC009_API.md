# DC008 and DC009 API Reference

## Base URL

```text
http://localhost:8091/ms-data-collection/api/v1/expenses
```

All APIs accept either an application number (`A#######`) or case number (`D#######`) in `appOrCaseNum`. Application numbers are resolved internally to the corresponding DC case number.

## DC008 — Expense Summary

### Initialize

```http
POST /summary/initialize
```

Full URL:

```text
http://localhost:8091/ms-data-collection/api/v1/expenses/summary/initialize
```

Request:

```json
{
  "appOrCaseNum": "D9000016"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC008",
  "expenses": [
    {
      "expenseId": 1,
      "expenseType": "Rent / Housing",
      "expenseStartDate": "2026-08-01",
      "expenseDetail": "Monthly rent for residential premises",
      "expenseFrequency": "Monthly"
    }
  ],
  "found": true
}
```

### Next

Returns navigation to DC010 without changing expense records.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/summary/next
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC008"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC008",
  "destinationPageId": "DC010",
  "mode": null,
  "expenseId": null
}
```

### Previous

Returns navigation to DC007 without changing expense records.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/summary/previous
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC008"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC008",
  "destinationPageId": "DC007",
  "mode": null,
  "expenseId": null
}
```

### Add New Expense

Returns navigation to DC009 in add mode. It does not create a database record.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/summary/add-new
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC008"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC008",
  "destinationPageId": "DC009",
  "mode": "ADD",
  "expenseId": null
}
```

### Edit Expense

Validates that the expense belongs to the case and returns DC009 edit navigation. It does not update the record.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/summary/edit
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "expenseId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC008",
  "destinationPageId": "DC009",
  "mode": "EDIT",
  "expenseId": 1
}
```

### Delete Expense

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/summary/delete
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "expenseId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "expenseId": 1,
  "status": "DELETED"
}
```

## DC009 — Expense Details

### Initialize Add Mode

Use a null `expenseId` to initialize an empty add form.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/details/initialize
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC009",
  "expenseId": null
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC009",
  "expense": null,
  "found": false
}
```

### Initialize Edit Mode

Use an existing `expenseId` to load its details.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/details/initialize
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC009",
  "expenseId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC009",
  "expense": {
    "expenseId": 1,
    "expenseType": "Rent / Housing",
    "expenseDetail": "Monthly rent for residential premises",
    "expenseFrequency": "Monthly",
    "expenseAmount": 15000.00,
    "expenseStartDate": "2026-08-01",
    "expenseEndDate": ""
  },
  "found": true
}
```

### Next

Inserts when `expenseId` is null and updates when it is populated. Returns navigation to DC010.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/details/next
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC009",
  "expense": {
    "expenseId": null,
    "expenseType": "Rent / Housing",
    "expenseDetail": "Monthly rent for residential premises",
    "expenseFrequency": "Monthly",
    "expenseAmount": 15000,
    "expenseStartDate": "2026-08-01",
    "expenseEndDate": ""
  }
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "expenseId": 1,
  "status": "SAVED",
  "currentPageId": "DC009",
  "destinationPageId": "DC010"
}
```

### Previous

Inserts or updates the expense and returns navigation to DC008.

```text
POST http://localhost:8091/ms-data-collection/api/v1/expenses/details/previous
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC009",
  "expense": {
    "expenseId": 1,
    "expenseType": "Rent / Housing",
    "expenseDetail": "Monthly rent for residential premises",
    "expenseFrequency": "Monthly",
    "expenseAmount": 15000,
    "expenseStartDate": "2026-08-01",
    "expenseEndDate": ""
  }
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "expenseId": 1,
  "status": "SAVED",
  "currentPageId": "DC009",
  "destinationPageId": "DC008"
}
```

## Validation Notes

- Dates must use `yyyy-MM-dd`.
- `expenseEndDate` may be an empty string for an ongoing expense.
- The end date cannot be before the start date.
- `expenseAmount` must be zero or greater.
- `expenseType`, `expenseDetail`, `expenseFrequency`, `expenseAmount`, and `expenseStartDate` are required.
