package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolDecoderTest;

public class Tk103ProtocolDecoderTest extends ProtocolDecoderTest {

    @Test
    public void testDecode() throws Exception {

        Tk103ProtocolDecoder decoder = new Tk103ProtocolDecoder(new Tk103Protocol());

        verifyPosition(decoder, text(
                "(088047365460BP05354188047365460150929A3258.1754S02755.4323E009.4193927301.9000000000L00000000)"));

        verifyPosition(decoder, text(
                "(088048003342BP05354188048003342150917A1352.9801N10030.9050E000.0103115265.5600010000L000003F9)"));

        verifyPosition(decoder, text(
                "(088048003342BR00150917A1352.9801N10030.9050E000.0103224000.0000010000L000003F9)"));
        
        verifyPosition(decoder, text(
                "(088048003342BR00150807A1352.9871N10030.9084E000.0110718000.0001010000L00000000)"));

        verifyNothing(decoder, text(
                "(090411121854BP0000001234567890HSO"));

        verifyPosition(decoder, text(
                "(01029131573BR00150428A3801.6382N02351.0159E000.0080729278.7800000000LEF9ECB9C)"));

        verifyPosition(decoder, text(
                "(035988863964BP05000035988863964110524A4241.7977N02318.7561E000.0123536356.5100000000L000946BB"));

        verifyPosition(decoder, text(
                "(013632782450BP05000013632782450120803V0000.0000N00000.0000E000.0174654000.0000000000L00000000"));

        verifyPosition(decoder, text(
                "(013666666666BP05000013666666666110925A1234.5678N01234.5678W000.002033490.00000000000L000024DE"));
        
        verifyPosition(decoder, text(
                "(013666666666BO012110925A1234.5678N01234.5678W000.0025948118.7200000000L000024DE"));

        verifyPosition(decoder, text(
                "\n\n\n(088045133878BR00130228A5124.5526N00117.7152W000.0233614352.2200000000L01B0CF1C"));
        
        verifyPosition(decoder, text(
                "(008600410203BP05000008600410203130721A4152.5790N01239.2770E000.0145238173.870100000AL0000000"));
        
        verifyPosition(decoder, text(
                "(013012345678BR00130515A4843.9703N01907.6211E000.019232800000000000000L00009239"));
        
        verifyPosition(decoder, text(
                "(012345678901BP05000012345678901130520A3439.9629S05826.3504W000.1175622323.8700000000L000450AC"));
        
        verifyPosition(decoder, text(
                "(012345678901BR00130520A3439.9629S05826.3504W000.1175622323.8700000000L000450AC"));
        
        verifyPosition(decoder, text(
                "(352606090042050,BP05,240414,V,0000.0000N,00000.0000E,000.0,193133,000.0"));
        
        verifyPosition(decoder, text(
                "(352606090042050,BP05,240414,A,4527.3513N,00909.9758E,4.80,112825,155.49"));
        
        verifyPosition(decoder, text(
                "(013632782450,BP05,101201,A,2234.0297N,11405.9101E,000.0,040137,178.48,00000000,L00000000"));
        
        verifyPosition(decoder, text(
                "(864768010009188,BP05,271114,V,4012.19376N,00824.05638E,000.0,154436,000.0"));

        verifyPosition(decoder, text(
                "(013632651491,BP05,040613,A,2234.0297N,11405.9101E,000.0,040137,178.48)"));

        verifyPosition(decoder, text(
                "(013632651491,ZC07,040613,A,2234.0297N,11405.9101E,000.0,040137,178.48)"));

        verifyPosition(decoder, text(
                "(013632651491,ZC11,040613,A,2234.0297N,11405.9101E,000.0,040137,178.48)"));

        verifyPosition(decoder, text(
                "(013632651491,ZC12,040613,A,2234.0297N,11405.9101E,000.0,040137,178.48)"));

        verifyPosition(decoder, text(
                "(013632651491,ZC13,040613,A,2234.0297N,11405.9101E,000.0,040137,178.48)"));

        verifyPosition(decoder, text(
                "(013632651491,ZC17,040613,A,2234.0297N,11405.9101E,000.0,040137,178.48)"));

        verifyNothing(decoder, text(
                "(013632651491,ZC20,040613,040137,6,42,112,0)"));

        verifyPosition(decoder, text(
                "(094050000111BP05000094050000111150808A3804.2418N04616.7468E000.0201447133.3501000011L0028019DT000)"));

    }

}
