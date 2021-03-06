/*
 * Copyright 2012 - 2013 Anton Tananaev (anton.tananaev@gmail.com)
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

public class V680ProtocolDecoder extends BaseProtocolDecoder {

    public V680ProtocolDecoder(V680Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .groupBegin()
            .num("#(d+)#")                       // imei
            .xpr("([^#]*)#")                     // user
            .groupEnd(true)
            .num("(d+)#")                        // fix
            .xpr("([^#]+)#")                     // password
            .xpr("([^#]+)#")                     // event
            .num("(d+)#")                        // packet number
            .xpr("([^#]+)?#?")                   // gsm base station
            .xpr("(?:[^#]+#)?")
            .num("(d+)?(dd.d+),")                // longitude
            .xpr("([EW]),")
            .num("(d+)?(dd.d+),")                // latitude
            .xpr("([NS]),")
            .num("(d+.d+),")                     // speed
            .num("(d+.?d*)?#")                   // course
            .num("(dd)(dd)(dd)#")                // date
            .num("(dd)(dd)(dd)")                 // time
            .any()
            .compile();

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg)
            throws Exception {

        String sentence = (String) msg;
        sentence = sentence.trim();

        if (sentence.length() == 16) {

            identify(sentence.substring(1, sentence.length()), channel);

        } else {

            Parser parser = new Parser(PATTERN, sentence);
            if (!parser.matches()) {
                return null;
            }

            Position position = new Position();
            position.setProtocol(getProtocolName());

            if (parser.hasNext()) {
                identify(parser.next(), channel);
            }
            if (!hasDeviceId()) {
                return null;
            }
            position.setDeviceId(getDeviceId());

            position.set("user", parser.next());
            position.setValid(parser.nextInt() > 0);
            position.set("password", parser.next());
            position.set(Event.KEY_EVENT, parser.next());
            position.set("packet", parser.next());
            position.set(Event.KEY_GSM, parser.next());

            position.setLongitude(parser.nextCoordinate());
            position.setLatitude(parser.nextCoordinate());
            position.setSpeed(parser.nextDouble());
            position.setCourse(parser.nextDouble());

            int day = parser.nextInt();
            int month = parser.nextInt();
            if (day == 0 && month == 0) {
                return null; // invalid date
            }

            DateBuilder dateBuilder = new DateBuilder()
                    .setDate(parser.nextInt(), month, day)
                    .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());
            position.setTime(dateBuilder.getDate());

            return position;
        }

        return null;
    }

}
