## AWS Lambda Setup

1. Create a Lambda function on AWS.
2. Upload the .jar file (Java 17) to the Lambda function.
3. Set the handler as `'com.tsfrm.loadtestproductcatalog.controller.LambdaController::handleRequest'`.
4. Set the following environment variables:

```
DESTINATION_URL - destination endpoint for sending messages
HEADER_PROVIDER_NAME - name of available provider (default "April_01")
OUTBOUND_THREADS_QUANTITY - Number of messages sending at the same time (default 3)
LOCATIONS_PER_OPERATOR_MINIMUM - params which let us remove organizations containing less than N locations (default: 1)

BUCKET_NAME - name of storage bucket (default: orgsstorage)
BUCKET_ORGS_KEY - name of file which contains organizations in bucket (default: org-storage.json)
BUCKET_LOCATIONS_KEY - name of file which contains locations in bucket (default: location-storage.json)
BUCKET_PRODUCTS_KEY - name of file which contains products in bucket (default: product-storage.json)

USER_KEY - AWS user's access key (probably create a new user for it)
USER_PASS - AWS user's ****
```

## Testing the Lambda Function

5. Open the Lambda console and use the following request payload:

```json
{
  "operators": 1,
  "locations": 3,
  "newProducts": 5,
  "productsToDelete": 0,
  "productsToUpdate": 4
}
```

- `operators`: Number of operators to extract for update.
- `locations`: Number of locations per operator to handle.
- `newProducts`: Number of products to be newly created for each location of every chosen organization.
- `productsToDelete`: Number of products to be deleted for each location of every chosen organization.
- `productsToUpdate`: Number of products to be updated for each location of every chosen organization.

Note: If the requested number of organizations, locations, or products for update/delete exceeds the available quantity, an error message will be displayed, and the handle operation will not be successful. On successful processing, you will see the log message `'All messages have been processed successfully'`. Additionally, check the CloudWatch logs for messages like `'Thread 1 finished with status: 200'` to ensure that the destination URL is available and all messages were successfully sent.

## Logs and Monitoring

After sending the message, you can check the logs in the following stack of Lambdas and SQS in the 365 AWS environment:

- `staging-vdiapi-full-april_01-mmsproducts.fifo`
- `arn:aws:lambda:us-east-2:765545258450:function:staging-vdiapi-message-split`
- `staging-vdiapi-messages-april_01-mmsmarkets.fifo`
- `staging-vdiapi-message-import`

Look for the log response in `staging-vdiapi-message-import`, which contains `'VdiProductsImportService - FinishImportLog(message=product_import_finish)'`. This log entry indicates that the message was successfully handled by the 365 Lambdas.