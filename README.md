## Test project provides OAuth2 with [Canvas](https://canvas.instructure.com/) and allows to fetch data, save into local DB, log the sync process.

### Technology stack

Java, Spring Boot (Web, Security, OAuth2, WebFlux, AOP), Liquibase, JDBC, QueryDSL, PostgreSQL, Mockito, Lombok, Gradle

### Prerequisites (tested on this environment):

JVM - 17.0.6
Gradle - 8.5

Note: if you run project from console (not from IDEA), check `echo $JAVA_HOME` returns version `17` and `gradle --version` uses JVM `17`

### How to run:

1. Clone repository
2. Get your CLIEND_ID (Canvas -> Admin -> Developer keys -> Details) and CLIENT_SECRET (Show key)
3. Put these variables into `.env` (or just put it directly into `bootstrap.yaml` file), add host variable
4. Run `docker-compose up -d` from root directory to create a local database
5. Run `./gradlew bootRun` to start the app (in this case all .env will be attached to app automatically), 
   or just run "main" method in `SyncApplication` (if you put variables directly to bootstrap.yaml)
6. Open `http://localhost:8077/` in your browser
7. Click on "Oauth setup", redirected to Canvas login page, put your username/password, redirect back to home app page
8. Click "Start sync", wait till the end, check the fetched data in your local database `jdbc:postgresql://localhost:5437/canvas`

### Details of implementation:

- OAuth2 functionality does NOT store any auth data in local database for security reasons
- The access_token is available for 1 hour, refresh_token is not used
- You can't start sync without getting the access token, the 401 error will be displayed
- All data is fetched async via WebFlux non-blocking the Canvas API
- First the app fetches all `accounts`, save them and only after that starts to fetch `courses` related to `accounts`
- Both entities can be fetched via batches, this variable can be set in `bootstrap.yaml`
- Saving of `accounts` and `courses` are done sequentially, but storing of `courses` themselves done in parallel
- If there are any exceptions during the sync, it will not terminate the process, error will be skipped and logged
- Both entities `accounts` and `courses` store `last_sync_at` field, it responsible for the last create/update of entity
- All `last_sync_at` is stored in database for UTC time zone no matter how it was set (by annotation or explicitly jdbc)
- Fetching data and setting `last_sync_at` for `account` done via JPA and annotation, for `courses` via JDBC query (for performance)
- The consistency and equality of entities based on `id` field
- If any data will be deleted from Canvas API, it will NOT be deleted from local database after next sync
- Added @Audit aop annotation to log exceptions, method start, end execution time

How to check that `accounts` are fetching and storing really in parallel: there are added `Threas.sleep()` in `*StoreImpl` classes, 
and always the total execution time of `sync` method ~ 'time of accounts fetching + time of ONE courses batch fetching'


https://github.com/evtimo/canvas-oauth2-sync-test/assets/22182922/20fcb049-8a62-47ba-b646-54aae731a876

