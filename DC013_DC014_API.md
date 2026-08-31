# DC013 and DC014 Disability API Reference

## Base URL

```text
http://localhost:8091/ms-data-collection/api/v1/disabilities
```

All endpoints accept either `A#######` or `D#######` through `appOrCaseNum`.
Application numbers are resolved internally to their corresponding DC case number.

## DC013 — Disability Summary

### Initialize

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/summary/initialize
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
  "pageId": "DC013",
  "disabilities": [
    {
      "disabilityId": 1,
      "personId": 101,
      "personName": "Ramesh Sharma",
      "age": 42,
      "disabilityTypes": [
        "Blindness",
        "Locomotor Disability",
        "Other Disability"
      ],
      "otherDisabilityDetails": "Partial neurological mobility impairment"
    }
  ],
  "found": true
}
```

When no records exist, `disabilities` is empty and `found` is false.

### Next

Returns navigation to DC015 without changing disability records.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/summary/next
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC013"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC013",
  "destinationPageId": "DC015",
  "mode": null,
  "disabilityId": null
}
```

### Previous

Returns navigation to DC012 without changing disability records.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/summary/previous
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC013"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC013",
  "destinationPageId": "DC012",
  "mode": null,
  "disabilityId": null
}
```

### Add New Disability

Returns navigation to DC014 in add mode. It does not create a database record.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/summary/add-new
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC013"
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC013",
  "destinationPageId": "DC014",
  "mode": "ADD",
  "disabilityId": null
}
```

### Edit Disability

Validates the disability record and returns DC014 edit navigation. It does not update the record.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/summary/edit
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "disabilityId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "currentPageId": "DC013",
  "destinationPageId": "DC014",
  "mode": "EDIT",
  "disabilityId": 1
}
```

### Delete Disability

Deletes the selected disability record belonging to the supplied case.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/summary/delete
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "disabilityId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "disabilityId": 1,
  "status": "DELETED"
}
```

## DC014 — Disability Details

### Initialize Add Mode

Use a null `disabilityId` to initialize an empty add form.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/details/initialize
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC014",
  "disabilityId": null
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC014",
  "disability": null,
  "found": false
}
```

### Initialize Edit Mode

Use an existing `disabilityId` to load its details.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/details/initialize
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC014",
  "disabilityId": 1
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC014",
  "disability": {
    "disabilityId": 1,
    "personId": 101,
    "personName": "Ramesh Sharma",
    "disabilityTypes": [
      "Blindness",
      "Locomotor Disability",
      "Other Disability"
    ],
    "disabilityType": "Blindness, Locomotor Disability, Other Disability",
    "otherDisabilityDetails": "Partial neurological mobility impairment",
    "proofOfDisability": "UDID Card",
    "disabilityStartDate": "1984-05-10",
    "sameAsBirthDate": true
  },
  "found": true
}
```

### Next

A null `disabilityId` inserts a record. A populated ID updates the existing record. Next returns navigation to DC015.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/details/next
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC014",
  "disability": {
    "disabilityId": null,
    "personId": 101,
    "personName": "Ramesh Sharma",
    "disabilityTypes": [
      "Blindness",
      "Locomotor Disability",
      "Other Disability"
    ],
    "disabilityType": "Blindness, Locomotor Disability, Other Disability",
    "otherDisabilityDetails": "Partial neurological mobility impairment",
    "proofOfDisability": "UDID Card",
    "disabilityStartDate": "1984-05-10",
    "sameAsBirthDate": false
  }
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "disabilityId": 1,
  "status": "SAVED",
  "currentPageId": "DC014",
  "destinationPageId": "DC015"
}
```

### Previous

Inserts or updates the disability and returns navigation to DC013.

```text
POST http://localhost:8091/ms-data-collection/api/v1/disabilities/details/previous
```

Request:

```json
{
  "appOrCaseNum": "D9000016",
  "pageId": "DC014",
  "disability": {
    "disabilityId": 1,
    "personId": 101,
    "personName": "Ramesh Sharma",
    "disabilityTypes": [
      "Blindness",
      "Locomotor Disability",
      "Other Disability"
    ],
    "disabilityType": "Blindness, Locomotor Disability, Other Disability",
    "otherDisabilityDetails": "Partial neurological mobility impairment",
    "proofOfDisability": "UDID Card",
    "disabilityStartDate": "1984-05-10",
    "sameAsBirthDate": true
  }
}
```

Response:

```json
{
  "appOrCaseNum": "D9000016",
  "disabilityId": 1,
  "status": "SAVED",
  "currentPageId": "DC014",
  "destinationPageId": "DC013"
}
```

## Validation Notes

- `personId` must identify a person belonging to the supplied case.
- At least one value in `disabilityTypes` is required.
- `proofOfDisability` is required.
- Selecting `Other Disability` requires `otherDisabilityDetails`.
- Dates must use `yyyy-MM-dd`.
- When `sameAsBirthDate` is true, the backend uses the selected person's date of birth as `disabilityStartDate`.
