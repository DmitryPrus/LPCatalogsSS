# LoadProductCatalog

The QA app is a serverless application built using AWS Lambda. Below are the steps to set up and run the app:

## Prerequisites

- AWS account with appropriate permissions
- Java Runtime Environment (JRE) installed
- Jar file for the app (attached)

## Setup Steps

1. Create an AWS Lambda function with any name of your choice.
2. Upload the jar file of the current app to the Lambda function.
3. Set runtime settings as com.tsfrm.loadtestproductcatalog.controller.LambdaController::handleRequest
4. Increase the timeout configuration of the Lambda function to 10 minutes. You can do this by going to Configuration ->
   General Configuration -> Edit -> Timeout.
5. Add the following data to the Lambda function's environment variables:
    - DB_URL: The URL of the database
    - DB_USER: The username for the database
    - DB_PASSWORD: The password for the database
    - DESTINATION_URL: The final endpoint where the data will be sent
    - OUTBOUND_THREADS_QUANTITY: The number of messages to be sent simultaneously
6. Run the Lambda function in the 'test' mode using the following example request:

```json
{
  "operators": 1,
  "locations": 3,
  "newProducts": 5,
  "productsToDelete": 0,
  "productsToUpdate": 4
}
```

## Initialization

The initialization of the Lambda function may take up to 5 minutes, which is normal. During this time, organizations are
extracted from the database based on the filter needed for vdi2.

## Request Validation

After initialization, the incoming request is validated. The app checks the number of locations and the number of
products to update or delete based on the information already available for all organizations. If the request exceeds
the available quantities, an exception is thrown with the description of the organization ID, location ID, and
quantities.

## Data Modification and Sending

If the request passes the validation, the app randomly selects organizations and modifies their products by updating or
creating new ones. Once all the data is prepared, it is sent to the configured URL simultaneously, considering the
configured quantity in OUTBOUND_THREADS_QUANTITY (default is 20).

## Source Code

The source code for the app can be found [here](https://github.com/DmitryPrus/LoadProductCatalog). If you need access to
the repository, please let us know.