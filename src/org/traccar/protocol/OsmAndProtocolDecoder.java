/*
 * Copyright 2013 Anton Tananaev (anton.tananaev@gmail.com)
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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.traccar.BaseProtocolDecoder;
import org.traccar.model.Event;
import org.traccar.model.Position;

public class OsmAndProtocolDecoder extends BaseProtocolDecoder {

    public OsmAndProtocolDecoder(OsmAndProtocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg)
            throws Exception {

        List<Position> positions = new LinkedList<>();

        HttpRequest request = (HttpRequest) msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> params = decoder.getParameters();
        if (params.isEmpty()) {
            String body = request.getContent().toString(Charset.defaultCharset());
            String[] dataArray = body.split("\\n");
            for (String data: dataArray) {
                decoder = new QueryStringDecoder(data, false);
                params = decoder.getParameters();
                if (!params.isEmpty()) {
                    positions.add(decodePositionFromParams(params, channel));
                }
            }
        }
        else {
            positions.add(decodePositionFromParams(params, channel));
        }

        // Send response
        if (channel != null) {
            HttpResponse response = new DefaultHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            channel.write(response).addListener(ChannelFutureListener.CLOSE);
        }

        if (positions.size() < 1)
            return null;
        if (positions.size() == 1)
            return positions.get(0);
        return positions;
    }

    private Position decodePositionFromParams(Map<String, List<String>> params, Channel channel) throws Exception {
        // Create new position
        Position position = new Position();
        position.setProtocol(getProtocolName());

        // Identification
        String id;
        if (params.containsKey("id")) {
            id = params.get("id").get(0);
        } else {
            id = params.get("deviceid").get(0);
        }
        if (!identify(id, channel)) {
            return null;
        }
        position.setDeviceId(getDeviceId());

        // Decode position
        position.setValid(true);
        if (params.containsKey("timestamp")) {
            try {
                long timestamp = Long.parseLong(params.get("timestamp").get(0));
                if (timestamp < Integer.MAX_VALUE) {
                    timestamp *= 1000;
                }
                position.setTime(new Date(timestamp));
            } catch (NumberFormatException error) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                position.setTime(dateFormat.parse(params.get("timestamp").get(0)));
            }
        } else {
            position.setTime(new Date());
        }
        position.setLatitude(Double.parseDouble(params.get("lat").get(0)));
        position.setLongitude(Double.parseDouble(params.get("lon").get(0)));

        // Optional parameters
        if (params.containsKey("speed")) {
            position.setSpeed(Double.parseDouble(params.get("speed").get(0)));
        }

        if (params.containsKey("bearing")) {
            position.setCourse(Double.parseDouble(params.get("bearing").get(0)));
        } else if (params.containsKey("heading")) {
            position.setCourse(Double.parseDouble(params.get("heading").get(0)));
        }

        if (params.containsKey("altitude")) {
            position.setAltitude(Double.parseDouble(params.get("altitude").get(0)));
        }

        if (params.containsKey("hdop")) {
            position.set(Event.KEY_HDOP, params.get("hdop").get(0));
        }

        if (params.containsKey("vacc")) {
            position.set("vacc", params.get("vacc").get(0));
        }

        if (params.containsKey("hacc")) {
            position.set("hacc", params.get("hacc").get(0));
        }

        if (params.containsKey("batt")) {
            position.set(Event.KEY_BATTERY, params.get("batt").get(0));
        }

        if (params.containsKey("desc")) {
            position.set("description", params.get("desc").get(0));
        }

        return position;
    }
}
