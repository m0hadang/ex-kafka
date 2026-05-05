package org.mohadang.cryptosentiment

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Printed


class CryptoTopology {
    companion object {
        fun basicBuild(): Topology {
            val builder = StreamsBuilder()

            // create kstream instance
            // - basically, kafka streams process data as byte array
            // - when send or save data, kafka stream use zero-copy way
            val kstream = builder.stream<ByteArray?, ByteArray?>("tweets")

            kstream.print(Printed.toSysOut<ByteArray?, ByteArray?>().withLabel("tweets-stream"))

            return builder.build()
        }
    }
}