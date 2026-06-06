### record schema
- define record file name and data type
- strongly constrains data formats between different applications and services.

### schema registry
- supply schema management tool

### avro
- generic record
  - using when dosen't enable to find record schema
  - it is used when the record schema specification cannot be verified at build time.
  - ex)
    - `GenericRecord.get(String key)`
    - `GenericRecord.put(String key, Object value)`
- specific record
  - code that made by avro schema file
  - it is used when the record schema specification can be verified at build time.
  - it's like protobuf

- ex)
```
{
    "namespace": "com.magicalpipelines.model",
    "name": "EntitySentiment",
    "type": "record",
    "fields": [
      {
        "name": "created_at",
        "type": "long"
      },
      {
        "name": "id",
        "type": "long"
      },
      {
        "name": "entity",
        "type": "string"
      },
...
    ]
  }
  
```

### sink operation
- to
- through
- repartition