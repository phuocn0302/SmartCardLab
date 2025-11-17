package Lab2Ex;

import javacard.framework.*;

/***
	
	/select 223344556700
	
	/send "00 ff 00 00 05 0102030405"
	
	/send "00 00 00 00 05 0102030401"
	
	/send "00 00 00 00 05 0102030405"

***/

public class PINAuth extends Applet
{
	public static final byte INS_SET_PIN = (byte) 0xff;
	public static final byte INS_ENTER_PIN = (byte) 0x00;
	
	private byte[] PIN = new byte[5];
	private short wrongTryCounter;

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new PINAuth().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		switch (buf[ISO7816.OFFSET_INS])
		{
		case INS_SET_PIN:
			setPin(apdu, buf);
			break;
		case INS_ENTER_PIN:
			enterPin(apdu, buf);
			
			Util.setShort(buf, (short) 0, wrongTryCounter);
			apdu.setOutgoingAndSend((short) 0, (short) 2);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}


	private void setPin(APDU apdu, byte[] buf) {
		short len = apdu.setIncomingAndReceive();
		
		if (len != PIN.length) {
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		}
		
		Util.arrayCopy(buf, (short) ISO7816.OFFSET_CDATA, PIN, (short) 0, len);
	}
	
	private void enterPin(APDU apdu, byte[] buf) {
		short len = apdu.setIncomingAndReceive();
		
		if (len != PIN.length) {
			wrongTryCounter++;
			return;
		}
		
		for (short i = 0; i < len; i++) {
			if (PIN[i] != buf[(short) (ISO7816.OFFSET_CDATA + i)]) {
				wrongTryCounter++;
				return;
			}
		}
		
		wrongTryCounter = 0;
	} 
}
