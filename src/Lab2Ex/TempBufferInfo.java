package Lab2Ex;

import javacard.framework.*;

/***

	/select 223344556701
	
	/send "00 00 00 00 20 4354303630333330 7C 5068756F63 7C 32332F30322F32303033 7C 4861204E6F69"
	
	/send "00 01 00 00"

***/

public class TempBufferInfo extends Applet
{

	private static final byte INS_SET_INFO = (byte) 0x00;
	private static final byte INS_GET_INFO = (byte) 0x01;

	private byte[] tempBuffer;
	
	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new TempBufferInfo().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
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
		case INS_SET_INFO:
			setInfo(apdu, buf);
			break;
		case INS_GET_INFO:
			getInfo(apdu, buf);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
	
	private void setInfo(APDU apdu, byte[] buf) {
		short len = apdu.setIncomingAndReceive();
		tempBuffer = JCSystem.makeTransientByteArray(len, JCSystem.CLEAR_ON_DESELECT);
		Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, tempBuffer, (short) 0, len);
	}
	
	private void getInfo(APDU apdu, byte[] buf) {
		if (tempBuffer == null) {
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		}
		
		apdu.setIncomingAndReceive();
		Util.arrayCopy(tempBuffer, (short) 0, buf, (short) 0, (short) tempBuffer.length);
		apdu.setOutgoingAndSend((short) 0, (short) tempBuffer.length);
		
		tempBuffer = null;
	}
	
}
