# FileStoreAPI

A minimal Spring Boot API to upload, download, and delete files in AWS S3 or Azure Blob Storage.

## Prerequisites
- Java 21
- Maven (or use the included Maven Wrapper `./mvnw`)
- Valid AWS and/or Azure credentials

## 1) Environment (.env)
Create a file named `.env` in the project root with the following keys:

```
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=us-east-1
AWS_BUCKET_NAME=your_s3_bucket_name

AZURE_STORAGE_ACCOUNT_NAME=your_azure_storage_account
AZURE_STORAGE_ACCOUNT_KEY=your_azure_storage_key
AZURE_STORAGE_ACCOUNT_ENDPOINT=https://your_azure_storage_account.blob.core.windows.net
AZURE_BLOB_CONTAINER_NAME=your_azure_blob_container
```

Note: The app auto-loads `.env` via `spring.config.import=optional:file:.env[.properties]`.

## 2) Run the API
Using Maven Wrapper (recommended):

```
./mvnw spring-boot:run
```

Or with Maven installed:

```
mvn spring-boot:run
```

The API runs by default at `http://localhost:8080`.

## 3) Endpoints
Base path: `/api/files`

- POST `/create-file` — Upload
- GET `/download-file` — Download
- DELETE `/delete-file` — Delete

Common query/form parameters:
- `FileName`: name of the file (e.g., `example.txt`)
- `FilePath`: folder/prefix, include trailing slash if needed (e.g., `docs/`)
- `ConnectionName`: one of `AWS_S3` or `AZURE_BLOB`
- `FileContents`: base64-encoded file contents (required only for upload)

### Minimal cURL examples
Upload to S3:
```
curl -X POST "http://localhost:8080/api/files/create-file" \
  -d "FileContents=$(base64 < path/to/file | tr -d '\n')" \
  -d "FileName=example.txt" \
  -d "FilePath=folder/" \
  -d "ConnectionName=AWS_S3"
```

Download from Azure Blob:
```
curl -G "http://localhost:8080/api/files/download-file" \
  --data-urlencode "FileName=example.txt" \
  --data-urlencode "FilePath=folder/" \
  --data-urlencode "ConnectionName=AZURE_BLOB" \
  -o example.txt
```

Delete from S3:
```
curl -X DELETE "http://localhost:8080/api/files/delete-file?FileName=example.txt&FilePath=folder/&ConnectionName=AWS_S3"
```
