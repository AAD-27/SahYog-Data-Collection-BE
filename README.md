# SahYog Data Collection Backend

Spring Boot 3 / Java 17 service for initiating data collection from an application-registration record.

## APIs

- `POST /ms-data-collection/api/v1/initiate-dc/lookup` returns first name, last name, and age. `A#######` reads AR tables; `D#######` reads DC tables.
- `POST /ms-data-collection/api/v1/initiate-dc/continue` converts `A#######` to `D#######`, copies matching rows from the four `AR_*` tables to the four `DC_*` tables, and sets the DC application status to `IN_PROGRESS`.

All DC endpoints use the `appOrCaseNum` JSON key and accept either `A#######` or `D#######`.
Calling `/continue` for an already initiated application returns HTTP 512 with the existing DC case number.
When `/lookup` finds no record, it returns HTTP 512 with `No records found for {appOrCaseNum}`.

The POST operation is transactional. Configure `MYSQL_PASSWORD` if the local development password is not `password`.

### DC002 — Applicant Details

- `POST /ms-data-collection/api/v1/applicant-details/initialize` loads applicant details from `DC_APPLICATION`.
- `POST /ms-data-collection/api/v1/applicant-details/next` validates and saves the screen, then returns `DC003`.
- `POST /ms-data-collection/api/v1/applicant-details/previous` validates and saves the screen, then returns `DC001`.

### DC003 — Address Details

- `POST /ms-data-collection/api/v1/address-details/initialize` loads permanent and temporary addresses.
- `POST /ms-data-collection/api/v1/address-details/next` saves both addresses and returns `DC004`.
- `POST /ms-data-collection/api/v1/address-details/previous` saves both addresses and returns `DC002`.

When `sameAsPermanent` is true, the permanent address is persisted as the temporary address too.

### DC004 — Person Information

- `POST /ms-data-collection/api/v1/person-information/initialize` loads primary-person details.
- `POST /ms-data-collection/api/v1/person-information/next` saves person information and returns `DC005`.
- `POST /ms-data-collection/api/v1/person-information/previous` saves person information and returns `DC003`.

### DC005 — Program Details

- `POST /ms-data-collection/api/v1/program-details/initialize` loads selected program codes.
- `POST /ms-data-collection/api/v1/program-details/next` saves program selections and returns `DC006`.
- `POST /ms-data-collection/api/v1/program-details/previous` saves program selections and returns `DC004`.

### DC006 — Income Summary

- `POST /ms-data-collection/api/v1/income-summary/initialize` reads the income grid.
- `POST /ms-data-collection/api/v1/income-summary/next` returns `DC008` without writing.
- `POST /ms-data-collection/api/v1/income-summary/previous` returns `DC005` without writing.
- `POST /ms-data-collection/api/v1/income-summary/add-new` returns DC007 add-mode navigation.
- `POST /ms-data-collection/api/v1/income-summary/edit` validates the row and returns DC007 edit-mode navigation.
- `POST /ms-data-collection/api/v1/income-summary/delete` deletes the selected income row.

Create the table with `src/main/resources/sql/create_dc_income.sql` before calling income APIs.

### DC007 — Income Details

- `POST /ms-data-collection/api/v1/income-details/initialize` loads a row by `incomeId`, or returns an empty add form when it is null.
- `POST /ms-data-collection/api/v1/income-details/next` inserts/updates income and returns `DC008`.
- `POST /ms-data-collection/api/v1/income-details/previous` inserts/updates income and returns `DC006`.

For an already-created basic `DC_INCOME` table, run `sql/alter_dc_income_for_details.sql`.

### DC008/DC009 — Expenses

One controller exposes `/api/v1/expenses/summary/{action}` for DC008 and
`/api/v1/expenses/details/{action}` for DC009. Summary supports initialize, next,
previous, add-new, edit, and delete. Details supports initialize, next, and previous.
Create the table with `sql/create_dc_expenses.sql`.

### DC010/DC011 — Resources

One controller exposes `/api/v1/resources/summary/{action}` for DC010 and
`/api/v1/resources/details/{action}` for DC011. Summary supports initialize, next,
previous, add-new, edit, and delete. Details supports initialize, next, and previous.
Create the table with `sql/create_dc_resources.sql`.

### DC013/DC014 — Disabilities

One controller exposes `/api/v1/disabilities/summary/{action}` for DC013 and
`/api/v1/disabilities/details/{action}` for DC014. Summary supports initialize,
next, previous, add-new, edit, and delete. Details supports initialize, next, and
previous. Create the table with `sql/create_dc_disability.sql`.
