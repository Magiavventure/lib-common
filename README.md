# Magiavventure - Lib common

This common library handles errors with a general handler and request and response logs from all services.
It also inserts the transactionId header to aid log searching.

## Configuration

The properties exposed to configure this project are:

```properties
logging.level.app.magiavventure="string"                                                 # Logging level package magiavventure
magiavventure.lib.common.errors.errors-messages.{error-key}.code="string"                # The exception key error code
magiavventure.lib.common.errors.errors-messages.{error-key}.message="string"             # The exception key error message
magiavventure.lib.common.errors.errors-messages.{error-key}.description="string"         # The exception key error description
magiavventure.lib.common.errors.errors-messages.{error-key}.status=integer               # The exception key error status
```


## Error message map
The error message map is a basic system for return the specific message in the error response,
the configuration path at the moment is only for one branch **errors-messages**.
This branch setting a specific error message to **it.magiavventure.common.error.MagiavventureException**