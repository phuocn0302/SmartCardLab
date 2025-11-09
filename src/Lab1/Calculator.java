package Lab1;

import javacard.framework.*;

public class Calculator extends Applet
{

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Calculator().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
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
		short result = 0;
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case (byte)0x00: // Divide
			result = (short) (p1 / p2);
			break;
		case (byte)0x01: // Multiply
			result = (short) (p1 * p2);
			break;
		case (byte)0x02: // Add
			result = (short) (p1 + p2);
			break;
		case (byte)0x03: // Subtract
			result = (short) (p1 - p2);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
		
		Util.setShort(buf, (short) 0, result);
		apdu.setOutgoingAndSend((short) 0, (short) 2);
		
	}

}
