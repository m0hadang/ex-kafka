
### ex) serde Topology

```kotlin
val builder = StreamsBuilder()
val kstream = builder.stream("tweets", Consumed.with(Serdes.String(), TweetSerdes()))
kstream.print(Printed.toSysOut<String?, Tweet?>().withLabel("tweets-stream"))
return builder.build()
```