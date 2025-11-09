package Lab1Ex;

import javacard.framework.*;

/***

	/select 112233445601

	/send "00 00 00 00"
	Expected output: 50 68 75 6F 63 90 00
					 P  h  u  o  c

	/send "00 01 00 00"
	Expected output: 32 33 2F 30 32 2F 32 30 30 33 90 00
					 2  3  /  0  2  /  2  0  0  3
					 
	/send "00 02 00 00"
	Expected output: 50 68 75 6F 63 32 33 2F 30 32 2F 32 30 30 33 90 00
					 P  h  u  o  c  2  3  /  0  2  /  2  0  0  3
					 
***/

public class Bai2 extends Applet
{
	public static final byte INS_NAME = 0x00;
	public static final byte INS_BIRTH = 0x01;
	public static final byte INS_NAME_BIRTH = 0x02;
		
	private static byte[] hoTen = {'P', 'h', 'u', 'o', 'c'};
    private static byte[] ngaySinh = {'2', '3', '/', '0', '2', '/', '2', '0', '0', '3'};

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Bai2().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		
		apdu.setIncomingAndReceive();
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case INS_NAME:
			printData(apdu, buf, hoTen);
			break;
		case INS_BIRTH:
			printData(apdu, buf, ngaySinh);
			break;
		case INS_NAME_BIRTH:
			printData(apdu, buf, hoTen, ngaySinh);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	private void printData(APDU apdu, byte[] buf, byte[] data) {
		short len = (short) data.length;
		Util.arrayCopy(data, (short) 0, buf, (short) 0, len);
		apdu.setOutgoingAndSend((short) 0, (short) len);
	}

	private void printData(APDU apdu, byte[] buf, byte[] data1, byte[] data2) {
		short d1Len = (short) data1.length;
		short d2Len = (short) data2.length;
		
		Util.arrayCopy(data1, (short) 0, buf, (short) 0, d1Len);
		Util.arrayCopy(data2, (short) 0, buf, d1Len, d2Len);
		
		apdu.setOutgoingAndSend((short) 0, (short) (d1Len + d2Len));
	}
}
