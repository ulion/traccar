package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolDecoderTest;

public class SanavProtocolDecoderTest extends ProtocolDecoderTest {

    @Test
    public void testDecode() throws Exception {

        SanavProtocolDecoder decoder = new SanavProtocolDecoder(new SanavProtocol());
        
        verifyPosition(decoder, text(
                "imei=352024028982787&rmc=$GPRMC,103048.000,A,4735.0399,N,01905.2895,E,0.00,0.00,171013,,*05,AUTO-4095mv"));

        verifyPosition(decoder, text(
                "imei:352024028980000rmc:$GPRMC,093604.354,A,4735.0862,N,01905.2146,E,0.00,0.00,171013,,*09,AUTO-4103mv"));

        verifyPosition(decoder, text(
                "imei:352024027800000rmc:$GPRMC,000025.000,A,4735.0349,N,01905.2899,E,0.00,202.97,171013,,*03,3950mV,AUTO"));

        verifyPosition(decoder, text(
                "imei:352024020976845rmc:$GPRMC,000201.000,A,4655.7043,N,01941.3796,E,0.54,159.14,171013,,,A*65,AUTO"));

        verifyPosition(decoder, text(
                "imei=352024028982787&rmc=$GPRMC,103048.000,A,4735.0399,N,01905.2895,E,0.00,0.00,171013,,"));

        verifyPosition(decoder, text(
                "65,AUTOimei=352024028982787&rmc=$GPRMC,103048.000,A,4735.0399,N,01905.2895,E,0.00,0.00,171013,,"));

    }

}
