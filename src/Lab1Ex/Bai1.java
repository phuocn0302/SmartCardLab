package Lab1Ex;

import javacard.framework.*;


/***

	/select 112233445600

	/send "00 00 00 00"
	Expected output: 58 69 6E 20 63 68 61 6F 2C 20 4B 4D 41 90 00
					 X  i  n     c  h  a  o  ,     K  M  A
***/

public class Bai1 extends Applet
{
	private static byte[] text = {'X', 'i', 'n', ' ', 'c', 'h', 'a', 'o', ',', ' ', 'K', 'M', 'A'};

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Bai1().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		
		apdu.setIncomingAndReceive();
		
		short len = (short) text.length;
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case (byte)0x00:
			
			Util.arrayCopy(text, (short) 0, buf, (short) 0, len);
			apdu.setOutgoingAndSend((short) 0, (short) text.length);
			break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

}
