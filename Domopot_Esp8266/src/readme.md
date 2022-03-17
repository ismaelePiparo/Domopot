## Realtime Database

See [RTDB examples](/examples/RTDB) for complete usages.



### Read Data

Data at a specific node in Firebase RTDB can be read through these get functions.

The functions included `get`, `getInt`, `getFloat`, `getDouble`, `getBool`, `getString`, `getJSON`, `getArray`, `getBlob`, `getFile`.


These functions return boolean value indicates the success of the operation which will be `true` if all of the following conditions were met.

* Server returns HTTP status code 200

* The data types matched between request and response.


For generic get, use Firebase.RTDB.get(&fbdo, \<path\>).

And check its type with fbdo.dataType() or fbdo.dataTypeEnum() and cast the value from it e.g. fbdo.to\<int\>(), fbdo.to\<std::string\>().

The data type of returning payload can be determined by `fbdo.dataType()` which returns String or `fbdo.dataTypeEnum()` returns enum value.

The String of type returns from `fbdo.dataType()` can be string, boolean, int, float, double, json, array, blob, file and null.

The enum value type, fb_esp_rtdb_data_type returns from `fbdo.dataTypeEnum()` can be fb_esp_rtdb_data_type_null (1), fb_esp_rtdb_data_type_integer, fb_esp_rtdb_data_type_float, fb_esp_rtdb_data_type_double, fb_esp_rtdb_data_type_boolean, fb_esp_rtdb_data_type_string, fb_esp_rtdb_data_type_json, fb_esp_rtdb_data_type_array, fb_esp_rtdb_data_type_blob, and fb_esp_rtdb_data_type_file (10)



The database data's payload (response) can be read or access through the casting value from FirebaseData object with to\<type\>() functions (since v2.4.0).

* `String s = fbdo.to<String>();`

* `std::string _s = fbdo.to<std::string>();`

* `const char *str = fbdo.to<const char *>();`

* `bool b = fbdo.to<bool>();`

* `int16_t _i = fbdo.to<int16_t>();`

* `int i = fbdo.to<int>();`

* `double d = fbdo.to<double>();`

* `float f = fbdo.to<float>();`

* `FirebaseJson *json = fbdo.to<FirebaseJson *>();` or

* `FirebaseJson &json = fbdo.to<FirebaseJson>();`

* `FirebaseJsonArray *arr = fbdo.to<FirebaseJsonArray *>();` or

* `FirebaseJsonArray &arr = fbdo.to<FirebaseJsonArray>();`

* `std::vector<uint8_t> *blob = fbdo.to<std::vector<uint8_t> *>();`

* `File file = fbdo.to<File>();`

Or through the legacy methods

* `int i = fbdo.intData();`

* `float f = fbdo.floatData();`

* `double d = fbdo.doubleData();`

* `bool b = fbdo.boolData();`

* `String s = fbdo.stringData();`

* `String js = fbdo.jsonString();`

* `FirebaseJson &json = fbdo.jsonObject();`

* `FirebaseJson *jsonPtr = fbdo.jsonObjectPtr();`

* `FirebaseJsonArray &arr = fbdo.jsonArray();` 

* `FirebaseJsonArray *arrPtr = fbdo.jsonArrayPtr();`

* `std::vector<uint8_t> blob = fbdo.blobData();`

 * `File file = fbdo.fileStream();`



Read the data which its type does not match the data type in the database from above functions will return empty (string, object or array).

BLOB and file stream data are stored as special base64 encoded string which are only supported and implemented by this library.

The encoded base64 string will be prefixed with some header string ("file,base64," and "blob,base64,") for data type manipulation. 



The following example showed how to read integer value from node "/test/int".


```cpp
  if (Firebase.RTDB.getInt(&fbdo, "/test/int")) {

    if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_integer) {
      Serial.println(fbdo.to<int>());
    }

  } else {
    Serial.println(fbdo.errorReason());
  }
```



### Store Data

To store data at a specific node in Firebase RTDB, use these set functions.

The function included `set`, `setInt`, `setFloat`, `setDouble`, `setBool`, `setString`, `setJSON`, `setArray`, `setBlob` and `setFile`. 

For faster sending data, non-waits or async mode functions are available e.g. `setAsync`, `setIntAsync`, `setFloatAsync`, `setDoubleAsync`, `setBoolAsync`, `setStringAsync`, `setJSONAsync`, `setArrayAsync`, `setBlobAsync` and `setFileAsync`. 

For generic set, use Firebase.RTDB.set(&fbdo, \<path\>, \<any variable or value\>).

These async functions will ignore the server responses.


The above functions return boolean value indicates the success of the operation which will be `true` if all of the following conditions matched.

* Server returns HTTP status code 200

* The data types matched between request and response.


Only setBlob and setFile functions that make a silent request to Firebase server, thus no payload response returned. 

The **priority**, virtual node **".priority"** of each database node can be set through Firebase's set functions.

The priority value can be used in a query or filtering the children's data under a defined node.

Priority option was removed from File and Blob functions since v2.4.0.

**ETag** (unique identifier value) assigned to Firebase's set functions is used as conditional checking.

If defined Etag is not matched the defined path's ETag, the set operation will fail with result **412 Precondition Failed**.

ETag at any node can be read through `Firebase.RTDB.getETag`.  ETag value changed upon the data was set or delete.

The server's **Timestamp** can be stored in the database through `Firebase.RTDB.setTimestamp`. 

The returned **Timestamp** value can get from `fbdo.fbdo.to<int>()`. 

The file systems for flash and sd memory can be changed in [**FirebaseFS.h**](/src/FirebaseFS.h).



The following example showed how to store file data to flash memory at node "/test/file_data".


```cpp

if (Firebase.RTDB.getFile(&fbdo, mem_storage_type_flash, "/test/file_data", "/test.txt"))
{
  //The file systems for flash and SD/SDMMC can be changed in FirebaseFS.h.
  File file = DEFAULT_FLASH_FS.open("/test.txt", "r");

  while (file.available())
  {     
    Serial.print(file.read(), HEX);     
  }    
  file.close();
  Serial.println();

} else {
  Serial.println(fbdo.fileTransferError());
}
```


### Append Data

To append new data to a specific node in Firebase RTDB, use these push functions.

The function included `push`, `pushInt`, `pushFloat`, `pushDouble`, `pushBool`, `pushString`, `pushJSON`, `pushArray`, `pushBlob`, and `pushFile`.

For faster sending data, non-waits or async mode functions are available e.g. `pushAsync`, `pushIntAsync`, `pushFloatAsync`, `pushDoubleAsync`, `pushBoolAsync`, `pushStringAsync`, `pushJSONAsync`, `pushArrayAsync`, `pushBlobAsync` and `pushFileAsync`. 

These functions return boolean value indicates the success of the operation.

The **unique key** of a new appended node can be determined from `fbdo.pushName()`.

As set functions, the Firebase's push functions support **priority**.

**ETag** was not available after push unless read the **ETag** at that new appended unique key later with `Firebase.RTDB.getETag`.

The server's **Timestamp** can be appended in the database through `Firebase.RTDB.pushTimestamp`.

The unique key of Timestamp can be determined after Timestamp was appended.



The following example showed how to append new data (using FirebaseJson object) to node "/test/append.


```cpp

FirebaseJson json;
FirebaseJson json2;

json2.add("child_of_002", 123.456);
json.add("parent_001", "parent 001 text");
json.add("parent 002", json2);

if (Firebase.RTDB.pushJSON(&fbdo, "/test/append", &json)) {

  Serial.println(fbdo.dataPath());

  Serial.println(fbdo.pushName());

  Serial.println(fbdo.dataPath() + "/"+ fbdo.pushName());

} else {
  Serial.println(fbdo.errorReason());
}
```



### Patch Data

Firebase's update functions used to patch or update new or existing data at the defined node.

These functions, `updateNode` and `updateNodeSilent` are available and work with JSON object (FirebaseJson object only).

For faster sending data, non-waits or async mode functions are available e.g. `updateNodeAsync`, and `updateNodeSilentAsync`.

If any key name provided at a defined node in JSON object has not existed, a new key will be created.

The server returns JSON data payload which was successfully patched.

Return of large JSON payload will cost the network data, alternative function `updateNodeSilent` or `updateNodeSilentAsync` should be used to save the network data.



The following example showed how to patch data at "/test".


```cpp

FirebaseJson updateData;
FirebaseJson json;
json.add("_data2","_value2");
updateData.add("data1","value1");
updateData.add("data2", json);

if (Firebase.RTDB.updateNode(&fbdo, "/test/update", &updateData)) {

  Serial.println(fbdo.dataPath());

  Serial.println(fbdo.dataType());

  Serial.println(fbdo.jsonString()); 

} else {
  Serial.println(fbdo.errorReason());
}
```


### Delete Data


The following example showed how to delete data and its children at node "/test/append"

```cpp
Firebase.RTDB.deleteNode(&fbdo, "/test/append");
```



### Filtering Data

To filter or query the data, the following query parameters are available through the QueryFilter class.

These parameters are `orderBy`, `limitToFirst`, `limitToLast`, `startAt`, `endAt`, and `equalTo`.

To filter data, parameter `orderBy` should be assigned.

Use **"$key"** as the `orderBy` parameter if the key of child nodes was used for the query.

Use **"$value"** as the `orderBy` parameter if the value of child nodes was used for the query.

Use **key (or full path) of child nodes** as the `orderBy` parameter if all values of the specific key were used for the query.

Use **"$priority"** as `orderBy` parameter if child nodes's **"priority"** was used for query.



The above `orderBy` parameter can be combined with the following parameters for limited and ranged the queries.

`QueryFilter.limitToFirst` -  The total children (number) to filter from the first child.

`QueryFilter.limitToLast` -   The total last children (number) to filter. 

`QueryFilter.startAt` -       Starting value of range (number or string) of query upon orderBy param.

`QueryFilter.endAt` -         Ending value of range (number or string) of query upon orderBy param.

`QueryFilter.equalTo` -       Value (number or string) matches the orderBy param



The following example showed how to use queries parameter in QueryFilter class to filter the data at node "/test/data"

```cpp
//Assume that children that have key "sensor" are under "/test/data"

//Instantiate the QueryFilter class
QueryFilter query;

//Build query using specified child node key "sensor" under "/test/data"
query.orderBy("sensor");

//Query any child that its value begins with 2 (number), assumed that its data type is float or integer
query.startAt(2);

//Query any child that its value ends with 8 (number), assumed that its data type is float or integer
query.endAt(8);

//Limit the maximum query result to return only the last 5 nodes
query.limitToLast(5);


if (Firebase.RTDB.getJSON(&fbdo, "/test/data", &query))
{
  //Success, then try to read the JSON payload value
  Serial.println(fbdo.jsonString());
}
else
{
  //Failed to get JSON data at defined node, print out the error reason
  Serial.println(fbdo.errorReason());
}

//Clear all query parameters
query.clear();
```


### Server Data Changes Listener with Server-Sent Events or HTTP Streaming

This library uses HTTP GET request with `text/event-stream` header to make [**HTTP streaming**](https://en.wikipedia.org/wiki/Server-sent_events) connection.

The Firebase's functions that involved the stream operations are `beginStream`, `beginMultiPathStream`, 
`setStreamCallback`, `setMultiPathStreamCallback` and/or `readStream`.

Function `beginStream` is to subscribe to the data changes at a defined node.

Function `beginMultiPathStream` is to subscribe to the data changes at a defined parent node path with multiple child nodes value parsing and works with setMultiPathStreamCallback.

Function `setStreamCallback` is to assign the callback function that accepts the **FirebaseStream** class as parameter.

Function `setMultiPathStreamCallback` is to assign the callback function that accepts the **MultiPathStream** class as parameter.


The **FirebaseStream** contains stream's event/data payloadd and interface function calls are similar to `FirebaseData` object.

The **MultiPathStream** contains stream's event/data payload for various child nodes.


To polling the stream's event/data payload manually, use `readStream` in loop().

Function `readStream` used in the loop() task to continuously read the stream's event and data.

Since polling the stream's event/data payload with `readStream`, use `fbdo.streamAvailable` to check if stream event/data payoad is available.

Function `fbdo.streamAvailable` returned true when new stream's event/data payload was available. 

When new stream payload was available, its data and event can be accessed from `FirebaseData` object functions.

Function `endStream` ends the stream operation.


Note that, when using the shared `FirebaseData` object for stream and CRUD usages(normal operation to create,read, update and delete data), the stream connection will be interrupted (closed) to connect in other HTTP mode, the stream will be resumed (open) after the CRUD usages.

For the above case, you need to provide the idle time for `FirebaseData` object to established the streaming connection and received the stream payload. The changes on the server at the streaming node path during the stream interruption will be missed.

To avoid this sitation, don't share the usage of stream's `FirebaseData` object, another `FirebaseData` object should be used.

In addition, delay function used in the same loop of `readStream()` will defer the streaming, the server data changes may be missed.

Keep in mind that `FirebaseData` object will create the SSL client inside of HTTPS data transaction and uses large memory.



The following example showed how to subscribe to the data changes at node "/test/data" with a callback function.

```cpp

//In setup(), set the stream callback function to handle data
//streamCallback is the function that called when database data changes or updates occurred
//streamTimeoutCallback is the function that called when the connection between the server 
//and client was timeout during HTTP stream

Firebase.RTDB.setStreamCallback(&fbdo, streamCallback, streamTimeoutCallback);

//In setup(), set the streaming path to "/test/data" and begin stream connection

if (!Firebase.RTDB.beginStream(&fbdo, "/test/data"))
{
  //Could not begin stream connection, then print out the error detail
  Serial.println(fbdo.errorReason());
}

  
  //Global function that handles stream data
void streamCallback(FirebaseStream data)
{

  //Print out all information

  Serial.println("Stream Data...");
  Serial.println(data.streamPath());
  Serial.println(data.dataPath());
  Serial.println(data.dataType());

  //Print out the value
  //Stream data can be many types which can be determined from function dataType

  if (data.dataTypeEnum() == fb_esp_rtdb_data_type_integer)
      Serial.println(data.to<int>());
  else if (data.dataTypeEnum() == fb_esp_rtdb_data_type_float)
      Serial.println(data.to<float>(), 5);
  else if (data.dataTypeEnum() == fb_esp_rtdb_data_type_double)
      printf("%.9lf\n", data.to<double>());
  else if (data.dataTypeEnum() == fb_esp_rtdb_data_type_boolean)
      Serial.println(data.to<bool>()? "true" : "false");
  else if (data.dataTypeEnum() == fb_esp_rtdb_data_type_string)
      Serial.println(data.to<String>());
  else if (data.dataTypeEnum() == fb_esp_rtdb_data_type_json)
  {
      FirebaseJson *json = data.to<FirebaseJson *>();
      Serial.println(json->raw());
  }
  else if (data.dataTypeEnum() == fb_esp_rtdb_data_type_array)
  {
      FirebaseJsonArray *arr = data.to<FirebaseJsonArray *>();
      Serial.println(arr->raw());
  }
     

}

//Global function that notifies when stream connection lost
//The library will resume the stream connection automatically
void streamTimeoutCallback(bool timeout)
{
  if(timeout){
    //Stream timeout occurred
    Serial.println("Stream timeout, resume streaming...");
  }  
}

```



For multiple paths streaming, see the MultiPath example.


The following example showed how to subscribe to the data changes at "/test/data" and polling the stream manually.

```cpp
//In setup(), set the streaming path to "/test/data" and begin stream connection
if (!Firebase.RTDB.beginStream(&fbdo, "/test/data"))
{
  Serial.println(fbdo.errorReason());
}

//Place this in loop()
if (!Firebase.RTDB.readStream(&fbdo))
{
  Serial.println(fbdo.errorReason());
}

if (fbdo.streamTimeout())
{
  Serial.println("Stream timeout, resume streaming...");
  Serial.println();
}

if (fbdo.streamAvailable())
{
  if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_integer)
    Serial.println(fbdo.to<int>());
  else if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_float)
    Serial.println(fbdo.to<float>(), 5);
  else if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_double)
    printf("%.9lf\n", fbdo.to<double>());
  else if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_boolean)
    Serial.println(fbdo.to<bool>() ? "true" : "false");
  else if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_string)
    Serial.println(fbdo.to<String>());
  else if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_json)
  {
      FirebaseJson *json = fbdo.to<FirebaseJson *>();
      Serial.println(json->raw());
  }
  else if (fbdo.dataTypeEnum() == fb_esp_rtdb_data_type_array)
  {
      FirebaseJsonArray *arr = fbdo.to<FirebaseJsonArray *>();
      Serial.println(arr->raw());
  }
}
```


### Backup and Restore Data


This library allows data backup and restores at a defined path.

The backup file will store in SD/SDMMC card or flash memory.

The file systems for flash and SD memory can be changed via [**FirebaseFS.h**](/src/FirebaseFS.h).

Due to SD library used, only 8.3 DOS format file name supported.

The maximum 8 characters for a file name and 3 characters for file extension.

The database restoration returned completed status only when Firebase server successfully updates the data. 

Any failed operation will not affect the database (no updates or changes).

The following example showed how to backup all database data at "/" and restore.

```cpp
 String backupFileName;

 if (!Firebase.RTDB.backup(&fbdo, mem_storage_type_sd, "/", "/backup.txt"))
 {
   Serial.println(fbdo.errorReason());
 }
 else
 {
   Serial.println(fbdo.getBackupFilename());
   Serial.println(fbdo.getBackupFileSize());
   backupFileName = fbdo.getBackupFilename();
  }


  //Begin restore backed dup data back to database
  if (!Firebase.RTDB.restore(&fbdo, mem_storage_type_sd, "/", backupFileName.c_str()))
  {
    Serial.println(fbdo.errorReason());
  }
  else
  {
    Serial.println(fbdo.getBackupFilename());
  }
```


### Database Error Handling

When read store, append and update operations were failed due to buffer overflow and network problems.

These operations can retry and queued after the retry amount was reached the maximum retry set in function `setMaxRetry`.

```cpp
//set maximum retry amount to 3
 Firebase.RTDB.setMaxRetry(&fbdo, 3);
```

The function `setMaxErrorQueue` limits the maximum queues in Error Queue collection.

The full of queue collection can be checked through function `isErrorQueueFull`.


```cpp
 //set maximum queues to 10
 Firebase.RTDB.setMaxErrorQueue(&fbdo, 10);

 //determine whether Error Queue collection is full or not
 Firebase.RTDB.isErrorQueueFull(&fbdo);
```

This library provides two approaches to run or process Error Queues with two functions. 

* `beginAutoRunErrorQueue`
* `processErrorQueue`

The function `beginAutoRunErrorQueue` will run or process queues automatically and can be called once. 

While function `processErrorQueue` will run or process queues and should call inside the loop().

With function `beginAutoRunErrorQueue`, you can assigned callback function that accept **QueueInfo** object as parameter.

Which contains all information about being processed queue, number of remaining queues and Error Queue collection status.

Otherwise, Error Queues can be tracked manually with the following functions.

Function `getErrorQueueID` will return the unsigned integer presents the id of the queue which will keep using later.

Use `getErrorQueueID` and `isErrorQueueExisted` to check whether this queue id is still existed or not. 

If Error Queue ID does not exist in Error Queues collection, that queue is already done.

The following example showed how to run Error Queues automatically and track the status with the callback function.

```cpp

//In setup()

//Set the maximum Firebase Error Queues in collection (0 - 255).
//Firebase read/store operation causes by network problems and buffer overflow will be 
//added to Firebase Error Queues collection.
Firebase.RTDB.setMaxErrorQueue(&fbdo, 10);

//Begin to run Error Queues in Error Queue collection  
Firebase.RTDB.beginAutoRunErrorQueue(&fbdo, callback);


//Use to stop the auto run queues
//Firebase.endAutoRunErrorQueue(fbdo);

void errorQueueCallback (QueueInfo queueinfo){

  if (queueinfo.isQueueFull())
  {
    Serial.println("Queue is full");
  }

  Serial.print("Remaining queues: ");
  Serial.println(queueinfo.totalQueues());

  Serial.print("Being processed queue ID: ");
  Serial.println(queueinfo.currentQueueID());  

  Serial.print("Data type:");
  Serial.println(queueinfo.dataType()); 

  Serial.print("Method: ");
  Serial.println(queueinfo.firebaseMethod());

  Serial.print("Path: ");
  Serial.println(queueinfo.dataPath());

  Serial.println();
}
```



The following example showed how to run Error Queues and track its status manually.

```cpp
//In setup()

//Set the maximum Firebase Error Queues in collection (0 - 255).
//Firebase read/store operation causes by network problems and buffer overflow will be added to 
//Firebase Error Queues collection.
Firebase.RTDB.setMaxErrorQueue(&fbdo, 10);


//All of the following are in loop()

Firebase.RTDB.processErrorQueue(&fbdo);

//Detrnine the queue status
if (Firebase.RTDB.isErrorQueueFull(&fbdo))
{
  Serial.println("Queue is full");
}

//Remaining Error Queues in Error Queue collection
Serial.print("Remaining queues: ");
Serial.println(Firebase.RTDB.errorQueueCount(&fbdo));

//Assumed that queueID is unsigned integer array of queue that added to Error Queue collection 
//when error and use Firebase.getErrorQueueID to get this Error Queue id.

for (uint8_t i = 0; i < LENGTH_OF_QUEUEID_ARRAY; i++)
{
  Serial.print("Error Queue ");
  Serial.print(queueID[i]);
  if (Firebase.RTDB.isErrorQueueExisted(&fbdo, queueID[i]))
    Serial.println(" is queuing");
  else
    Serial.println(" is done");
}
Serial.println();
```



Error Queues can be saved as a file in SD/SDMMC card or flash memory with function `saveErrorQueue`.

The file systems for flash and SD memory can be changed via [**FirebaseFS.h**](/src/FirebaseFS.h).

Error Queues stored as a file can be restored to Error Queue collection with function `restoreErrorQueue`.

Two types of storage can be assigned with these functions, `mem_storage_type_flash` and `mem_storage_type_sd`.

The following example showed how to restore and save Error Queues in /test.txt file.

```cpp
//To restore Error Queues

if (Firebase.RTDB.errorQueueCount(&fbdo, "/test.txt", mem_storage_type_flash) > 0)
{
    Firebase.RTDB.restoreErrorQueue(&fbdo, "/test.txt", mem_storage_type_flash);
    Firebase.deleteStorageFile("/test.txt", mem_storage_type_flash);
}

//To save Error Queues to file
Firebase.RTDB.saveErrorQueue(&fbdo, "/test.txt", mem_storage_type_flash);

```

## FireSense, The Programmable Data Logging and IO Control (Add On)

This add on library is for the advance usages and works with Firebase RTDB.

With this add on library, you can remotely program your device to control its IOs or do some task or call predefined functions on the fly.

This allows you to change your device behaviour and functions without to flash a new firmware via serial or OTA.

See [examples/RTDB/FireSense](examples/RTDB/FireSense) for the usage.

For FireSense function description, see [src/addons/FireSense/README.md](src/addons/FireSense/README.md).

