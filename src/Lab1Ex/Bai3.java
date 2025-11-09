package Lab1Ex;

import javacard.framework.*;

/***

	/select 112233445602

	/send "00 00 03 02"
	Expected output: 00 06 90 00
					 3 x 2 = 6

	/send "00 01 03 02"
	Expected output: 00 03 90 00
					 (3 x 2) / 2 = 3
					 
	/send "00 01 05 03"
	Expected output: 00 07 90 00
					 (5 x 3) / 2 = 7.5 -> 7
					 
***/

public class Bai3 extends Applet
{
	public static final byte INS_RECT_AREA = 0x00;
	public static final byte INS_TRIANGLE_AREA = 0x01;

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Bai3().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		
		short p1 = buf[ISO7816.OFFSET_P1];
		short p2 = buf[ISO7816.OFFSET_P2];
		short retVal = 0;
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case INS_RECT_AREA:
			retVal = getRectArea(p1, p2);
			break;
		case INS_TRIANGLE_AREA:
			retVal = getTriangleArea(p1, p2);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
		
		Util.setShort(buf, (short) 0, retVal);
		apdu.setOutgoingAndSend((short) 0, (short) 2);
		
	}
	
	private short getTriangleArea(short base, short height) {
		return  (short) ((short) (base * height) / (short) 2);
	}
	
	private short getRectArea(short side1, short side2) {
		return (short) (side1 * side2);
	}
}
