

# Stream-to-Stream Joins
- joins correlate events from two unbounded data streams
- correlate events by
  - matching keys
  - time windows
- maintain state for events within a time window.
  -  both inputs are continuously flowing
- Inner joins
  - emit results only when matching events exist in both streams within the time window
- Left joins 
  - emit all events from the left stream, with null values when no match exists in the right stream
- Outer joins
  - emit events from both streams, filling nulls when matches don't exist
- time window
  - determines how long the system waits for matching events
  - e.g., 10 min window : events with the same key must arrive within 10 min of each other to join successfully


# Stream-to-Table Joins
- enrich stream events with the latest state from a table(changelog stream)
  - The table represents the current snapshot of refrence data, continuously updated as change events arrive.
- enrichment scenarios 
  - stream events need contextual information from slowly changing dimensions
    - The table side maintains only the latest value per key, while the stream side flows through
    - Each stream event triggers a lookup against the current table state, producing an enriched output event.
  - e.g., 
    - enriching order events with current customer profile data
    - adding product information to clickstream events

# Temporal Join Semantics
- define which version of data gets joined
- Processing-time joins
  - "When did Kafka Streams see the event?"
  - use whatever data is available when the join operation executes
  - Processing-time joins are simpler but sacrifice reproducibility 
    - reprocessing historical data produces different results.
- Event-time joins 
  - "When did the event actually happen?"
  - use the timestamp embedded in the events themselves, ensuring deterministic results regardless of wehn processing occurs.
  - Event-time semantics provide correctness guarantees and reproducibility even with late-arriving or out-of-order data, making them preferable for most production scenarios.
- different clocks
  - Processing-time clock = your application/server clock
  - Event-time clock = timestamp inside the event


### e.g., pizza delivery(Alice changes her address after places an order)

Record of Events
```
10:00  Alice places an order
10:05  Alice changes her address
10:10  Network delay ends
10:10  Order arrives at Kafka Streams
```

order event(stream)
```
Order
--------
orderId = 1
user = Alice
eventTime = 10:00
```

user history change log(table)
```
09:00  Address = Old House
10:05  Address = New House
```

Processing-time Join
- Kafka Streams asks: "What is the user's address right now, when I'm processing this order?"
- The order finally arrives at 10:10.
- At 10:10 the latest profile is
```
Alice
Address = New House
```
- result: `Order -> New House`
> non-deterministic behavior.


Event-time Join
- Kafka Streams asks: "What was the user's address when this order happened (10:00)?"
- At 10:00 the profile is
```
Alice
Address = Old House
```
- result: `Order -> Old House`
> deterministic behavior.


Why are they different?
- network is slow
```
10:00  Order created
10:10  Kafka receives it
```

- Processing-time only knows : "I'm processing at 10:10."
- Event-time knows : "This event actually happened at 10:00."


### Why does Kafka Streams usually use event-time?


Real systems often have:
- network delays
- retries
- out-of-order events
- historical replay
- disaster recovery

- Processing-time 
  - these situations can produce different join results depending on when the data is processed.
- Event-time
  - Kafka Streams joins records according to when the business event actually occurred, so the same input data always produces the same output, even if events arrive late or you replay the entire topic
  - This makes event-time semantics much more suitable for production systems where correctness and reproducibility are important.

# Enrichment Patterns in Practice

### Lookup Enrichment

- enriching fast-moving event streams with reference data from slower-changing tables
- The pattern maintains user profiles as a KTable backed by a compacted kafka topic
  - a topic configured to retain only the latest value per key

e.g.,
```
// Kafka Streams example: Enrich clicks with user profiles
KStream<String, ClickEvent> clicks = builder.stream("clicks");
KTable<String, UserProfile> profiles = builder.table("user-profiles");

KStream<String, EnrichedClick> enriched = clicks
    .leftJoin(profiles, (click, profile) -> {
        // Each click event joins against the current profile state, producing an enriched event with combined information.
        return new EnrichedClick(click, profile);
    });
```

### Bidirectional Stream Correlation
- this pattern requires maintaining state for both streams within the join window, consuming more memory than stream-to-table joins
- the join window determines how far apart in time two events can be and still match
e.g.,

```
// Kafka Streams: Join payment streams with time window

// Fraud detection might join payment initiation events with payment authorization events, matching them within a time window to detect anomalies.
KStream<String, PaymentInit> initStream = builder.stream("payment-init");
KStream<String, PaymentAuth> authStream = builder.stream("payment-auth");

KStream<String, PaymentCorrelation> correlated = initStream
    .join(authStream,
        (init, auth) -> new PaymentCorrelation(init, auth),
        // payment initialization and authorization must occur within 5 minutes of each other.
        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(5))
    );
```

### Multi-Step Enrichment
- creating a pipeline of joins with multiple enrichment steps
- e.g.,
  - An order event might first be enriched with customer data, then with product details, then with inventory status.
- carefully to avoid excessive state an and latency
  - eatch join step adds processing time and state storage requirements

# Windowing and Time Semantics

> Time windows are fundamental to stream joins

- defining how long the system retains events waiting for matches


- Tumbling Windows
  - Timbling windows divide time into fixed, non-overlapping intervals
  - e.g., 1 hour tumbling window creates distinct buckets
    - 00:00-01:00, 01:00-02:00, etc.
  - Events within the same window can join, but events in adjacent windows cannot
- Sliding Windows
  - define a time range relative to each event
  - e.g., 10-minute sliding window
    - an event at 12:05:00 can join with events from 11:55:00(-10 min) to 12:15:00(+10 min). 
- Session Windows
  - session windows group events by periods of activity separated by inactivity gaps

### Choosing Window Sizes

Window size directly impacts both correctness and resource usage. Consider these factors:

- Business requirements
  - How far apart can related events occur? Payment authorization typically happens within seconds to minutes of initiation, while click-to-conversion might span hours or days.
- Resource constraints
  - Larger windows require more state storage. A 1-hour window with 10,000 events/second buffers ~36 million events. A 10-minute window buffers only 6 million events - a 6x reduction in memory/disk requirements.
- Latency tolerance
  - Longer windows delay results. If you need real-time alerts, use shorter windows (seconds to minutes). For analytical workloads, longer windows (hours) are acceptable.
- Practical starting points:
  - Real-time correlation (fraud detection, system monitoring): 1-5 minutes
  - User session analysis: 15-30 minutes
  - Click-to-conversion tracking: 1-24 hours
  - IoT sensor correlation: 5-30 seconds

### Late Data Handling

- Real-world streams produce late-arriving events 
  - network delays, system failures, or mobile devices coming back online, etc.
- Grace periods extend how long the system accepts late data after a window closes
  - e.g., 10-minute join window and a 2-minute grace period, an event arriving 11 minutes after its timestamp will still be processed (within grace), but one arriving after 12 minutes will be dropped as too late.

# Challenges and Best Practices

- State Management
  - arge windows or high-throughput streams create significant state storage requirements
  - e.g., A 1-hour join window, 10,000 events/second => might buffer millions of events, requiring several GB of storage.
  - Monitor state store size and tune retention policies to balance completeness with resource usage.
  - RocksDB-based state stores (used by both Kafka Streams and Flink) provide disk-backed storage for large state with memory caching for performance.
- For Kafka Streams specifically, state stores are backed by changelog topics for fault tolerance, enabling recovery after failures

- Data Skew
  - Uneven key distribution causes some partitions to handle disproportionate load.
  -  e.g., A celebrity user ID or popular product might generate far more events than others, creating hotspots.
- Mitigation strategies
  - increasing parallelism
  - redesigning partitioning strategies.
  - include key salting (adding randomness to keys) ??
- Join Ordering
  - When enriching with multiple tables, join order affects performance and state requirements
  - Join with smaller tables first to reduce intermediate data volume.
- Testing, Monitoring


# Key takeaways:

- Understand KStream vs KTable abstractions before implementing joins
- Use stream-to-table joins for enrichment with reference data
- Use stream-to-stream joins for correlating active event streams
- Choose event-time semantics for deterministic, correct results
- Size windows based on business requirements, resource constraints, and latency tolerance
- Monitor state size, watermark lag, and join throughput
- Handle late data with watermarks and grace periods
- Test join behavior with controlled time progression and chaos engineering tools
- Leverage Kafka 4.0+ with KRaft mode for improved rebalancing performance
- Use modern tools like Conduktor for consumer lag monitoring and data quality enforcement

### ref
- https://www.conduktor.io/glossary/stream-joins-and-enrichment-patterns#session-windows
- https://www.conduktor.io/glossary/windowing-in-apache-flink-tumbling-sliding-and-session-windows
- https://www.conduktor.io/glossary/session-windows-in-stream-processing
