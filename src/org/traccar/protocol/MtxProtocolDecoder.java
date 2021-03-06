/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import java.net.SocketAddress;
import java.util.regex.Pattern;
import org.jboss.netty.channel.Channel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.Parser;
import org.traccar.helper.PatternBuilder;
import org.traccar.model.Event;
import org.traccar.model.Position;

public class MtxProtocolDecoder extends BaseProtocolDecoder {

    public MtxProtocolDecoder(MtxProtocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .txt("#MTX,")
            .num("(d+),")                        // IMEI
            .num("(dddd)(dd)(dd),")              // Date
            .num("(dd)(dd)(dd),")                // Time
            .num("(-?d+.d+),")                   // Latitude
            .num("(-?d+.d+),")                   // Longitude
            .num("(d+.?d*),")                    // Speed
            .num("(d+),")                        // Course
            .num("(d+.?d*),")                    // Odometer
            .groupBegin()
            .num("d+")
            .or()
            .txt("X")
            .groupEnd(false)
            .txt(",")
            .xpr("(?:[01]|X),")
            .xpr("([01]+),")                     // Input
            .xpr("([01]+),")                     // Output
            .num("(d+),")                        // ADC1
            .num("(d+)")                         // ADC2
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        if (channel != null) {
            channel.write("#ACK");
        }

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());

        if (!identify(parser.next(), channel)) {
            return null;
        }
        position.setDeviceId(getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setDate(parser.nextInt(), parser.nextInt(), parser.nextInt())
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.setValid(true);
        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());
        position.setSpeed(parser.nextDouble());
        position.setCourse(parser.nextDouble());

        position.set(Event.KEY_ODOMETER, parser.nextDouble());
        position.set(Event.KEY_INPUT, parser.next());
        position.set(Event.KEY_OUTPUT, parser.next());
        position.set(Event.PREFIX_ADC + 1, parser.next());
        position.set(Event.PREFIX_ADC + 2, parser.next());

        return position;
    }

}
