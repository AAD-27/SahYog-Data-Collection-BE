# DC010 and DC011 API Reference

## Base URL

```text
http://localhost:8091/ms-data-collection/api/v1/resources
```

Every API accepts an application number (`A#######`) or case number (`D#######`) in `appOrCaseNum`. An application number is resolved internally to its corresponding DC case number.

## DC010 — Resource Summary

### Initialize

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/summary/initialize
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
  "pageId": "DC010",
  "resources": [
    {
      "resourceId": 1,
      "resourceType": "Bank Account",
      "resourceDetail": "State Bank of India - Savings Account (XXXX1234)",
      "resourceStartDate": "2026-08-01"
    }
  ],
  "found": true
}
```

When no resources exist, `resources` is empty and `found` is false.

### Next

Returns navigation to DC012 without changing resource records.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/summary/next
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC010"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC010",
  "destinationPageId": "DC012",
  "mode": null,
  "resourceId": null
}
```

### Previous

Returns navigation to DC009 without changing resource records.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/summary/previous
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC010"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC010",
  "destinationPageId": "DC009",
  "mode": null,
  "resourceId": null
}
```

### Add New Resource

Returns navigation to DC011 in add mode. It does not create a database record.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/summary/add-new
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC010"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC010",
  "destinationPageId": "DC011",
  "mode": "ADD",
  "resourceId": null
}
```

### Edit Resource

Validates that the resource belongs to the case and returns DC011 edit navigation. It does not update the record.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/summary/edit
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "resourceId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC010",
  "destinationPageId": "DC011",
  "mode": "EDIT",
  "resourceId": 1
}
```

### Delete Resource

Deletes the selected resource belonging to the supplied case.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/summary/delete
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "resourceId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "resourceId": 1,
  "status": "DELETED"
}
```

## DC011 — Resource Details

### Initialize Add Mode

Use a null `resourceId` to initialize an empty add form.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/details/initialize
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC011",
  "resourceId": null
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC011",
  "resource": null,
  "found": false
}
```

### Initialize Edit Mode

Use an existing `resourceId` to load its details.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/details/initialize
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC011",
  "resourceId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC011",
  "resource": {
    "resourceId": 1,
    "resourceType": "Bank Account",
    "resourceDetail": "State Bank of India - Savings Account (XXXX1234)",
    "resourceValue": 50000.00,
    "resourceStartDate": "2026-08-01",
    "resourceEndDate": ""
  },
  "found": true
}
```

### Next

A null `resourceId` inserts a new resource. A populated ID updates the existing resource. Next returns navigation to DC012.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/details/next
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC011",
  "resource": {
    "resourceId": null,
    "resourceType": "Bank Account",
    "resourceDetail": "State Bank of India - Savings Account (XXXX1234)",
    "resourceValue": 50000,
    "resourceStartDate": "2026-08-01",
    "resourceEndDate": ""
  }
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "resourceId": 1,
  "status": "SAVED",
  "currentPageId": "DC011",
  "destinationPageId": "DC012"
}
```

### Previous

Inserts or updates the resource and returns navigation to DC010.

```text
POST http://localhost:8091/ms-data-collection/api/v1/resources/details/previous
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC011",
  "resource": {
    "resourceId": 1,
    "resourceType": "Bank Account",
    "resourceDetail": "State Bank of India - Savings Account (XXXX1234)",
    "resourceValue": 50000,
    "resourceStartDate": "2026-08-01",
    "resourceEndDate": ""
  }
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "resourceId": 1,
  "status": "SAVED",
  "currentPageId": "DC011",
  "destinationPageId": "DC010"
}
```

## Validation Notes

- Dates must use `yyyy-MM-dd`.
- `resourceEndDate` may be empty for an ongoing resource.
- The end date cannot be earlier than the start date.
- `resourceValue` must be zero or greater.
- `resourceType`, `resourceDetail`, `resourceValue`, and `resourceStartDate` are required.
