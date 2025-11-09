package Lab1;

import javacard.framework.*;

public class Lab1 extends Applet
{
	private byte[] helloText = {'h', 'e', 'l', 'l', 'o'};

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Lab1().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		
		apdu.setIncomingAndReceive();
		
		short len = (short) helloText.length;
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case (byte)0x00:
			apdu.setOutgoing();
			apdu.setOutgoingLength(len);
			
			Util.arrayCopy(helloText, (short) 0, buf, (short) 0, len);
			apdu.sendBytes((short) 0, len);
			break;
			
		case (byte)0x01:
			Util.arrayCopy(helloText, (short) 0, buf, (short) 0, len);
			apdu.setOutgoingAndSend((short) 0, (short) helloText.length);
			break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

}
