package Lab1Ex;

import javacard.framework.*;

/***

==================================================

	/select 112233445603
	
	maSv = CT060330 
	hoTen = Phuoc
	ngaySinh = 23/02/2003
	queQuan = Ha Noi
	
	seperator = | x 3
	-> Lc = 8 + 5 + 10 + 6+ 3 = 32 (dec) = 20 (hex)
	
	/send "00 00 00 00 20 4354303630333330 7C 5068756F63 7C 32332F30322F32303033 7C 4861204E6F69"
					   Lc C T 0 6 0 3 3 0  |  P h u o c  |  2 3 / 0 2 / 2 0 0 3  |  H a   N o i
	
==================================================

	MaSV: CT060330
	
	/send "00 01 01 00"
	Expected output: 43 54 30 36 30 33 33 30 90 00
	
==================================================

	hoTen = Phuoc
	
	/send "00 01 02 00"
	Expected output: 50 68 75 6F 63 90 00
				
==================================================

	ngaySinh = 23/02/2003
	
	/send "00 01 03 00"
	Expected output: 32 33 2F 30 32 2F 32 30 30 33 90 00

==================================================

	queQuan = Ha Noi
	
	/send "00 01 04 00"
	Expected output: 48 61 20 4E 6F 69 90 00
	
==================================================
	
	/send "00 01 00 00"
	Expected output: 43 54 30 36 30 33 33 30 7C 50 68 75 6F 63 7C 32 33 2F 30 32 2F 32 30 30 33 7C 48 61 20 4E 6F 69 90 00
					 C  T  0  6  0  3  3  0  |  P  h  u  o  c  |  2  3  /  0  2  /  2  0  0  3  |  H  a     N  o  i
	
==================================================
					 
***/

public class Bai4 extends Applet
{
	private static final byte INS_SET_INFO = (byte)0x00;
	private static final byte INS_GET_INFO = (byte)0x01;
	
	private static final short P1_GET_ALL = 0;
	private static final short P1_GET_MASV = 1;
	private static final short P1_GET_HOTEN = 2;
	private static final short P1_GET_NGAYSINH = 3;
	private static final short P1_GET_QUEQUAN = 4;


	private byte[] maSV;
	private byte[] hoTen;
	private byte[] ngaySinh;
	private byte[] queQuan;

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Bai4().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
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
			switch((short) buf[ISO7816.OFFSET_P1])
			{
			case P1_GET_ALL:
				sendAllData(apdu, buf);
				break;
			case P1_GET_MASV:
				sendData(apdu, buf, maSV);
				break;
			case P1_GET_HOTEN:
				sendData(apdu, buf, hoTen);
				break;
			case P1_GET_NGAYSINH:
				sendData(apdu, buf, ngaySinh);
				break;
			case P1_GET_QUEQUAN:
				sendData(apdu, buf, queQuan);
				break;
			}
			break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}


	private void setInfo(APDU apdu, byte[] buf) {
		short len = apdu.setIncomingAndReceive();
		short start = ISO7816.OFFSET_CDATA;
		short pos = start;
		short end = (short)(start + len);

		short sep1 = -1;
		short sep2 = -1;
		short sep3 = -1;

		for (; pos < end; pos++) {
			if (buf[pos] == '|') {
				if (sep1 == -1) sep1 = pos;
				else if (sep2 == -1) sep2 = pos;
				else if (sep3 == -1) sep3 = pos;
			}
		}

		if (sep1 == -1 || sep2 == -1 || sep3 == -1)
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);

		short len1 = (short)(sep1 - start);
		short len2 = (short)(sep2 - sep1 - 1);
		short len3 = (short)(sep3 - sep2 - 1);
		short len4 = (short)(end - sep3 - 1);

		maSV = new byte[len1];
		Util.arrayCopy(buf, start, maSV, (short)0, len1);

		hoTen = new byte[len2];
		Util.arrayCopy(buf, (short)(sep1 + 1), hoTen, (short)0, len2);

		ngaySinh = new byte[len3];
		Util.arrayCopy(buf, (short)(sep2 + 1), ngaySinh, (short)0, len3);

		queQuan = new byte[len4];
		Util.arrayCopy(buf, (short)(sep3 + 1), queQuan, (short)0, len4);
	}


	private void sendData(APDU apdu, byte[] buf, byte[] data) {
		if (data == null) {
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		}
		
		apdu.setIncomingAndReceive();
		
		short len = (short) data.length;
		Util.arrayCopy(data, (short) 0, buf, (short) 0, len);
		apdu.setOutgoingAndSend((short) 0, len);
	}

	private void sendAllData(APDU apdu, byte[] buf) {
		if (maSV == null || hoTen == null || ngaySinh == null || queQuan == null) {
			ISOException.throwIt(ISO7816.SW_DATA_INVALID); 
		}
		
		apdu.setIncomingAndReceive();
		
		byte[] separator = {'|'};
		
		short maSvLen = (short) maSV.length;
		short hoTenLen = (short) hoTen.length;
		short ngaySinhLen = (short) ngaySinh.length;
		short queQuanLen = (short) queQuan.length;
		
		short totalLen = (short) (maSvLen + hoTenLen + ngaySinhLen + queQuanLen + (short) 3);
		
		apdu.setOutgoing();
		apdu.setOutgoingLength(totalLen);
		
		apdu.sendBytesLong(maSV, (short) 0, maSvLen);

		apdu.sendBytesLong(separator, (short) 0, (short) 1);

		apdu.sendBytesLong(hoTen, (short) 0, hoTenLen);

		apdu.sendBytesLong(separator, (short) 0, (short) 1);

		apdu.sendBytesLong(ngaySinh, (short) 0, ngaySinhLen);

		apdu.sendBytesLong(separator, (short) 0, (short) 1);

		apdu.sendBytesLong(queQuan, (short) 0, queQuanLen);
	}
}